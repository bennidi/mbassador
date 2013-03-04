package net.engio.mbassy.dispatch;

/**
 * A handler invocation encapsulates the logic that is used to invoke a single
 * message handler to process a given message.
 * A handler invocation might come in different flavours and can be composed
 * of various independent invocations be means of delegation (decorator pattern)
 *
 * @author bennidi
 *         Date: 11/23/12
 */
public interface IHandlerInvocation extends ISubscriptionContextAware {

    /**
     * Invoke the message delivery logic of this handler
     *
     * @param listener The listener that will receive the message
     * @param message  The message to be delivered to the listener
     */
    public void invoke(final Object listener, final Object message);


}
