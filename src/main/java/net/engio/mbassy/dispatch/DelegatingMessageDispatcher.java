package net.engio.mbassy.dispatch;

import net.engio.mbassy.subscription.AbstractSubscriptionContextAware;

/**
 * A delegating dispatcher wraps additional logic around a given delegate. Essentially its
 * an implementation of the decorator pattern.
 *
 * @author bennidi
 *         Date: 3/1/13
 */
public abstract class DelegatingMessageDispatcher extends AbstractSubscriptionContextAware implements IMessageDispatcher {

    private final IMessageDispatcher delegate;


    public DelegatingMessageDispatcher(IMessageDispatcher delegate) {
        super(delegate.getContext());
        this.delegate = delegate;
    }

    protected IMessageDispatcher getDelegate() {
        return delegate;
    }

    @Override
    public IHandlerInvocation getInvocation() {
        return delegate.getInvocation();
    }
}
