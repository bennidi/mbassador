package net.engio.mbassy.dispatch;

import net.engio.mbassy.bus.IMessagePublication;
import net.engio.mbassy.bus.MessagePublication;
import net.engio.mbassy.subscription.ISubscriptionContextAware;

/**
 * A message dispatcher provides the functionality to deliver a single message
 * to a set of listeners. A message dispatcher uses a message context to access
 * all information necessary for the message delivery.
 * <p/>
 * The delivery of a single message to a single listener is responsibility of the
 * handler invocation object associated with the dispatcher.
 * <p/>
 * Implementations if IMessageDispatcher are partially designed using decorator pattern
 * such that it is possible to compose different message dispatchers into dispatcher chains
 * to achieve more complex dispatch logic.
 *
 * @author bennidi
 *         Date: 11/23/12
 */
public interface IMessageDispatcher extends ISubscriptionContextAware {

    /**
     * Delivers the given message to the given set of listeners.
     * Delivery may be delayed, aborted or restricted in various ways, depending
     * on the configuration of the dispatcher
     *
     * @param publication The message publication that initiated the dispatch
     * @param message     The message that should be delivered to the listeners
     * @param listeners   The listeners that should receive the message
     */
    void dispatch(MessagePublication publication, Object message, Iterable listeners);

    /**
     * Get the handler invocation that will be used to deliver the
     * message to each listener.
     *
     * @return the handler invocation that will be used to deliver the
     *         message to each listener
     */
    IHandlerInvocation getInvocation();
}
