package org.mbassy.dispatch;

import org.mbassy.common.ConcurrentSet;

/**
 * A message dispatcher provides the functionality to deliver a single message
 * to a set of listeners. A message dispatcher uses a message context to access
 * all information necessary for the message delivery.
 *
 * The delivery of a single message to a single listener is responsibility of the
 * handler invocation object associated with the dispatcher.
 *
 * Implementations if IMessageDispatcher are partially designed using decorator pattern
 * such that it is possible to compose different message dispatchers to achieve more complex
 * dispatch logic.
 *
 * @author bennidi
 *         Date: 11/23/12
 */
public interface IMessageDispatcher {

    /**
     * Delivers the given message to the given set of listeners.
     * Delivery may be delayed, aborted or restricted in various ways, depending
     * on the configuration of the dispatcher
     *
     * @param message The message that should be delivered to the listeners
     * @param listeners The listeners that should receive the message
     */
    public void dispatch(Object message, ConcurrentSet listeners);

    /**
     * Get the messaging context associated with this dispatcher
     *
     * @return
     */
    public MessagingContext getContext();

    /**
     * Get the handler invocation that will be used to deliver the message to each
     * listener
     * @return
     */
    public IHandlerInvocation getInvocation();
}
