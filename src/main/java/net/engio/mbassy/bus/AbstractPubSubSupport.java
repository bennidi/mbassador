package net.engio.mbassy.bus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.engio.mbassy.bus.common.DeadMessage;
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

    private final MessagePublication.Factory publicationFactory;

    private final SubscriptionManager subscriptionManager;


    public AbstractPubSubSupport(IBusConfiguration configuration) {
        // configure the pub sub feature
        Feature.SyncPubSub pubSubFeature = configuration.getFeature(Feature.SyncPubSub.class);
        this.subscriptionManager = new SubscriptionManager(pubSubFeature.getMetadataReader(), getRegisteredErrorHandlers());
        this.publicationFactory = pubSubFeature.getPublicationFactory();
    }

    protected MessagePublication.Factory getPublicationFactory() {
        return this.publicationFactory;
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

    protected IMessagePublication createMessagePublication(T message) {
        Class<? extends Object> class1 = message.getClass();
        Collection<Subscription> subscriptions = getSubscriptionsByMessageType(class1);

        if ((subscriptions == null || subscriptions.isEmpty()) && !class1.equals(DeadMessage.class)) {
            // Dead Event
            subscriptions = getSubscriptionsByMessageType(DeadMessage.class);
            return getPublicationFactory().createPublication(this, subscriptions, new DeadMessage(message));
        } else {
            return getPublicationFactory().createPublication(this, subscriptions, message);
        }
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
