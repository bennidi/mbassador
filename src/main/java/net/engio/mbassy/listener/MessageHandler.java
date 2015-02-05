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

    // todo: have 1, 2, 3 versions
//    /**
//     * @return true if the message types are handled
//     */
//    public boolean handlesMessage(Class<?> requiredMessageType, Class<?>... optionalMessageTypes) {
//        // have to check ALL of the handled messages
//
//        Class<?>[] handledMessages = this.handledMessages;
//        // handle var-args
//        if (handledMessages.length == 1 && handledMessages[0].isArray()) {
//            Class<?> componentType = handledMessages[0].getComponentType();
//
//            // is requiredMessageType var-arg?
//            if ((optionalMessageTypes == null || optionalMessageTypes.length == 0) &&
//                requiredMessageType.isArray()) {
//
//                // only var-arg -> var-arg
//                return requiredMessageType.getComponentType() == componentType;
//            }
//
//            // otherwise, it's not a var-arg (but it still might be an array!)
//
//            // fast exit
//            if (requiredMessageType != componentType) {
//                return false;
//            }
//
//            // only using 1 arg
//            if (optionalMessageTypes == null || optionalMessageTypes.length == 0) {
//                return true;
//            }
//
//            // are the OPTIONAL arrays of same type??
//            if (optionalMessageTypes[0] != componentType) {
//                return false;
//            }
//
//            boolean ofSameType = true;
//            Class<?> typeCheck = optionalMessageTypes[0];
//            for (int i = 1; i < optionalMessageTypes.length; i++) {
//                Class<?> t1 = optionalMessageTypes[i];
//                if (t1 != typeCheck) {
//                    ofSameType = false;
//                    break;
//                }
//            }
//
//            return ofSameType && typeCheck == componentType;
//        } else {
//            // is requiredMessageType var-arg?
//            if ((optionalMessageTypes == null || optionalMessageTypes.length == 0) &&
//                requiredMessageType.isArray()) {
//
//                // only var-arg -> var-arg (handler var-arg is first check)
//                return false;
//            }
//
//            // otherwise, it's not a var-arg (but it still might be an array!)
//
//
//            // fast exit
//            if (requiredMessageType != this.handledMessages[0]) {
//                return false;
//            }
//
//            int length = this.handledMessages.length;
//            // is arg2 var-arg?
//            if (length == 2 && this.handledMessages[1].isArray()) {
//                Class<?> componentType = handledMessages[1].getComponentType();
//
//                // are they BOTH arrays of same type??
//                if (optionalMessageTypes.length == 1) {
//                    return optionalMessageTypes[0] == componentType;
//                }
//
//                boolean ofSameType = true;
//                Class<?> typeCheck = optionalMessageTypes[0];
//                for (int i = 1; i < optionalMessageTypes.length; i++) {
//                    Class<?> t1 = optionalMessageTypes[i];
//                    if (t1 != typeCheck) {
//                        ofSameType = false;
//                        break;
//                    }
//                }
//
//                return ofSameType && typeCheck == componentType;
//            }
//
//            // fast exit (check arg2)
//            if (optionalMessageTypes.length + 1 != length) {
//                return false;
//            }
//
//            // slow check
//            if (this.acceptsSubtypes) {
//                for (int i = 0; i < optionalMessageTypes.length; i++) {
//                    Class<?> messageType = optionalMessageTypes[i];
//                    Class<?> handledMessage = this.handledMessages[i+1];
//
//                    if (!handledMessage.isAssignableFrom(messageType)) {
//                        return false;
//                    }
//                }
//
//                return true;
//            } else {
//                for (int i = 0; i < optionalMessageTypes.length; i++) {
//                    Class<?> messageType = optionalMessageTypes[i];
//                    Class<?> handledMessage = this.handledMessages[i+1];
//
//                    if (handledMessage != messageType) {
//                        return false;
//                    }
//                }
//
//                return true;
//            }
//        }
//    }

//    /**
//     * @return true if the message types are handled
//     */
//    public boolean handlesMessage(Class<?>... messageTypes) {
//        // have to check ALL of the handled messages
//
//        Class<?>[] handledMessages = this.handledMessages;
//        // handle var-args
//        int length = handledMessages.length;
//
//        if (length == 1 && handledMessages[0].isArray()) {
//            Class<?> componentType = handledMessages[0].getComponentType();
//
//            // are they BOTH arrays of same type??
//            if (messageTypes.length == 1) {
//                return messageTypes[0].getComponentType() == componentType;
//            }
//
//            boolean ofSameType = true;
//            Class<?> typeCheck = messageTypes[0];
//            for (int i = 1; i < messageTypes.length; i++) {
//                Class<?> t1 = messageTypes[i];
//                if (t1 != typeCheck) {
//                    ofSameType = false;
//                    break;
//                }
//            }
//
//            return ofSameType && typeCheck == componentType;
//        } else {
//            // is the last handler a var-arg?
//            if (handledMessages[length-1].isArray()) {
//
//                // handler var-arg starting position
//                int handlerVA_startsAt = length-1;
//
//                // do they match up to the point var-args start?
//                if (messageTypes.length >= handlerVA_startsAt) {
//                    if (this.acceptsSubtypes) {
//                        for (int i = 1; i < length-1; i++) {
//                            Class<?> messageType = messageTypes[i];
//                            Class<?> handledMessage = handledMessages[i];
//
//                            if (!handledMessage.isAssignableFrom(messageType)) {
//                                return false;
//                            }
//                        }
//                    } else {
//                        for (int i = 1; i < length-1; i++) {
//                            Class<?> messageType = messageTypes[i];
//                            Class<?> handledMessage = handledMessages[i];
//
//                            if (handledMessage != messageType) {
//                                return false;
//                            }
//                        }
//                    }
//
//                    // they matched so far, do we have something for the handler var-arg?
//                    if (messageTypes.length == handlerVA_startsAt) {
//                        // nothing for var-arg check
//                        return true;
//                    }
//
//                    // need var-arg check
//
//                    // how many vars in the messageType need to be checked?
//
//
//                    Class<?> varArgCheckType = handledMessages[length-1].getComponentType();
//
//                    // are they BOTH arrays of same type??
//                    if (messageTypes.length messageTypes[handlerVA_startsAt].isArray()) {
//                        return messageTypes[handlerVA_startsAt].getComponentType() == varArgCheckType;
//                    }
//
//                    boolean ofSameType = true;
//                    Class<?> typeCheck = messageTypes[0];
//                    for (int i = 1; i < messageTypes.length; i++) {
//                        Class<?> t1 = messageTypes[i];
//                        if (t1 != typeCheck) {
//                            ofSameType = false;
//                            break;
//                        }
//                    }
//
//                    return ofSameType && typeCheck == varArgCheckType;
//
//                }
//
//
//
//
//
//
//
//
//            }
//
//
//            // fast exit
//            if (messageTypes.length != length) {
//                return false;
//            }
//
//            // fast check
//            if (Arrays.equals(handledMessages, messageTypes)) {
//                return true;
//            }
//
//            // slow check
//            if (this.acceptsSubtypes) {
//                for (int i = 0; i < length; i++) {
//                    Class<?> handledMessage = handledMessages[i];
//                    Class<?> messageType = messageTypes[i];
//
//                    if (!handledMessage.isAssignableFrom(messageType)) {
//                        return false;
//                    }
//                }
//
//                return true;
//            } else {
//                for (int i = 0; i < length; i++) {
//                    Class<?> handledMessage = handledMessages[i];
//                    Class<?> messageType = messageTypes[i];
//
//                    if (handledMessage != messageType) {
//                        return false;
//                    }
//                }
//
//                return true;
//            }
//        }
//    }


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


    public boolean acceptsSubtypes() {
        return this.acceptsSubtypes;
    }
}
