package net.engio.mbassy.subscription;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import net.engio.mbassy.bus.error.ErrorHandlingSupport;
import net.engio.mbassy.bus.error.PublicationError;
import net.engio.mbassy.common.IConcurrentSet;
import net.engio.mbassy.dispatch.IHandlerInvocation;
import net.engio.mbassy.listener.MessageHandler;

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
 */
public class Subscription {

    // the handler's metadata -> for each handler in a listener, a unique subscription context is created
    private final MessageHandler handlerMetadata;

    private final IHandlerInvocation invocation;
    protected final IConcurrentSet<Object> listeners;

    Subscription(MessageHandler handler, IHandlerInvocation invocation, IConcurrentSet<Object> listeners) {
        this.handlerMetadata = handler;
        this.invocation = invocation;
        this.listeners = listeners;
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

    public void subscribe(Object o) {
        this.listeners.add(o);
    }


    public boolean unsubscribe(Object existingListener) {
        return this.listeners.remove(existingListener);
    }

    public int size() {
        return this.listeners.size();
    }

    public void publishToSubscription(ErrorHandlingSupport errorHandler, Object message) {
        if (this.listeners.size() > 0) {

            /**
             * Delivers the given message to the given set of listeners.
             * Delivery may be delayed, aborted or restricted in various ways, depending
             * on the configuration of the dispatcher
             *
             * @param publication The message publication that initiated the dispatch
             * @param message     The message that should be delivered to the listeners
             * @param listeners   The listeners that should receive the message
             */
            Method handler = this.handlerMetadata.getHandler();

            for (Object listener : this.listeners) {
                try {
                    this.invocation.invoke(listener, handler, message);
                } catch (IllegalAccessException e) {
                    errorHandler.handlePublicationError(new PublicationError()
                                                            .setMessage("Error during invocation of message handler. " +
                                                                        "The class or method is not accessible")
                                                            .setCause(e)
                                                            .setHandler(handler)
                                                            .setListener(listener)
                                                            .setPublishedObject(new Object[] {message}));
                } catch (IllegalArgumentException e) {
                    errorHandler.handlePublicationError(new PublicationError()
                                                            .setMessage("Error during invocation of message handler. " +
                                                                        "Wrong arguments passed to method. Was: " + message.getClass()
                                                                        + "Expected: " + handler.getParameterTypes()[0])
                                                            .setCause(e)
                                                            .setHandler(handler)
                                                            .setListener(listener)
                                                            .setPublishedObject(new Object[] {message}));
                } catch (InvocationTargetException e) {
                    errorHandler.handlePublicationError(new PublicationError()
                                                            .setMessage("Error during invocation of message handler. " +
                                                                        "Message handler threw exception")
                                                            .setCause(e)
                                                            .setHandler(handler)
                                                            .setListener(listener)
                                                            .setPublishedObject(new Object[] {message}));

                } catch (Throwable e) {
                    errorHandler.handlePublicationError(new PublicationError()
                                                            .setMessage("Error during invocation of message handler. " +
                                                                        "The handler code threw an exception")
                                                            .setCause(e)
                                                            .setHandler(handler)
                                                            .setListener(listener)
                                                            .setPublishedObject(new Object[] {message}));
                }
            }
        }
    }

    public void publishToSubscription(ErrorHandlingSupport errorHandler, Object message1, Object message2) {
        if (this.listeners.size() > 0) {

            /**
             * Delivers the given message to the given set of listeners.
             * Delivery may be delayed, aborted or restricted in various ways, depending
             * on the configuration of the dispatcher
             *
             * @param publication The message publication that initiated the dispatch
             * @param message     The message that should be delivered to the listeners
             * @param listeners   The listeners that should receive the message
             */
            Method handler = this.handlerMetadata.getHandler();

            for (Object listener : this.listeners) {
                try {
                    this.invocation.invoke(listener, handler, message1, message2);
                } catch (IllegalAccessException e) {
                    errorHandler.handlePublicationError(new PublicationError()
                                                            .setMessage("Error during invocation of message handler. " +
                                                                        "The class or method is not accessible")
                                                            .setCause(e)
                                                            .setHandler(handler)
                                                            .setListener(listener)
                                                            .setPublishedObject(new Object[] {message1, message2}));
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
                                                            .setHandler(handler)
                                                            .setListener(listener)
                                                            .setPublishedObject(new Object[] {message1, message2}));
                } catch (InvocationTargetException e) {
                    errorHandler.handlePublicationError(new PublicationError()
                                                            .setMessage("Error during invocation of message handler. " +
                                                                        "Message handler threw exception")
                                                            .setCause(e)
                                                            .setHandler(handler)
                                                            .setListener(listener)
                                                            .setPublishedObject(new Object[] {message1, message2}));

                } catch (Throwable e) {
                    errorHandler.handlePublicationError(new PublicationError()
                                                            .setMessage("Error during invocation of message handler. " +
                                                                        "The handler code threw an exception")
                                                            .setCause(e)
                                                            .setHandler(handler)
                                                            .setListener(listener)
                                                            .setPublishedObject(new Object[] {message1, message2}));
                }
            }
        }
    }

    public void publishToSubscription(ErrorHandlingSupport errorHandler, Object message1, Object message2, Object message3) {
        if (this.listeners.size() > 0) {

            /**
             * Delivers the given message to the given set of listeners.
             * Delivery may be delayed, aborted or restricted in various ways, depending
             * on the configuration of the dispatcher
             *
             * @param publication The message publication that initiated the dispatch
             * @param message     The message that should be delivered to the listeners
             * @param listeners   The listeners that should receive the message
             */
            Method handler = this.handlerMetadata.getHandler();

            for (Object listener : this.listeners) {
                try {
                    this.invocation.invoke(listener, handler, message1, message2, message3);
                } catch (IllegalAccessException e) {
                    errorHandler.handlePublicationError(new PublicationError()
                                    .setMessage("Error during invocation of message handler. " +
                                                "The class or method is not accessible")
                                    .setCause(e)
                                    .setHandler(handler)
                                    .setListener(listener)
                                    .setPublishedObject(new Object[] {message1, message2, message3}));
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
                                    .setHandler(handler)
                                    .setListener(listener)
                                    .setPublishedObject(new Object[] {message1, message2, message3}));
                } catch (InvocationTargetException e) {
                    errorHandler.handlePublicationError(new PublicationError()
                                    .setMessage("Error during invocation of message handler. " +
                                                "Message handler threw exception")
                                    .setCause(e)
                                    .setHandler(handler)
                                    .setListener(listener)
                                    .setPublishedObject(new Object[] {message1, message2, message3}));

                } catch (Throwable e) {
                    errorHandler.handlePublicationError(new PublicationError()
                                    .setMessage("Error during invocation of message handler. " +
                                                "The handler code threw an exception")
                                    .setCause(e)
                                    .setHandler(handler)
                                    .setListener(listener)
                                    .setPublishedObject(new Object[] {message1, message2, message3}));
                }
            }
        }
    }

    public void publishToSubscription(ErrorHandlingSupport errorHandler, Object... messages) {
        if (this.listeners.size() > 0) {

            /**
             * Delivers the given message to the given set of listeners.
             * Delivery may be delayed, aborted or restricted in various ways, depending
             * on the configuration of the dispatcher
             *
             * @param publication The message publication that initiated the dispatch
             * @param message     The message that should be delivered to the listeners
             * @param listeners   The listeners that should receive the message
             */
            Method handler = this.handlerMetadata.getHandler();

            for (Object listener : this.listeners) {
                try {
                    this.invocation.invoke(listener, handler, messages);
                } catch (IllegalAccessException e) {
                    errorHandler.handlePublicationError(new PublicationError()
                                    .setMessage("Error during invocation of message handler. " +
                                                    "The class or method is not accessible")
                                    .setCause(e)
                                    .setHandler(handler)
                                    .setListener(listener)
                                    .setPublishedObject(messages));
                } catch (IllegalArgumentException e) {
                    errorHandler.handlePublicationError(new PublicationError()
                                    .setMessage("Error during invocation of message handler. " +
                                                "Wrong arguments passed to method. Was: " + Arrays.deepToString(messages)
                                                + "Expected: " + Arrays.deepToString(handler.getParameterTypes()))
                                    .setCause(e)
                                    .setHandler(handler)
                                    .setListener(listener)
                                    .setPublishedObject(messages));
                } catch (InvocationTargetException e) {
                    errorHandler.handlePublicationError(new PublicationError()
                                    .setMessage("Error during invocation of message handler. " +
                                                "Message handler threw exception")
                                    .setCause(e)
                                    .setHandler(handler)
                                    .setListener(listener)
                                    .setPublishedObject(messages));

                } catch (Throwable e) {
                    errorHandler.handlePublicationError(new PublicationError()
                                    .setMessage("Error during invocation of message handler. " +
                                                "The handler code threw an exception")
                                    .setCause(e)
                                    .setHandler(handler)
                                    .setListener(listener)
                                    .setPublishedObject(messages));
                }
            }
        }
    }
}
