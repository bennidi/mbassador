package net.engio.mbassy.bus;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

/**
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
 * as a message handler using the @Handler annotation.
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
 * <p/>
 * NOTE: Generic type parameters of messages will not be taken into account, e.g. a List<Long> will
 * get dispatched to all message handlers that take an instance of List as their parameter
 *
 * @Author bennidi
 * Date: 2/8/12
 */
public interface IMessageBus<T, P extends IMessageBus.IPostCommand> extends ISyncMessageBus<T,P> {

    /**
     * Get the executor service that is used for asynchronous message publications.
     * The executor is passed to the message bus at creation time.
     *
     * @return
     */
    Executor getExecutor();

    /**
     * Check whether any asynchronous message publications are pending for being processed
     *
     * @return
     */
    boolean hasPendingMessages();

    /**
     * Shutdown the bus such that it will stop delivering asynchronous messages. Executor service and
     * other internally used threads will be shutdown gracefully. After calling shutdown it is not safe
     * to further use the message bus.
     */
    void shutdown();

    /**
     * @param message
     * @return
     */
    P post(T message);


    interface IPostCommand extends ISyncPostCommand {

        /**
         * Execute the message publication asynchronously. The behaviour of this method depends on the
         * configured queuing strategy:
         * <p/>
         * If an unbound queuing strategy is used the call returns immediately.
         * If a bounded queue is used the call might block until the message can be placed in the queue.
         *
         * @return A message publication that can be used to access information about the state of
         */
        MessagePublication asynchronously();

        /**
         * Execute the message publication asynchronously. The behaviour of this method depends on the
         * configured queuing strategy:
         * <p/>
         * If an unbound queuing strategy is used the call returns immediately.
         * If a bounded queue is used the call will block until the message can be placed in the queue
         * or the timeout is reached.
         *
         * @return A message publication that wraps up the publication request
         */
        MessagePublication asynchronously(long timeout, TimeUnit unit);
    }

}
