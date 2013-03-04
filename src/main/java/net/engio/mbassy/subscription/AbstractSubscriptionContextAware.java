package net.engio.mbassy.subscription;

import net.engio.mbassy.bus.IMessageBus;
import net.engio.mbassy.dispatch.ISubscriptionContextAware;

/**
 * The base implementation for subscription context aware objects (mightily obvious :)
 *
 * @author bennidi
 *         Date: 3/1/13
 */
public class AbstractSubscriptionContextAware implements ISubscriptionContextAware{

    private SubscriptionContext context;

    public AbstractSubscriptionContextAware(SubscriptionContext context) {
        this.context = context;
    }

    public SubscriptionContext getContext() {
        return context;
    }

    @Override
    public IMessageBus getBus() {
        return context.getOwningBus();
    }
}
