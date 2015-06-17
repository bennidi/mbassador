package net.engio.mbassy.bus.common;

import net.engio.mbassy.bus.publication.ISyncAsyncPublicationCommand;

/**
 * A message bus offers facilities for publishing messages to the message handlers of registered listeners.
 * A message publication starts when an object is send to the bus using one of the its publication methods.
 *
 * Messages can be published synchronously or asynchronously and may be of any type that is a valid sub type of the type parameter T.
 * Message handlers can be invoked synchronously or asynchronously depending on their configuration. Thus, there
 * are two notions of synchronicity / asynchronicity. One on the caller side, e.g. the invocation of the message publishing
 * methods. The second on the handler side, e.g. whether the handler is invoked in the same or a different thread.
 *
 * <p/>
 * Each message publication is isolated from all other running publications such that it does not interfere with them.
 * Hence, the bus generally expects message handlers to be stateless as it may invoke them concurrently if multiple
 * messages get published asynchronously. If handlers are stateful and not thread-safe they can be marked to be invoked
 * in a synchronized fashion using @Synchronized annotation
 *
 * <p/>
 * A listener is any object that defines at least one message handler and that has been subscribed to at least
 * one message bus. A message handler can be any method that accepts exactly one parameter (the message) and is marked
 * as a message handler using the @Handler annotation.
 *
 * <p/>
 * By default, the bus uses weak references to all listeners such that registered listeners do not need to
 * be explicitly unregistered to be eligible for garbage collection. Dead (garbage collected) listeners are
 * removed on-the-fly as messages get dispatched. This can be changed using the @Listener annotation.
 *
 * <p/>
 * Generally message handlers will be invoked in inverse sequence of subscription but any
 * client using this bus should not rely on this assumption. The basic contract of the bus is that it will deliver
 * a specific message exactly once to each of the respective message handlers.
 *
 * <p/>
 * Messages are dispatched to all listeners that accept the type or supertype of the dispatched message. Additionally
 * a message handler may define filters to narrow the set of messages that it accepts.
 *
 * <p/>
 * Subscribed message handlers are available to all pending message publications that have not yet started processing.
 * Any message listener may only be subscribed once -> subsequent subscriptions of an already subscribed message listener
 * will be silently ignored)
 *
 * <p/>
 * Removing a listener (unsubscribing) means removing all subscribed message handlers of that listener. This remove operation
 * immediately takes effect and on all running dispatch processes -> A removed listener (a listener
 * is considered removed after the remove(Object) call returned) will under no circumstances receive any message publications.
 * Any running message publication that has not yet delivered the message to the removed listener will not see the listener
 * after the remove operation completed.
 *
 * <p/>
 * NOTE: Generic type parameters of messages will not be taken into account, e.g. a List<Long> will
 * get dispatched to all message handlers that take an instance of List as their parameter
 *
 * @author bennidi
 * Date: 2/8/12
 */
public interface IMessageBus<T, P extends ISyncAsyncPublicationCommand>
        extends GenericMessagePublicationSupport<T, P>{

    /**
     * {@inheritDoc}
     */
    @Override
    P post(T message);

    /**
     * Check whether any asynchronous message publications are pending to be processed
     *
     * @return true if any unfinished message publications are found
     */
    boolean hasPendingMessages();

    /**
     * Shutdown the bus such that it will stop delivering asynchronous messages. Executor service and
     * other internally used threads will be shutdown gracefully. After calling shutdown it is not safe
     * to further use the message bus.
     */
    void shutdown();


}
