package org.mbassy.listener;

import org.mbassy.listener.Listener;
import org.mbassy.listener.Mode;
import org.mbassy.listener.MessageFilter;

import java.lang.reflect.Method;

/**
 * @author bennidi
 * Date: 11/14/12
 */
public class MessageHandlerMetadata {

    private Method handler;

    private MessageFilter[] filter;

    private Listener listenerConfig;

    private boolean isAsynchronous = false;


    public MessageHandlerMetadata(Method handler, MessageFilter[] filter, Listener listenerConfig) {
        this.handler = handler;
        this.filter = filter;
        this.listenerConfig = listenerConfig;
        this.isAsynchronous = listenerConfig.dispatch().equals(Mode.Asynchronous);
    }


    public boolean isAsynchronous(){
        return isAsynchronous;
    }

    public boolean isFiltered(){
        return filter == null || filter.length == 0;
    }

    public int getPriority(){
        return listenerConfig.priority();
    }

    public Method getHandler() {
        return handler;
    }

    public MessageFilter[] getFilter() {
        return filter;
    }
}
