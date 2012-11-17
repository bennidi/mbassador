package org.mbassy.listener;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: benni
 * Date: 11/16/12
 * Time: 10:22 AM
 * To change this template use File | Settings | File Templates.
 */
public class MetadataReader {

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
}
