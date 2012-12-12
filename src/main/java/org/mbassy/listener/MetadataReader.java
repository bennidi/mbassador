package org.mbassy.listener;

import com.sun.xml.internal.messaging.saaj.soap.Envelope;
import org.mbassy.common.IPredicate;
import org.mbassy.common.ReflectionUtils;
import org.mbassy.subscription.MessageEnvelope;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.*;

/**
 *
 * The meta data reader is responsible for parsing and validating message handler configurations.
 *
 * @author bennidi
 * Date: 11/16/12
 */
public class MetadataReader {

    //  This predicate is used to find all message listeners (methods annotated with @Listener)
    private static final IPredicate<Method> AllMessageHandlers = new IPredicate<Method>() {
        @Override
        public boolean apply(Method target) {
            return target.getAnnotation(Listener.class) != null;
        }
    };

    // cache already created filter instances
    private final Map<Class<? extends IMessageFilter>, IMessageFilter> filterCache = new HashMap<Class<? extends IMessageFilter>, IMessageFilter>();

    // retrieve all instances of filters associated with the given subscription
    private IMessageFilter[] getFilter(Listener subscription){
        if (subscription.filters().length == 0) return null;
        IMessageFilter[] filters = new IMessageFilter[subscription.filters().length];
        int i = 0;
        for (Filter filterDef : subscription.filters()) {
            IMessageFilter filter = filterCache.get(filterDef.value());
            if (filter == null) {
                try{
                    filter = filterDef.value().newInstance();
                    filterCache.put(filterDef.value(), filter);
                }
                catch (Exception e){
                    throw new RuntimeException(e);// propagate as runtime exception
                }

            }
            filters[i] = filter;
            i++;
        }
        return filters;
    }


    public MessageHandlerMetadata getHandlerMetadata(Method messageHandler){
        Listener config = messageHandler.getAnnotation(Listener.class);
        return new MessageHandlerMetadata(messageHandler, getFilter(config), config);
    }

    // get all listeners defined by the given class (includes
    // listeners defined in super classes)
    public List<MessageHandlerMetadata> getMessageHandlers(Class<?> target) {
        List<Method> allMethods = ReflectionUtils.getMethods(AllMessageHandlers, target);
        List<Method>  handlers = new LinkedList<Method>();
        for(Method listener : allMethods){
            Method overriddenHandler = ReflectionUtils.getOverridingMethod(listener, target);

            if(overriddenHandler != null && isHandler(overriddenHandler)){
                handlers.add(overriddenHandler);
            }
            if(overriddenHandler == null){
                handlers.add(listener);
            }
        }
        handlers =  ReflectionUtils.withoutOverridenSuperclassMethods(handlers);
        List<MessageHandlerMetadata> messageHandlers = new ArrayList<MessageHandlerMetadata>(handlers.size());
        for(Method handler : handlers){
            if(isValidMessageHandler(handler))
                messageHandlers.add(getHandlerMetadata(handler));
        }
        return messageHandlers;
    }

    private static boolean isHandler(Method m){
        Annotation[] annotations  = m.getDeclaredAnnotations();
        for(Annotation annotation : annotations){
            if(annotation.equals(Listener.class))return true;
        }
        return false;
    }

    private boolean isValidMessageHandler(Method handler) {
        if (handler.getParameterTypes().length != 1) {
            // a messageHandler only defines one parameter (the message)
            System.out.println("Found no or more than one parameter in messageHandler [" + handler.getName()
                    + "]. A messageHandler must define exactly one parameter");
            return false;
        }
        Enveloped envelope = handler.getAnnotation(Enveloped.class);
        if(envelope != null && !MessageEnvelope.class.isAssignableFrom(handler.getParameterTypes()[0])){
            System.out.println("Message envelope configured but no subclass of MessageEnvelope found as parameter");
            return false;
        }
        if(envelope != null && envelope.messages().length == 0){
            System.out.println("Message envelope configured but message types defined for handler");
            return false;
        }
        return true;
    }

}
