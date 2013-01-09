package net.engio.mbassy.listener;

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

    private boolean acceptsSubtypes = true;


    public MessageHandlerMetadata(Method handler, IMessageFilter[] filter, Listener listenerConfig) {
        this.handler = handler;
        this.filter = filter;
        this.listenerConfig = listenerConfig;
        this.isAsynchronous = listenerConfig.dispatch().equals(Mode.Asynchronous);
        this.envelope = handler.getAnnotation(Enveloped.class);
        this.acceptsSubtypes = !listenerConfig.rejectSubtypes();
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

    public boolean handlesMessage(Class<?> messageType){
        for(Class<?> handledMessage : handledMessages){
            if(handledMessage.equals(messageType))return true;
            if(handledMessage.isAssignableFrom(messageType) && acceptsSubtypes()) return true;
        }
        return false;
    }

    public boolean acceptsSubtypes(){
        return acceptsSubtypes;
    }


}
