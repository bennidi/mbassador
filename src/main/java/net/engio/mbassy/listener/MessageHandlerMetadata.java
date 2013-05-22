package net.engio.mbassy.listener;

import net.engio.mbassy.dispatch.HandlerInvocation;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * @author bennidi
 *         Date: 11/14/12
 */
public class MessageHandlerMetadata {

    private final Method handler;

    private final IMessageFilter[] filter;

    private final Handler handlerConfig;

    private final boolean isAsynchronous;

    private final Enveloped envelope;

    private final List<Class<?>> handledMessages = new LinkedList<Class<?>>();

    private final boolean acceptsSubtypes;

    private final MessageListenerMetadata listenerConfig;

    private final boolean isSynchronized;

    private Class listeningClass;


    public MessageHandlerMetadata(Method handler, IMessageFilter[] filter, Handler handlerConfig, MessageListenerMetadata listenerConfig) {
        if(handler == null || handlerConfig == null){
            throw new IllegalArgumentException("The message handler configuration may not be null");
        }
        this.handler = handler;
        this.filter = filter;
        this.handlerConfig = handlerConfig;
        this.isAsynchronous = handlerConfig.delivery().equals(Invoke.Asynchronously);
        this.envelope = handler.getAnnotation(Enveloped.class);
        this.acceptsSubtypes = !handlerConfig.rejectSubtypes();
        this.listenerConfig = listenerConfig;
        this.isSynchronized = handler.getAnnotation(Synchronized.class) != null;
        if (this.envelope != null) {
            for(Class messageType : envelope.messages()){
                handledMessages.add(messageType);
            }
        } else {
            handledMessages.add(handler.getParameterTypes()[0]);
        }
        this.handler.setAccessible(true);
    }

    public boolean isSynchronized(){
        return isSynchronized;
    }

    public boolean useStrongReferences(){
        return listenerConfig.useStrongReferences();
    }

    public boolean isFromListener(Class listener){
        return listenerConfig.isFromListener(listener);
    }

    public boolean isAsynchronous() {
        return isAsynchronous;
    }

    public boolean isFiltered() {
        return filter != null && filter.length > 0;
    }

    public int getPriority() {
        return handlerConfig.priority();
    }

    public Method getHandler() {
        return handler;
    }

    public IMessageFilter[] getFilter() {
        return filter;
    }

    public List<Class<?>> getHandledMessages() {
        return handledMessages;
    }

    public boolean isEnveloped() {
        return envelope != null;
    }

    public Class<? extends HandlerInvocation> getHandlerInvocation(){
        return handlerConfig.invocation();
    }

    public boolean handlesMessage(Class<?> messageType) {
        for (Class<?> handledMessage : handledMessages) {
            if (handledMessage.equals(messageType)) {
                return true;
            }
            if (handledMessage.isAssignableFrom(messageType) && acceptsSubtypes()) {
                return true;
            }
        }
        return false;
    }

    public boolean acceptsSubtypes() {
        return acceptsSubtypes;
    }


    public boolean isEnabled() {
        return handlerConfig.enabled();
    }
}
