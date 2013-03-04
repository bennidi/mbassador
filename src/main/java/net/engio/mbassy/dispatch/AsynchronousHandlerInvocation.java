package net.engio.mbassy.dispatch;

import net.engio.mbassy.subscription.AbstractSubscriptionContextAware;

/**
 * This invocation will schedule the wrapped (decorated) invocation to be executed asynchronously
 *
 * @author bennidi
 *         Date: 11/23/12
 */
public class AsynchronousHandlerInvocation extends AbstractSubscriptionContextAware implements IHandlerInvocation {

    private IHandlerInvocation delegate;

    public AsynchronousHandlerInvocation(IHandlerInvocation delegate) {
        super(delegate.getContext());
        this.delegate = delegate;
    }

    @Override
    public void invoke(final Object listener, final Object message) {
        getContext().getOwningBus().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                delegate.invoke(listener, message);
            }
        });
    }
}
