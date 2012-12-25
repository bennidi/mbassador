package org.mbassy;

import java.util.Collection;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
 *
 * A message bus offers facilities for publishing messages to registered listeners. Messages can be dispatched
 * synchronously or asynchronously and may be of any type that is a valid sub type of the type parameter T.
 * The dispatch mechanism can by controlled for per message handler and message publication.
 * A message publication is the publication of any message using one of the bus' publication methods.
 * <p/>
 * Each message publication is isolated from all other running publications such that it does not interfere with them.
 * Hence, the bus expects message handlers to be stateless as it may invoke them concurrently if multiple
 * messages get published asynchronously.
 * <p/>
 * A listener is any object that defines at least one message handler and that has been subscribed to at least
 * one message bus. A message handler can be any method that accepts exactly one parameter (the message) and is marked
 * as a message handler using the @Listener annotation.
 * <p/>
 * The bus uses weak references to all listeners such that registered listeners do not need to
 * be explicitly unregistered to be eligible for garbage collection. Dead (garbage collected) listeners are
 * removed on-the-fly as messages get dispatched.
 * <p/>
 * Generally message handlers will be invoked in inverse sequence of subscription but any
 * client using this bus should not rely on this assumption. The basic contract of the bus is that it will deliver
 * a specific message exactly once to each of the subscribed message handlers.
 * <p/>
 * Messages are dispatched to all listeners that accept the type or supertype of the dispatched message. Additionally
 * a message handler may define filters to narrow the set of messages that it accepts.
 * <p/>
 * Subscribed message handlers are available to all pending message publications that have not yet started processing.
 * Any message listener may only be subscribed once -> subsequent subscriptions of an already subscribed message listener
 * will be silently ignored)
 * <p/>
 * Removing a listener (unsubscribing) means removing all subscribed message handlers of that listener. This remove operation
 * immediately takes effect and on all running dispatch processes -> A removed listener (a listener
 * is considered removed after the remove(Object) call returned) will under no circumstances receive any message publications.
 * Any running message publication that has not yet delivered the message to the removed listener will not see the listener
 * after the remove operation completed.
 *
 * NOTE: Generic type parameters of messages will not be taken into account, e.g. a List<Long> will
 * get dispatched to all message handlers that take an instance of List as their parameter
 *
 * @Author bennidi
 * Date: 2/8/12
 */
public interface IMessageBus<T, P extends IMessageBus.IPostCommand> {

    /**
     * Subscribe all listeners of the given message to receive message publications.
     * Any message may only be subscribed once (subsequent subscriptions of an already subscribed
     * message will be silently ignored)
     *
     * @param listener
     */
    public void subscribe(Object listener);


    /**
     * Immediately remove all registered message handlers (if any) of the given listener. When this call returns all handlers
     * have effectively been removed and will not receive any message publications (including asynchronously scheduled
     * publications that have been published when the message listener was still subscribed).
     *
     * A call to this method passing null, an already unsubscribed listener or any object that does not define any message
     * handlers will not have any effect and is silently ignored.
     *
     * @param listener
     * @return  true, if the listener was found and successfully removed
     *          false otherwise
     */
    public boolean unsubscribe(Object listener);

    /**
     *
     * @param message
     * @return
     */
    public P post(T message);

    /**
     * Publication errors may occur at various points of time during message delivery. A handler may throw an exception,
     * may not be accessible due to security constraints or is not annotated properly.
     * In any of all possible cases a publication error is created and passed to each of the registered error handlers.
     * A call to this method will add the given error handler to the chain
     *
     * @param errorHandler
     */
    public void addErrorHandler(IPublicationErrorHandler errorHandler);

    /**
     * Returns an immutable collection containing all the registered error handlers
     *
     * @return
     */
    public Collection<IPublicationErrorHandler> getRegisteredErrorHandlers();


    /**
     * Get the executor service that is used to asynchronous message publication.
     * The executor is passed to the message bus at creation time.
     *
     * @return
     */
    public Executor getExecutor();

    /**
     * Check whether any asynchronous message publications are pending for being processed
     *
     * @return
     */
    public boolean hasPendingMessages();


    /**
     * A post command is used as an intermediate object created by a call to the message bus' post method.
     * It encapsulates the functionality provided by the message bus that created the command.
     * Subclasses may extend this interface and add functionality, e.g. different dispatch schemes.
     *
     */
    public static interface IPostCommand<T>{

        /**
         * Execute the message publication immediately. This call blocks until every matching message handler
         * has been invoked.
         */
        public void now();

        /**
         * Execute the message publication asynchronously. The behaviour of this method depends on the
         * configured queuing strategy:
         *
         * If an unbound queuing strategy is used the call returns immediately.
         * If a bounded queue is used the call might block until the message can be placed in the queue.
         *
         * @return A message publication that can be used to access information about the state of
         */
        public MessagePublication<T> asynchronously();


        /**
         * Execute the message publication asynchronously. The behaviour of this method depends on the
         * configured queuing strategy:
         *
         * If an unbound queuing strategy is used the call returns immediately.
         * If a bounded queue is used the call will block until the message can be placed in the queue
         * or the timeout r
         *
         * @return A message publication that wraps up the publication request
         */
        public MessagePublication<T> asynchronously(long timeout, TimeUnit unit);

    }

}
