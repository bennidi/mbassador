package net.engio.mbassy.dispatch;

import net.engio.mbassy.bus.MessagePublication;
import net.engio.mbassy.subscription.AbstractSubscriptionContextAware;

/**
 * Synchronizes message handler invocations for all handlers that specify @Synchronized
 *
 * @author bennidi
 *         Date: 3/31/13
 */
public class SynchronizedHandlerInvocation extends AbstractSubscriptionContextAware implements IHandlerInvocation<Object,Object>  {

    private IHandlerInvocation delegate;

    public SynchronizedHandlerInvocation(IHandlerInvocation delegate) {
        super(delegate.getContext());
        this.delegate = delegate;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invoke(final Object listener, final Object message, MessagePublication publication){
        synchronized (listener){
            delegate.invoke(listener, message, publication);
        }
    }

}
