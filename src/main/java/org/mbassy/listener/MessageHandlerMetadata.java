package org.mbassy.listener;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

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

    private Enveloped envelope = null;

    private List<Class<?>> handledMessages = new LinkedList<Class<?>>();


    public MessageHandlerMetadata(Method handler, IMessageFilter[] filter, Listener listenerConfig) {
        this.handler = handler;
        this.filter = filter;
        this.listenerConfig = listenerConfig;
        this.isAsynchronous = listenerConfig.dispatch().equals(Mode.Asynchronous);
        this.envelope = handler.getAnnotation(Enveloped.class);
        if(this.envelope != null){
            for(Class messageType : envelope.messages())
                handledMessages.add(messageType);
        }
        else{
            handledMessages.add(handler.getParameterTypes()[0]);
        }
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

    public List<Class<?>> getHandledMessages(){
        return handledMessages;
    }

    public boolean isEnveloped() {
        return envelope != null;
    }
}
