package net.engio.mbassy.listener;

import net.engio.mbassy.common.IPredicate;
import net.engio.mbassy.common.ReflectionUtils;
import net.engio.mbassy.subscription.MessageEnvelope;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The meta data reader is responsible for parsing and validating message handler configurations.
 *
 * @author bennidi
 *         Date: 11/16/12
 */
public class MetadataReader {

    //  This predicate is used to find all message listeners (methods annotated with @Handler)
    private static final IPredicate<Method> AllMessageHandlers = new IPredicate<Method>() {
        @Override
        public boolean apply(Method target) {
            return ReflectionUtils.getAnnotation(target, Handler.class) != null;
        }
    };

    // cache already created filter instances
    private final Map<Class<? extends IMessageFilter>, IMessageFilter> filterCache = new HashMap<Class<? extends IMessageFilter>, IMessageFilter>();

    // retrieve all instances of filters associated with the given subscription
    private IMessageFilter[] getFilter(Method method, Handler subscription, Class<?> targetClass) {
        Filter[] filterDefinitions = collectFilters(method, subscription);
        if (filterDefinitions.length == 0) {
            return null;
        }
        IMessageFilter[] filters = new IMessageFilter[filterDefinitions.length];
        int i = 0;
        for (Filter filterDef : filterDefinitions) {
            IMessageFilter filter = filterCache.get(filterDef.value());
            if (filter == null) {
                try {
                    filter = filterDef.value().newInstance();
                    filterCache.put(filterDef.value(), filter);
                } catch (Exception e) {
                    throw new RuntimeException(e);// propagate as runtime exception
                }
            }
            filters[i] = filter;
            i++;
        }
        return filters;
    }

    private Filter[] collectFilters(Method method, Handler subscription) {
        List<Filter> filters = new ArrayList<Filter>(subscription.filters().length);
        Collections.addAll(filters, subscription.filters());
        Annotation[] annotations = method.getAnnotations();
        for (int i = 0; i < method.getAnnotations().length; i++) {
            Class<? extends Annotation> annotationType = annotations[i].annotationType();
            IncludeFilters repeated = annotationType.getAnnotation(IncludeFilters.class);
            if (repeated != null) {
                Collections.addAll(filters, repeated.value());
            }
            Filter filter = annotationType.getAnnotation(Filter.class);
            if (filter != null) {
                filters.add(filter);
            }
        }
        return filters.toArray(new Filter[filters.size()]);
    }

    // get all listeners defined by the given class (includes
    // listeners defined in super classes and interfaces)
    public MessageListener getMessageListener(Class target) {
        MessageListener listenerMetadata = new MessageListener(target);

        // Step 1: Get all handlers from class hierarchy (existing logic)
        Method[] allHandlers = ReflectionUtils.getMethods(AllMessageHandlers, target);

        // Track which methods have been processed to avoid duplicates
        java.util.Set<String> processedMethods = new java.util.HashSet<String>();

        // Step 2: Process class-defined handlers
        processClassHandlers(allHandlers, target, listenerMetadata, processedMethods);

        // Step 3: Process interface-defined handlers (new functionality)
        processInterfaceHandlers(target, listenerMetadata, processedMethods);

        return listenerMetadata;
    }

    /**
     * Processes handlers that are directly annotated in the class hierarchy.
     */
    private void processClassHandlers(Method[] allHandlers, Class target, MessageListener listenerMetadata,
                                       java.util.Set<String> processedMethods) {
        final int length = allHandlers.length;

        Method handler;
        for (int i = 0; i < length; i++) {
            handler = allHandlers[i];

            // retain only those that are at the bottom of their respective class hierarchy (deepest overriding method)
            if (!ReflectionUtils.containsOverridingMethod(allHandlers, handler)) {

                // for each handler there will be no overriding method that specifies @Handler annotation
                // but an overriding method does inherit the listener configuration of the overridden method

                Handler handlerConfig = ReflectionUtils.getAnnotation(handler, Handler.class);
                Enveloped enveloped = ReflectionUtils.getAnnotation( handler, Enveloped.class );

                if (!handlerConfig.enabled() || !isValidMessageHandler(handler)) {
                    continue; // disabled or invalid listeners are ignored
                }

                Method overriddenHandler = ReflectionUtils.getOverridingMethod(handler, target);
                Method actualHandler = overriddenHandler == null ? handler : overriddenHandler;

                // Mark this method as processed
                String methodKey = getMethodKey(actualHandler);
                processedMethods.add(methodKey);

                // if a handler is overridden it inherits the configuration of its parent method
                Map<String, Object> handlerProperties = MessageHandler.Properties.Create(
                    actualHandler,
                    handlerConfig,
                    enveloped,
                    getFilter(handler, handlerConfig, target),
                    listenerMetadata);

                MessageHandler handlerMetadata = new MessageHandler(handlerProperties);
                listenerMetadata.addHandler(handlerMetadata);
            }
        }
    }

    /**
     * Processes handlers defined in interfaces but not in the class itself.
     * Only processes methods that:
     * 1. Are defined in an interface with @Handler annotation
     * 2. Are implemented in the target class
     * 3. Do NOT have their own @Handler annotation in the class (class annotation wins)
     * 4. Have not already been processed
     */
    private void processInterfaceHandlers(Class target, MessageListener listenerMetadata,
                                           java.util.Set<String> processedMethods) {
        // Get all interface methods with @Handler annotation
        Method[] interfaceHandlers = ReflectionUtils.getInterfaceMethods(AllMessageHandlers, target);

        for (Method interfaceMethod : interfaceHandlers) {
            // Find the corresponding method in the target class
            Method classMethod = findClassMethod(target, interfaceMethod);

            if (classMethod != null) {
                String methodKey = getMethodKey(classMethod);

                // Skip if already processed (either by class annotation or superclass)
                if (processedMethods.contains(methodKey)) {
                    continue;
                }

                // Check if the class method already has its own @Handler annotation
                // Use direct annotation check, not meta-annotation search
                Handler classAnnotation = classMethod.getAnnotation(Handler.class);

                if (classAnnotation == null) {
                    // No class annotation - inherit from interface
                    Handler interfaceAnnotation = ReflectionUtils.getAnnotation(interfaceMethod, Handler.class);
                    Enveloped interfaceEnveloped = ReflectionUtils.getAnnotation(interfaceMethod, Enveloped.class);

                    if (interfaceAnnotation != null && interfaceAnnotation.enabled() && isValidMessageHandler(interfaceMethod)) {
                        // Mark as processed
                        processedMethods.add(methodKey);

                        // Use interface annotation but actual class method for invocation
                        Map<String, Object> handlerProperties = MessageHandler.Properties.Create(
                            classMethod,
                            interfaceAnnotation,
                            interfaceEnveloped,
                            getFilter(interfaceMethod, interfaceAnnotation, target),
                            listenerMetadata);

                        MessageHandler handlerMetadata = new MessageHandler(handlerProperties);
                        listenerMetadata.addHandler(handlerMetadata);
                    }
                }
                // else: class annotation exists, already processed in processClassHandlers
            }
        }
    }

    /**
     * Creates a unique key for a method to track processed methods.
     */
    private String getMethodKey(Method method) {
        StringBuilder key = new StringBuilder();
        key.append(method.getName());
        for (Class<?> paramType : method.getParameterTypes()) {
            key.append("_").append(paramType.getName());
        }
        return key.toString();
    }

    /**
     * Finds the method in the target class that implements the given interface method.
     */
    private Method findClassMethod(Class<?> target, Method interfaceMethod) {
        try {
            return target.getMethod(interfaceMethod.getName(), interfaceMethod.getParameterTypes());
        } catch (NoSuchMethodException e) {
            return null;
        }
    }

    private boolean isValidMessageHandler(Method handler) {
        if (handler == null || ReflectionUtils.getAnnotation( handler, Handler.class) == null) {
            return false;
        }
        if (handler.getParameterTypes().length != 1) {
            // a messageHandler only defines one parameter (the message)
            System.out.println("Found no or more than one parameter in messageHandler [" + handler.getName()
                    + "]. A messageHandler must define exactly one parameter");
            return false;
        }
        Enveloped envelope = ReflectionUtils.getAnnotation( handler, Enveloped.class);
        if (envelope != null && !MessageEnvelope.class.isAssignableFrom(handler.getParameterTypes()[0])) {
            System.out.println("Message envelope configured but no subclass of MessageEnvelope found as parameter");
            return false;
        }
        if (envelope != null && envelope.messages().length == 0) {
            System.out.println("Message envelope configured but message types defined for handler");
            return false;
        }
        return true;
    }
}
