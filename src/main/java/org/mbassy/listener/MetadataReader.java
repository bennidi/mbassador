package org.mbassy.listener;

import org.mbassy.common.IPredicate;
import org.mbassy.common.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * @author bennidi
 * Date: 11/16/12
 * Time: 10:22 AM
 * To change this template use File | Settings | File Templates.
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
    private final Map<Class<? extends MessageFilter>, MessageFilter> filterCache = new HashMap<Class<? extends MessageFilter>, MessageFilter>();

    // retrieve all instances of filters associated with the given subscription
    private MessageFilter[] getFilter(Listener subscription) throws Exception{
        if (subscription.filters().length == 0) return null;
        MessageFilter[] filters = new MessageFilter[subscription.filters().length];
        int i = 0;
        for (Filter filterDef : subscription.filters()) {
            MessageFilter filter = filterCache.get(filterDef.value());
            if (filter == null) {
                    filter = filterDef.value().newInstance();
                    filterCache.put(filterDef.value(), filter);

            }
            filters[i] = filter;
            i++;
        }
        return filters;
    }


    public MessageHandlerMetadata getHandlerMetadata(Method messageHandler) throws Exception{
        Listener config = messageHandler.getAnnotation(Listener.class);
        MessageFilter[] filter = getFilter(config);
        return new MessageHandlerMetadata(messageHandler, filter, config);
    }

    // get all listeners defined by the given class (includes
    // listeners defined in super classes)
    public List<Method> getListeners(Class<?> target) {
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
        return ReflectionUtils.withoutOverridenSuperclassMethods(handlers);
    }

    private static boolean isHandler(Method m){
        Annotation[] annotations  = m.getDeclaredAnnotations();
        for(Annotation annotation : annotations){
            if(annotation.equals(Listener.class))return true;
        }
        return false;

    }

}
