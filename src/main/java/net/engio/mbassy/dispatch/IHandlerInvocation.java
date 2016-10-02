package net.engio.mbassy.dispatch;

import net.engio.mbassy.bus.MessagePublication;
import net.engio.mbassy.subscription.ISubscriptionContextAware;

/**
 * A handler invocation encapsulates the logic that is used to invoke a single
 * message handler to process a given message.
 *
 * A handler invocation might come in different flavours and can be composed
 * of various independent invocations by means of delegation (-> decorator pattern)
 *
 * If an exception is thrown during handler invocation it is wrapped and propagated
 * as a publication error
 *
 * @author bennidi
 *         Date: 11/23/12
 */
public interface IHandlerInvocation<HANDLER, MESSAGE> extends ISubscriptionContextAware {

    /**
     * Invoke the message delivery logic of this handler
     *
     * @param handler The listener that will receive the message. This can be a reference to a method object
     *                 from the java reflection api or any other wrapper that can be used to invoke the handler
     * @param message  The message to be delivered to the handler. This can be any object compatible with the object
     *                 type that the handler consumes
     */
    void invoke(HANDLER handler, MESSAGE message, MessagePublication publication);
}
