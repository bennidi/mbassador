package net.engio.mbassy.subscription;

/**
 * The base implementation for subscription context aware objects (mightily obvious :)
 *
 * @author bennidi
 *         Date: 3/1/13
 */
public class AbstractSubscriptionContextAware implements ISubscriptionContextAware {

    private final SubscriptionContext context;

    public AbstractSubscriptionContextAware(SubscriptionContext context) {
        this.context = context;
    }

    public final SubscriptionContext getContext() {
        return context;
    }

}
