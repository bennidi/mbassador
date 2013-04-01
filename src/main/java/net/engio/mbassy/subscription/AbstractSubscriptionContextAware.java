package net.engio.mbassy.subscription;

import net.engio.mbassy.bus.ISyncMessageBus;
import net.engio.mbassy.dispatch.ISubscriptionContextAware;

/**
 * The base implementation for subscription context aware objects (mightily obvious :)
 *
 * @author bennidi
 *         Date: 3/1/13
 */
public class AbstractSubscriptionContextAware<Bus extends ISyncMessageBus> implements ISubscriptionContextAware<Bus> {

    private final SubscriptionContext<Bus> context;

    public AbstractSubscriptionContextAware(SubscriptionContext<Bus> context) {
        this.context = context;
    }

    public SubscriptionContext<Bus> getContext() {
        return context;
    }

    @Override
    public Bus getBus() {
        return context.getOwningBus();
    }
}
