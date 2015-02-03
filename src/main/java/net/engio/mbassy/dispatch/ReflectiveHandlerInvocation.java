package net.engio.mbassy.dispatch;

import java.lang.reflect.Method;

/**
 * Uses reflection to invoke a message handler for a given message.
 *
 * @author bennidi
 *         Date: 11/23/12
 */
public class ReflectiveHandlerInvocation implements IHandlerInvocation {

    public ReflectiveHandlerInvocation() {
        super();
    }

    @Override
    public void invoke(final Object listener, final Object message, Method handler) throws Throwable {
        handler.invoke(listener, message);
    }
}
