package net.engio.mbassy.dispatch;

import com.esotericsoftware.reflectasm.MethodAccess;

/**
 * Synchronizes message handler invocations for all handlers that specify @Synchronized
 *
 * @author bennidi
 *         Date: 3/31/13
 * @author dorkbox, llc
 *         Date: 2/2/15
 */
public class SynchronizedHandlerInvocation implements IHandlerInvocation {

    private IHandlerInvocation delegate;

    public SynchronizedHandlerInvocation(IHandlerInvocation delegate) {
        this.delegate = delegate;
    }

    @Override
    public void invoke(final Object listener, final MethodAccess handler, final int methodIndex, final Object message) throws Throwable {
        synchronized (listener) {
            this.delegate.invoke(listener, handler, methodIndex, message);
        }
    }

    @Override
    public void invoke(final Object listener, MethodAccess handler, int methodIndex, final Object message1, final Object message2) throws Throwable {
        synchronized (listener) {
            this.delegate.invoke(listener, handler, methodIndex, message1, message2);
        }
    }

    @Override
    public void invoke(final Object listener, MethodAccess handler, int methodIndex, final Object message1, final Object message2, final Object message3) throws Throwable {
        synchronized (listener) {
            this.delegate.invoke(listener, handler, methodIndex, message1, message2, message3);
        }
    }

    @Override
    public void invoke(final Object listener, MethodAccess handler, int methodIndex, final Object... messages) throws Throwable {
        synchronized (listener) {
            this.delegate.invoke(listener, handler, methodIndex, messages);
        }
    }
}
