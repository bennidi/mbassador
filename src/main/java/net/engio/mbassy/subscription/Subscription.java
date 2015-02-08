package net.engio.mbassy.subscription;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

import net.engio.mbassy.common.IConcurrentSet;
import net.engio.mbassy.common.WeakConcurrentSet;
import net.engio.mbassy.dispatch.IHandlerInvocation;
import net.engio.mbassy.dispatch.ReflectiveHandlerInvocation;
import net.engio.mbassy.dispatch.SynchronizedHandlerInvocation;
import net.engio.mbassy.error.ErrorHandlingSupport;
import net.engio.mbassy.error.PublicationError;
import net.engio.mbassy.listener.MessageHandler;

import com.esotericsoftware.reflectasm.MethodAccess;

/**
 * A subscription is a thread-safe container that manages exactly one message handler of all registered
 * message listeners of the same class, i.e. all subscribed instances (excluding subclasses) of a SingleMessageHandler.class
 * will be referenced in the subscription created for SingleMessageHandler.class.
 *
 * There will be as many unique subscription objects per message listener class as there are message handlers
 * defined in the message listeners class hierarchy.
 *
 * The subscription provides functionality for message publication by means of delegation to the respective
 * message dispatcher.
 *
 * @author bennidi
 * @author dorkbox, llc
 *         Date: 2/2/15
 */
public class Subscription {

    // the handler's metadata -> for each handler in a listener, a unique subscription context is created
    private final MessageHandler handlerMetadata;

    private final IHandlerInvocation invocation;
    protected final IConcurrentSet<Object> listeners;

    Subscription(MessageHandler handler) {
        this.listeners = new WeakConcurrentSet<Object>();
        this.handlerMetadata = handler;

        IHandlerInvocation invocation = new ReflectiveHandlerInvocation();
        if (handler.isSynchronized()){
            invocation = new SynchronizedHandlerInvocation(invocation);
        }

        this.invocation = invocation;
    }

    /**
     * Check whether this subscription manages a message handler of the given message listener class
     *
     * @param listener
     * @return
     */
    public boolean belongsTo(Class<?> listener){
        return this.handlerMetadata.isFromListener(listener);
    }

    /**
     * Check whether this subscriptions manages the given listener instance
     * @param listener
     * @return
     */
    public boolean contains(Object listener){
        return this.listeners.contains(listener);
    }

    /** Check if this subscription permits sending objects as a VarArg (variable argument) */
    public boolean isVarArg() {
        return this.handlerMetadata.isVarArg();
    }

    /**
     * Check whether this subscription manages a message handler
     */
    public boolean handlesMessageType(Class<?> messageType) {
        return this.handlerMetadata.handlesMessage(messageType);
    }

    /**
     * Check whether this subscription manages a message handler
     */
    public boolean handlesMessageType(Class<?> messageType1, Class<?> messageType2) {
        return this.handlerMetadata.handlesMessage(messageType1, messageType2);
    }

    /**
     * Check whether this subscription manages a message handler
     */
    public boolean handlesMessageType(Class<?> messageType1, Class<?> messageType2, Class<?> messageType3) {
        return this.handlerMetadata.handlesMessage(messageType1, messageType2, messageType3);
    }

    /**
     * Check whether this subscription manages a message handler
     */
    public boolean handlesMessageType(Class<?>... messageTypes) {
        return this.handlerMetadata.handlesMessage(messageTypes);
    }

    public Class<?>[] getHandledMessageTypes(){
        return this.handlerMetadata.getHandledMessages();
    }

    public void subscribe(Object listener) {
        this.listeners.add(listener);
    }


    /**
     * @return TRUE if the element was removed
     */
    public boolean unsubscribe(Object existingListener) {
        return this.listeners.remove(existingListener);
    }

    public boolean isEmpty() {
        return this.listeners.isEmpty();
    }

    public int size() {
        return this.listeners.size();
    }

    public void publishToSubscription(ErrorHandlingSupport errorHandler, Object message) {
        if (this.listeners.size() > 0) {
            MethodAccess handler = this.handlerMetadata.getHandler();
            int methodIndex = this.handlerMetadata.getMethodIndex();

            for (Object listener : this.listeners) {
                try {
                    this.invocation.invoke(listener, handler, methodIndex, message);
                } catch (IllegalAccessException e) {
                    errorHandler.handlePublicationError(new PublicationError()
                                                            .setMessage("Error during invocation of message handler. " +
                                                                        "The class or method is not accessible")
                                                            .setCause(e)
                                                            .setMethodName(handler.getMethodNames()[methodIndex])
                                                            .setListener(listener)
                                                            .setPublishedObject(message));
                } catch (IllegalArgumentException e) {
                    errorHandler.handlePublicationError(new PublicationError()
                                                            .setMessage("Error during invocation of message handler. " +
                                                                        "Wrong arguments passed to method. Was: " + message.getClass()
                                                                        + "Expected: " + handler.getParameterTypes()[0])
                                                            .setCause(e)
                                                            .setMethodName(handler.getMethodNames()[methodIndex])
                                                            .setListener(listener)
                                                            .setPublishedObject(message));
                } catch (InvocationTargetException e) {
                    errorHandler.handlePublicationError(new PublicationError()
                                                            .setMessage("Error during invocation of message handler. " +
                                                                        "Message handler threw exception")
                                                            .setCause(e)
                                                            .setMethodName(handler.getMethodNames()[methodIndex])
                                                            .setListener(listener)
                                                            .setPublishedObject(message));
                } catch (Throwable e) {
                    errorHandler.handlePublicationError(new PublicationError()
                                                            .setMessage("Error during invocation of message handler. " +
                                                                        "The handler code threw an exception")
                                                            .setCause(e)
                                                            .setMethodName(handler.getMethodNames()[methodIndex])
                                                            .setListener(listener)
                                                            .setPublishedObject(message));
                }
            }
        }
    }

    public void publishToSubscription(ErrorHandlingSupport errorHandler, Object message1, Object message2) {
        if (this.listeners.size() > 0) {
            MethodAccess handler = this.handlerMetadata.getHandler();
            int methodIndex = this.handlerMetadata.getMethodIndex();

            for (Object listener : this.listeners) {
                try {
                    this.invocation.invoke(listener, handler, methodIndex, message1, message2);
                } catch (IllegalAccessException e) {
                    errorHandler.handlePublicationError(new PublicationError()
                                                            .setMessage("Error during invocation of message handler. " +
                                                                        "The class or method is not accessible")
                                                            .setCause(e)
                                                            .setMethodName(handler.getMethodNames()[methodIndex])
                                                            .setListener(listener)
                                                            .setPublishedObject(message1, message2));
                } catch (IllegalArgumentException e) {
                    errorHandler.handlePublicationError(new PublicationError()
                                                            .setMessage("Error during invocation of message handler. " +
                                                                        "Wrong arguments passed to method. Was: " +
                                                                            message1.getClass() + ", " +
                                                                            message2.getClass()
                                                                        + ".  Expected: " + handler.getParameterTypes()[0] + ", " +
                                                                                            handler.getParameterTypes()[1]
                                                                            )
                                                            .setCause(e)
                                                            .setMethodName(handler.getMethodNames()[methodIndex])
                                                            .setListener(listener)
                                                            .setPublishedObject(message1, message2));
                } catch (InvocationTargetException e) {
                    errorHandler.handlePublicationError(new PublicationError()
                                                            .setMessage("Error during invocation of message handler. " +
                                                                        "Message handler threw exception")
                                                            .setCause(e)
                                                            .setMethodName(handler.getMethodNames()[methodIndex])
                                                            .setListener(listener)
                                                            .setPublishedObject(message1, message2));
                } catch (Throwable e) {
                    errorHandler.handlePublicationError(new PublicationError()
                                                            .setMessage("Error during invocation of message handler. " +
                                                                        "The handler code threw an exception")
                                                            .setCause(e)
                                                            .setMethodName(handler.getMethodNames()[methodIndex])
                                                            .setListener(listener)
                                                            .setPublishedObject(message1, message2));
                }
            }
        }
    }

    public void publishToSubscription(ErrorHandlingSupport errorHandler, Object message1, Object message2, Object message3) {
        if (this.listeners.size() > 0) {
            MethodAccess handler = this.handlerMetadata.getHandler();
            int methodIndex = this.handlerMetadata.getMethodIndex();

            for (Object listener : this.listeners) {
                try {
                    this.invocation.invoke(listener, handler, methodIndex, message1, message2, message3);
                } catch (IllegalAccessException e) {
                    errorHandler.handlePublicationError(new PublicationError()
                                                            .setMessage("Error during invocation of message handler. " +
                                                                        "The class or method is not accessible")
                                                            .setCause(e)
                                                            .setMethodName(handler.getMethodNames()[methodIndex])
                                                            .setListener(listener)
                                                            .setPublishedObject(message1, message2, message3));
                } catch (IllegalArgumentException e) {
                    errorHandler.handlePublicationError(new PublicationError()
                                                            .setMessage("Error during invocation of message handler. " +
                                                                        "Wrong arguments passed to method. Was: " +
                                                            message1.getClass() + ", " +
                                                            message2.getClass() + ", " +
                                                            message3.getClass()
                                                            + ".  Expected: " + handler.getParameterTypes()[0] + ", " +
                                                                                handler.getParameterTypes()[1] + ", " +
                                                                                handler.getParameterTypes()[2]
                                                            )
                                                            .setCause(e)
                                                            .setMethodName(handler.getMethodNames()[methodIndex])
                                                            .setListener(listener)
                                                            .setPublishedObject(message1, message2, message3));
                } catch (InvocationTargetException e) {
                    errorHandler.handlePublicationError(new PublicationError()
                                                            .setMessage("Error during invocation of message handler. " +
                                                                        "Message handler threw exception")
                                                            .setCause(e)
                                                            .setMethodName(handler.getMethodNames()[methodIndex])
                                                            .setListener(listener)
                                                            .setPublishedObject(message1, message2, message3));
                } catch (Throwable e) {
                    errorHandler.handlePublicationError(new PublicationError()
                                                            .setMessage("Error during invocation of message handler. " +
                                                                        "The handler code threw an exception")
                                                            .setCause(e)
                                                            .setMethodName(handler.getMethodNames()[methodIndex])
                                                            .setListener(listener)
                                                            .setPublishedObject(message1, message2, message3));
                }
            }
        }
    }

    public void publishToSubscription(ErrorHandlingSupport errorHandler, Object... messages) {
        if (this.listeners.size() > 0) {
            MethodAccess handler = this.handlerMetadata.getHandler();
            int methodIndex = this.handlerMetadata.getMethodIndex();

            for (Object listener : this.listeners) {
                try {
                    this.invocation.invoke(listener, handler, methodIndex, messages);
                } catch (IllegalAccessException e) {
                    errorHandler.handlePublicationError(new PublicationError()
                                                            .setMessage("Error during invocation of message handler. " +
                                                                            "The class or method is not accessible")
                                                            .setCause(e)
                                                            .setMethodName(handler.getMethodNames()[methodIndex])
                                                            .setListener(listener)
                                                            .setPublishedObject(messages));
                } catch (IllegalArgumentException e) {
                    errorHandler.handlePublicationError(new PublicationError()
                                                            .setMessage("Error during invocation of message handler. " +
                                                                        "Wrong arguments passed to method. Was: " + Arrays.deepToString(messages)
                                                                        + "Expected: " + Arrays.deepToString(handler.getParameterTypes()))
                                                            .setCause(e)
                                                            .setMethodName(handler.getMethodNames()[methodIndex])
                                                            .setListener(listener)
                                                            .setPublishedObject(messages));
                } catch (InvocationTargetException e) {
                    errorHandler.handlePublicationError(new PublicationError()
                                                            .setMessage("Error during invocation of message handler. " +
                                                                        "Message handler threw exception")
                                                            .setCause(e)
                                                            .setMethodName(handler.getMethodNames()[methodIndex])
                                                            .setListener(listener)
                                                            .setPublishedObject(messages));
                } catch (Throwable e) {
                    errorHandler.handlePublicationError(new PublicationError()
                                                            .setMessage("Error during invocation of message handler. " +
                                                                        "The handler code threw an exception")
                                                            .setCause(e)
                                                            .setMethodName(handler.getMethodNames()[methodIndex])
                                                            .setListener(listener)
                                                            .setPublishedObject(messages));
                }
            }
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (this.handlerMetadata == null ? 0 : this.handlerMetadata.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Subscription other = (Subscription) obj;
        if (this.handlerMetadata == null) {
            if (other.handlerMetadata != null) {
                return false;
            }
        } else if (!this.handlerMetadata.equals(other.handlerMetadata)) {
            return false;
        }
        return true;
    }
}
