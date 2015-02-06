package net.engio.mbassy.dispatch;

import java.lang.reflect.Method;

/**
 * Synchronizes message handler invocations for all handlers that specify @Synchronized
 *
 * @author bennidi
 *         Date: 3/31/13
 */
public class SynchronizedHandlerInvocation implements IHandlerInvocation {

    private IHandlerInvocation delegate;

    public SynchronizedHandlerInvocation(IHandlerInvocation delegate) {
        this.delegate = delegate;
    }

    @Override
    public void invoke(final Object listener, Method handler, final Object message) throws Throwable {
        synchronized (listener) {
            this.delegate.invoke(listener, handler, message);
        }
    }

    @Override
    public void invoke(final Object listener, Method handler, final Object message1, final Object message2) throws Throwable {
        synchronized (listener) {
            this.delegate.invoke(listener, handler, message1, message2);
        }
    }

    @Override
    public void invoke(final Object listener, Method handler, final Object message1, final Object message2, final Object message3) throws Throwable {
        synchronized (listener) {
            this.delegate.invoke(listener, handler, message1, message2, message3);
        }
    }

    @Override
    public void invoke(final Object listener, Method handler, final Object... messages) throws Throwable {
        synchronized (listener) {
            this.delegate.invoke(listener, handler, messages);
        }
    }
}
