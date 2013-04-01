package net.engio.mbassy.dispatch;

import net.engio.mbassy.bus.ISyncMessageBus;

/**
 * A handler invocation encapsulates the logic that is used to invoke a single
 * message handler to process a given message.
 * A handler invocation might come in different flavours and can be composed
 * of various independent invocations be means of delegation (decorator pattern)
 *
 * @author bennidi
 *         Date: 11/23/12
 */
public interface IHandlerInvocation<Listener, Message, Bus extends ISyncMessageBus> extends ISubscriptionContextAware<Bus> {

    /**
     * Invoke the message delivery logic of this handler
     *
     * @param listener The listener that will receive the message
     * @param message  The message to be delivered to the listener
     */
    void invoke(Listener listener, Message message);
}
