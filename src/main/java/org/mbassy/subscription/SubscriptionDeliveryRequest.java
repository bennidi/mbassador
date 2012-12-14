package org.mbassy.subscription;

import java.util.Collection;

/**
 * @author bennidi
 * Date: 11/16/12
 */
public class SubscriptionDeliveryRequest<T> {

    private Collection<Subscription> subscriptions;

    private T message;

    public SubscriptionDeliveryRequest(Collection<Subscription> subscriptions, T message) {
        this.subscriptions = subscriptions;
        this.message = message;
    }

    public boolean add(Subscription subscription) {
        return subscriptions.add(subscription);
    }

    public void execute(){
        for(Subscription sub : subscriptions)
            sub.publish(message);
    }
}
