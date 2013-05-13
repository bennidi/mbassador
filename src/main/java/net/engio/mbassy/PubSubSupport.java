package net.engio.mbassy;

/**
 * This interface defines the very basic message publication semantics according to the publish subscribe pattern.
 *
 * Listeners can be subscribed and unsubscribed using the corresponding methods. When a listener is subscribed
 *
 *
 */
public interface PubSubSupport<T> {

    /**
     * Subscribe all handler of the given listener. Any listener may only be subscribed once
     * -> subsequent subscriptions of an already subscribed listener will be silently ignored)
     *
     * @param listener
     */
    void subscribe(Object listener);

    /**
     * Immediately remove all registered message handlers (if any) of the given listener. When this call returns all handlers
     * have effectively been removed and will not receive any message publications (including asynchronously scheduled
     * publications that have been published when the message listener was still subscribed).
     * <p/>
     * A call to this method passing any object that is not subscribed will not have any effect and is silently ignored.
     *
     * @param listener
     * @return true, if the listener was found and successfully removed
     *         false otherwise
     */
    boolean unsubscribe(Object listener);


    /**
     * Synchronously publish a message to all registered listeners (this includes listeners defined for super types)
     * The call blocks until every messageHandler has processed the message.
     *
     * @param message
     */
    void publish(T message);
}
