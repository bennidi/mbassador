package net.engio.mbassy.subscription;

/**
 * This interface marks components that have access to the subscription context.
 *
 * @author bennidi
 *         Date: 3/1/13
 */
public interface ISubscriptionContextAware{

    /**
     * Get the subscription context associated with this object
     *
     * @return the subscription context associated with this object
     */
    SubscriptionContext getContext();
}
