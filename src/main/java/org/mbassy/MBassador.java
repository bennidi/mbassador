package org.mbassy;

import org.mbassy.subscription.Subscription;

import java.util.Collection;
import java.util.concurrent.TimeUnit;


public class MBassador<T> extends AbstractMessageBus<T, SyncAsyncPostCommand<T>> {

    public MBassador(BusConfiguration configuration) {
        super(configuration);
    }


    public MessagePublication<T> publishAsync(T message) {
        return addAsynchronousDeliveryRequest(MessagePublication.Create(
                getSubscriptionsByMessageType(message.getClass()), message));
    }

    public MessagePublication<T> publishAsync(T message, long timeout, TimeUnit unit) {
        return addAsynchronousDeliveryRequest(MessagePublication.Create(
                getSubscriptionsByMessageType(message.getClass()), message), timeout, unit);
    }


    /**
     * Synchronously publish a message to all registered listeners (this includes listeners defined for super types)
     * The call blocks until every messageHandler has processed the message.
     *
     * @param message
     */
    public void publish(T message) {
        try {
            final Collection<Subscription> subscriptions = getSubscriptionsByMessageType(message.getClass());
            if (subscriptions == null) {
    			// Dead Event
				final Collection<Subscription> deadEventSubscriptions = getSubscriptionsByMessageType(DeadEvent.class);
				
				if (deadEventSubscriptions == null) {
					return; 
				}
				
				for (Subscription subscription : deadEventSubscriptions) {
					subscription.publish(new DeadEvent(message));
				}
            }
            for (Subscription subscription : subscriptions) {
                subscription.publish(message);
            }
        } catch (Throwable e) {
            handlePublicationError(new PublicationError()
                    .setMessage("Error during publication of message")
                    .setCause(e)
                    .setPublishedObject(message));
        }

    }


    @Override
    public SyncAsyncPostCommand<T> post(T message) {
        return new SyncAsyncPostCommand<T>(this, message);
    }

}
