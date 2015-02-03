package net.engio.mbassy.subscription;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

import net.engio.mbassy.bus.IMessagePublication;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
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

    // error handling is first-class functionality
    private final Collection<IPublicationErrorHandler> errorHandlers;

    private final IHandlerInvocation invocation;

    protected final IConcurrentSet<Object> listeners;

    Subscription(MessageHandler handler, Collection<IPublicationErrorHandler> errorHandlers,
                 IHandlerInvocation invocation, IConcurrentSet<Object> listeners) {

        this.handlerMetadata = handler;
        this.errorHandlers = errorHandlers;
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

    /**
     * Check whether this subscription manages a message handler
     * @param messageType
     * @return
     */
    public boolean handlesMessageType(Class<?> messageType) {
        return this.handlerMetadata.handlesMessage(messageType);
    }

    public Class<?>[] getHandledMessageTypes(){
        return this.handlerMetadata.getHandledMessages();
    }

    public void publish(IMessagePublication publication, Object message){
        if (this.listeners.size() > 0) {
            publication.markDelivered();

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
                    this.invocation.invoke(listener, message, handler);
                } catch (IllegalAccessException e) {
                    handlePublicationError(new PublicationError(e, "Error during invocation of message handler. " +
                                    "The class or method is not accessible",
                                    handler, listener, message));
                } catch (IllegalArgumentException e) {
                    handlePublicationError(new PublicationError(e, "Error during invocation of message handler. " +
                                    "Wrong arguments passed to method. Was: " + message.getClass()
                                    + "Expected: " + handler.getParameterTypes()[0],
                                    handler, listener, message));
                } catch (InvocationTargetException e) {
                    handlePublicationError( new PublicationError(e, "Error during invocation of message handler. " +
                                    "Message handler threw exception",
                                    handler, listener, message));
                } catch (Throwable e) {
                    handlePublicationError( new PublicationError(e, "Error during invocation of message handler. " +
                                    "The handler code threw an exception",
                                    handler, listener, message));
                }
            }
        }
    }


    private final void handlePublicationError(PublicationError error) {
        for (IPublicationErrorHandler handler : this.errorHandlers) {
            handler.handleError(error);
        }
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
}
