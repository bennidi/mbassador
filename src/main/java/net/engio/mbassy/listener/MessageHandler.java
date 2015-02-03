package net.engio.mbassy.listener;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import net.engio.mbassy.common.ReflectionUtils;

/**
 * Any method in any class annotated with the @Handler annotation represents a message handler. The class that contains
 * the handler is called a  message listener and more generally, any class containing a message handler in its class hierarchy
 * defines such a message listener.
 *
 * @author bennidi
 *         Date: 11/14/12
 */
public class MessageHandler {

    private final Method handler;
    private final Class<?>[] handledMessages;
    private final boolean acceptsSubtypes;
    private final MessageListener listenerConfig;

    private final boolean isSynchronized;


    public MessageHandler(Method handler, Handler handlerConfig, MessageListener listenerMetadata){
        super();

        if (handler == null) {
            throw new IllegalArgumentException("The message handler configuration may not be null");
        }

        Class<?>[] handledMessages = handler.getParameterTypes();
        handler.setAccessible(true);

        this.handler         = handler;
        this.acceptsSubtypes = !handlerConfig.rejectSubtypes();
        this.listenerConfig  = listenerMetadata;
        this.isSynchronized  = ReflectionUtils.getAnnotation(handler, Synchronized.class) != null;
        this.handledMessages = handledMessages;
    }

    public <A extends Annotation> A getAnnotation(Class<A> annotationType){
        return ReflectionUtils.getAnnotation(this.handler,annotationType);
    }

    public boolean isSynchronized(){
        return this.isSynchronized;
    }

    public boolean isFromListener(Class<?> listener){
        return this.listenerConfig.isFromListener(listener);
    }

    public Method getHandler() {
        return this.handler;
    }

    public Class<?>[] getHandledMessages() {
        return this.handledMessages;
    }

    public boolean handlesMessage(Class<?> messageType) {
        for (Class<?> handledMessage : this.handledMessages) {
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
        return this.acceptsSubtypes;
    }
}
