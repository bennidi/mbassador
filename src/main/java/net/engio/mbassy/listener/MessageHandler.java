package net.engio.mbassy.listener;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import net.engio.mbassy.annotations.Handler;
import net.engio.mbassy.annotations.Synchronized;
import net.engio.mbassy.common.ReflectionUtils;

/**
 * Any method in any class annotated with the @Handler annotation represents a message handler. The class that contains
 * the handler is called a  message listener and more generally, any class containing a message handler in its class hierarchy
 * defines such a message listener.
 *
 * @author bennidi
 *         Date: 11/14/12
 * @author dorkbox, llc
 *         Date: 2/2/15
 */
public class MessageHandler {

    private final Method handler;
    private final Class<?>[] handledMessages;
    private final boolean acceptsSubtypes;
    private final MessageListener listenerConfig;

    private final boolean acceptsVarArg;
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
        this.acceptsVarArg   = handlerConfig.vararg();
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

    /*
     * @author dorkbox, llc
     *         Date: 2/2/15
     */
    /**
     * @return true if the message types are handled
     */
    public boolean handlesMessage(Class<?>... messageTypes) {
        Class<?>[] handledMessages = this.handledMessages;
        int handledLength = handledMessages.length;
        int handledLengthMinusVarArg = handledLength-1;

        int messagesLength = messageTypes.length;

        // do we even have enough to even CHECK the var-arg?
        if (messagesLength < handledLengthMinusVarArg) {
            // totally wrong number of args
            return false;
        }

        // check BEFORE var-arg in handler (var-arg can ONLY be last element in array)
        if (handledLengthMinusVarArg <= messagesLength) {
            if (this.acceptsSubtypes) {
                for (int i = 0; i < handledLengthMinusVarArg; i++) {
                    Class<?> handledMessage = handledMessages[i];
                    Class<?> messageType = messageTypes[i];

                    if (!handledMessage.isAssignableFrom(messageType)) {
                        return false;
                    }
                }
            } else {
                for (int i = 0; i < handledLengthMinusVarArg; i++) {
                    Class<?> handledMessage = handledMessages[i];
                    Class<?> messageType = messageTypes[i];

                    if (handledMessage != messageType) {
                        return false;
                    }
                }
            }
        }

        // do we even HAVE var-arg?
        if (!handledMessages[handledLengthMinusVarArg].isArray()) {
            // DO NOT HAVE VAR_ARG PRESENT IN HANDLERS

            // fast exit
            if (handledLength != messagesLength) {
                return false;
            }

            // compare remaining arg
            Class<?> handledMessage = handledMessages[handledLengthMinusVarArg];
            Class<?> messageType = messageTypes[handledLengthMinusVarArg];

            if (this.acceptsSubtypes) {
                if (!handledMessage.isAssignableFrom(messageType)) {
                    return false;
                }
            } else {
                if (handledMessage != messageType) {
                    return false;
                }
            }
            // all args are dandy
            return true;
        }

        // WE HAVE VAR_ARG PRESENT IN HANDLER

        // do we have enough args to NEED to check the var-arg?
        if (handledLengthMinusVarArg == messagesLength) {
            // var-arg doesn't need checking
            return true;
        }

        // then check var-arg in handler

        // all the args to check for the var-arg MUST be the same! (ONLY ONE ARRAY THOUGH CAN BE PRESENT)
        int messagesLengthMinusVarArg = messagesLength-1;

        Class<?> typeCheck = messageTypes[handledLengthMinusVarArg];
        for (int i = handledLengthMinusVarArg; i < messagesLength; i++) {
            Class<?> t1 = messageTypes[i];
            if (t1 != typeCheck) {
                return false;
            }
        }

        // if we got this far, then the args are the same type. IF we have more than one, AND they are arrays, NOPE!
        if (messagesLength - handledLengthMinusVarArg > 1 && messageTypes[messagesLengthMinusVarArg].isArray()) {
            return false;
        }

        // are we comparing array -> array or string -> array
        Class<?> componentType;
        if (messageTypes[messagesLengthMinusVarArg].isArray()) {
            componentType = handledMessages[handledLengthMinusVarArg];
        } else {
            componentType = handledMessages[handledLengthMinusVarArg].getComponentType();
        }

        if (this.acceptsSubtypes) {
            return componentType.isAssignableFrom(typeCheck);
        } else {
            return typeCheck == componentType;
        }
    }

    /** Check if this handler permits sending objects as a VarArg (variable argument) */
    public boolean isVarArg() {
        return this.acceptsVarArg;
    }
}
