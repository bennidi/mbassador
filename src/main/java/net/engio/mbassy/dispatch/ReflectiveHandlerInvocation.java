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
    public void invoke(final Object listener, Method handler, final Object message) throws Throwable {
        handler.invoke(listener, message);
    }

    @Override
    public void invoke(final Object listener, Method handler, final Object message1, final Object message2) throws Throwable {
        handler.invoke(listener, message1, message2);
    }

    @Override
    public void invoke(final Object listener, Method handler, final Object message1, final Object message2, final Object message3) throws Throwable {
        handler.invoke(listener, message1, message2, message3);
    }

    @Override
    public void invoke(final Object listener, Method handler, final Object... messages) throws Throwable {
        handler.invoke(listener, messages);
    }
}
