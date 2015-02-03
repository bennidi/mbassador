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

    /**
     * {@inheritDoc}
     */
    @Override
    public void invoke(final Object listener, final Object message, Method handler) throws Throwable {
        synchronized (listener){
            this.delegate.invoke(listener, message, handler);
        }
    }

}
