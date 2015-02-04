package net.engio.mbassy.bus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.engio.mbassy.bus.common.PubSubSupport;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;
import net.engio.mbassy.subscription.Subscription;
import net.engio.mbassy.subscription.SubscriptionManager;

/**
 * The base class for all message bus implementations.
 *
 * @param <T>
 */
public abstract class AbstractPubSubSupport<T> implements PubSubSupport<T> {


    // this handler will receive all errors that occur during message dispatch or message handling
    private final List<IPublicationErrorHandler> errorHandlers = new ArrayList<IPublicationErrorHandler>();

    private final SubscriptionManager subscriptionManager;


    public AbstractPubSubSupport(IBusConfiguration configuration) {
        // configure the pub sub feature
        Feature.SyncPubSub pubSubFeature = configuration.getFeature(Feature.SyncPubSub.class);
        this.subscriptionManager = new SubscriptionManager(pubSubFeature.getMetadataReader(), getRegisteredErrorHandlers());
    }

    public Collection<IPublicationErrorHandler> getRegisteredErrorHandlers() {
        return Collections.unmodifiableCollection(this.errorHandlers);
    }

    @Override
    public boolean unsubscribe(Object listener) {
        return this.subscriptionManager.unsubscribe(listener);
    }


    @Override
    public void subscribe(Object listener) {
        this.subscriptionManager.subscribe(listener);
    }


    public final void addErrorHandler(IPublicationErrorHandler handler) {
        synchronized (this){
            this.errorHandlers.add(handler);
        }
    }

    protected void publishMessage(T message) {
        Class<? extends Object> class1 = message.getClass();
        Collection<Subscription> subscriptions = getSubscriptionsByMessageType(class1);

        if (subscriptions == null || subscriptions.isEmpty()) {
            // Dead Event
            subscriptions = getSubscriptionsByMessageType(DeadMessage.class);
            DeadMessage deadMessage = new DeadMessage(message);

            for (Subscription sub : subscriptions) {
                sub.publishToSubscription(deadMessage);
            }
        } else {
            boolean delivered = false;
            boolean success = false;
            for (Subscription sub : subscriptions) {
                delivered = sub.publishToSubscription(message);
                if (delivered) {
                    success = true;
                }
            }

            // if the message did not have any listener/handler accept it
            if (!success) {
                if (!isDeadEvent(message)) {
                    // Dead Event
                    subscriptions = getSubscriptionsByMessageType(DeadMessage.class);
                    DeadMessage deadMessage = new DeadMessage(message);

                    for (Subscription sub : subscriptions) {
                        sub.publishToSubscription(deadMessage);
                    }
                }
            }
        }
    }

    private final boolean isDeadEvent(Object message) {
        return DeadMessage.class.equals(message.getClass());
    }


    // obtain the set of subscriptions for the given message type
    // Note: never returns null!
    protected Collection<Subscription> getSubscriptionsByMessageType(Class<?> messageType) {
        return this.subscriptionManager.getSubscriptionsByMessageType(messageType);
    }


    public void handlePublicationError(PublicationError error) {
        for (IPublicationErrorHandler errorHandler : this.errorHandlers) {
            errorHandler.handleError(error);
        }
    }

}
