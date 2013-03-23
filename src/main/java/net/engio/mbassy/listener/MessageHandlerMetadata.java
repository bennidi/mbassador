package net.engio.mbassy.listener;

import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author bennidi
 *         Date: 11/14/12
 */
public class MessageHandlerMetadata {

    private Method handler;

    private IMessageFilter[] filter;

    private Handler handlerConfig;

    private boolean isAsynchronous = false;

    private Enveloped envelope = null;

    private List<Class<?>> handledMessages = new LinkedList<Class<?>>();

    private boolean acceptsSubtypes = true;


    public MessageHandlerMetadata(Method handler, IMessageFilter[] filter, Handler handlerConfig) {
        this.handler = handler;
        this.filter = filter;
        this.handlerConfig = handlerConfig;
        this.isAsynchronous = handlerConfig.delivery().equals(Mode.Concurrent);
        this.envelope = handler.getAnnotation(Enveloped.class);
        this.acceptsSubtypes = !handlerConfig.rejectSubtypes();
        if (this.envelope != null) {
            Collections.addAll(handledMessages, envelope.messages());
        } else {
            handledMessages.add(handler.getParameterTypes()[0]);
        }
        this.handler.setAccessible(true);
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
