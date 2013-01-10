package net.engio.mbassy.dispatch;

import java.lang.reflect.Method;

/**
 * A handler invocation encapsulates the logic that is used to invoke a single
 * message handler to process a given message.
 * A handler invocation might come in different flavours and can be composed
 * of various independent invocations be means of delegation (decorator pattern)
 *
 * @author bennidi
 *         Date: 11/23/12
 */
public interface IHandlerInvocation {

    /**
     * Invoke the message delivery logic of this handler invocation
     *
     * @param handler The method that represents the actual message handler logic of the listener
     * @param listener The listener that will receive the message
     * @param message  The message to be delivered to the listener
     */
    public void invoke(final Method handler, final Object listener, final Object message);

    /**
     * Get the messaging context associated with this invocation
     * @return
     */
    public MessagingContext getContext();

}
