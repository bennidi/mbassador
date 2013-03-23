package net.engio.mbassy.dispatch;

import net.engio.mbassy.subscription.SubscriptionContext;

/**
 * This interface marks components that have access to the subscription context.
 *
 * @author bennidi
 *         Date: 3/1/13
 */
public interface ISubscriptionContextAware extends IMessageBusAware {

    /**
     * Get the subscription context associated with this object
     *
     * @return the subscription context associated with this object
     */
    SubscriptionContext getContext();
}
