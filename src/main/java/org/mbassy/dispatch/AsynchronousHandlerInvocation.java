package org.mbassy.dispatch;

import org.mbassy.IPublicationErrorHandler;
import org.mbassy.PublicationError;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * This invocation will schedule the wrapped (decorated) invocation to be executed asynchronously
 *
 * @author bennidi
 *         Date: 11/23/12
 */
public class AsynchronousHandlerInvocation implements IHandlerInvocation {

    private IHandlerInvocation delegate;

    public AsynchronousHandlerInvocation(IHandlerInvocation delegate) {
        super();
        this.delegate = delegate;
    }

    @Override
    public void invoke(final Method handler, final Object listener, final Object message) {
        getContext().getOwningBus().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                delegate.invoke(handler, listener, message);
            }
        });
    }

    @Override
    public MessagingContext getContext() {
        return delegate.getContext();
    }
}
