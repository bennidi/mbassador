package net.engio.mbassy;

import net.engio.mbassy.common.DeadEvent;
import net.engio.mbassy.subscription.Subscription;

import java.util.Collection;
import java.util.concurrent.TimeUnit;


public class MBassador<T> extends AbstractMessageBus<T, SyncAsyncPostCommand<T>> {

    public MBassador(BusConfiguration configuration) {
        super(configuration);
    }


    public MessagePublication publishAsync(T message) {
        return addAsynchronousDeliveryRequest(createMessagePublication(message));
    }

    public MessagePublication publishAsync(T message, long timeout, TimeUnit unit) {
        return addAsynchronousDeliveryRequest(createMessagePublication(message), timeout, unit);
    }

    private MessagePublication createMessagePublication(T message) {
        Collection<Subscription> subscriptions = getSubscriptionsByMessageType(message.getClass());
        if (subscriptions == null || subscriptions.isEmpty()) {
            // Dead Event
            subscriptions = getSubscriptionsByMessageType(DeadEvent.class);
            return MessagePublication.Create(subscriptions, new DeadEvent(message));
        }
        else return MessagePublication.Create(subscriptions, message);
    }



    /**
     * Synchronously publish a message to all registered listeners (this includes listeners defined for super types)
     * The call blocks until every messageHandler has processed the message.
     *
     * @param message
     */
    public void publish(T message) {
        try {
            MessagePublication publication = createMessagePublication(message);
            publication.execute();

            /*
            final Collection<Subscription> subscriptions = getSubscriptionsByMessageType(message.getClass());
            if (subscriptions == null || subscriptions.isEmpty()) {
                // publish a DeadEvent since no subscriptions could be found
                final Collection<Subscription> deadEventSubscriptions = getSubscriptionsByMessageType(DeadEvent.class);
                if (deadEventSubscriptions != null && !deadEventSubscriptions.isEmpty()) {
                    for (Subscription subscription : deadEventSubscriptions) {
                        subscription.publish(new DeadEvent(message));
                    }
                }
            }
            else{
                for (Subscription subscription : subscriptions) {
                    subscription.publish(message);
                }
            }*/
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
