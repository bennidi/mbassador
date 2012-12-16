package org.mbassy;

import java.util.Collection;

import org.mbassy.subscription.Subscription;
import org.mbassy.subscription.SubscriptionDeliveryRequest;


public class MBassador<T> extends AbstractMessageBus<T, SyncAsyncPostCommand<T>>{

    public MBassador(BusConfiguration configuration){
        super(configuration);
    }


    public void publishAsync(T message){
        addAsynchronousDeliveryRequest(new SubscriptionDeliveryRequest<T>(getSubscriptionsByMessageType(message.getClass()), message));
    }


    /**
     * Synchronously publish a message to all registered listeners (this includes listeners defined for super types)
     * The call blocks until every messageHandler has processed the message.
     *
     * @param message
     */
	public void publish(T message){
		try {
			final Collection<Subscription> subscriptions = getSubscriptionsByMessageType(message.getClass());
			if(subscriptions == null){
                return; // TODO: Dead Event?
            }
            for (Subscription subscription : subscriptions){
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
