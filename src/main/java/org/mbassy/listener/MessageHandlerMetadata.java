package org.mbassy.listener;

import java.lang.reflect.Method;

/**
 *
 *
 * @author bennidi
 * Date: 11/14/12
 */
public class MessageHandlerMetadata {

    private Method handler;

    private IMessageFilter[] filter;

    private Listener listenerConfig;

    private boolean isAsynchronous = false;


    public MessageHandlerMetadata(Method handler, IMessageFilter[] filter, Listener listenerConfig) {
        this.handler = handler;
        this.filter = filter;
        this.listenerConfig = listenerConfig;
        this.isAsynchronous = listenerConfig.dispatch().equals(Mode.Asynchronous);
        this.handler.setAccessible(true);
    }


    public boolean isAsynchronous(){
        return isAsynchronous;
    }

    public boolean isFiltered(){
        return filter != null && filter.length > 0;
    }

    public int getPriority(){
        return listenerConfig.priority();
    }

    public Method getHandler() {
        return handler;
    }

    public IMessageFilter[] getFilter() {
        return filter;
    }

    public Class getDeclaredMessageType(){
        return handler.getParameterTypes()[0];
    }
}
