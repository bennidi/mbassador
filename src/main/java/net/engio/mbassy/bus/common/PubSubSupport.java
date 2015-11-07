package net.engio.mbassy.bus.common;

import net.engio.mbassy.bus.IMessagePublication;

/**
 * This interface defines the very basic message publication semantics according to the publish subscribe pattern.
 * Listeners can be subscribed and unsubscribed using the corresponding methods. When a listener is subscribed its
 * handlers will be registered and start to receive matching message publications.
 *
 */
public interface PubSubSupport<T> extends RuntimeProvider{

    /**
     * Subscribe all handlers of the given listener. Any listener is only subscribed once
     * -> subsequent subscriptions of an already subscribed listener will be silently ignored
     *
     * @param listener
     */
    void subscribe(Object listener);

    /**
     * Immediately remove all registered message handlers (if any) of the given listener. When this call returns all handlers
     * have effectively been removed and will not receive any messages (provided that running publications (iterators) in other threads
     * have not yet obtained a reference to the listener)
     * <p/>
     * A call to this method passing any object that is not subscribed will not have any effect and is silently ignored.
     *
     * @param listener
     * @return true, if the listener was found and successfully removed
     *         false otherwise
     */
    boolean unsubscribe(Object listener);


    /**
     * Synchronously publish a message to all registered listeners. This includes listeners defined for super types of the
     * given message type, provided they are not configured to reject valid subtype. The call returns when all matching handlers
     * of all registered listeners have been notified (invoked) of the message.
     *
     * @param message
     */
    IMessagePublication publish(T message);
}
