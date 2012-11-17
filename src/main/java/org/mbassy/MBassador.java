package org.mbassy;

import org.mbassy.subscription.*;

import java.util.*;
import java.util.concurrent.*;


public class MBassador<T> extends AbstractMessageBus<T, SimplePostCommand<T>>{

    public MBassador(){
        this(2);
    }

    public MBassador(int dispatcherThreadCount){
        super(dispatcherThreadCount);
    }

    public MBassador(int dispatcherThreadCount, ExecutorService executor){
        super(dispatcherThreadCount,executor);
    }

    @Override
    protected SubscriptionFactory getSubscriptionFactory() {
        return new SubscriptionFactory(this);
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
    public SimplePostCommand post(T message) {
        return new SimplePostCommand(this, message);
    }

}
