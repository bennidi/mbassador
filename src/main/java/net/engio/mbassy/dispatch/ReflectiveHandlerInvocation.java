package net.engio.mbassy.dispatch;

import java.lang.reflect.Method;

/**
 * Uses reflection to invoke a message handler for a given message.
 *
 * @author bennidi
 *         Date: 11/23/12
 */
public class ReflectiveHandlerInvocation extends AbstractHandlerInvocation implements IHandlerInvocation {

    public ReflectiveHandlerInvocation(MessagingContext context) {
        super(context);
    }

    @Override
    public void invoke(final Method handler, final Object listener, final Object message) {
        invokeHandler(message, listener, handler);
    }
}
