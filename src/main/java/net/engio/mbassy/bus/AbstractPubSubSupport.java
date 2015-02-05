package net.engio.mbassy.bus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import net.engio.mbassy.PubSubSupport;
import net.engio.mbassy.bus.error.ErrorHandlingSupport;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;
import net.engio.mbassy.subscription.Subscription;
import net.engio.mbassy.subscription.SubscriptionManager;

/**
 * The base class for all message bus implementations.
 */
public abstract class AbstractPubSubSupport implements PubSubSupport, ErrorHandlingSupport {

    // error handling is first-class functionality
    // this handler will receive all errors that occur during message dispatch or message handling
    private final List<IPublicationErrorHandler> errorHandlers = new ArrayList<IPublicationErrorHandler>();

    private final SubscriptionManager subscriptionManager;


    public AbstractPubSubSupport() {
        this.subscriptionManager = new SubscriptionManager();
    }

    @Override
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


    @Override
    public final void addErrorHandler(IPublicationErrorHandler handler) {
        synchronized (this.errorHandlers) {
            this.errorHandlers.add(handler);
        }
    }

    public void publishMessage(Object message) {
        Class<? extends Object> messageClass = message.getClass();

        // TODO: convert this to have N number of message types
        Collection<Subscription> subscriptions = getSubscriptionsByMessageType(messageClass);

        if (subscriptions == null || subscriptions.isEmpty()) {
            // Dead Event
            subscriptions = getSubscriptionsByMessageType(DeadMessage.class);
            DeadMessage deadMessage = new DeadMessage(message);

            for (Subscription sub : subscriptions) {
                sub.publishToSubscription(this, deadMessage);
            }
        } else {
            boolean delivered = false;
            boolean success = false;
            for (Subscription sub : subscriptions) {
                delivered = sub.publishToSubscription(this, message);
                if (delivered) {
                    success = true;
                }
            }

            // if the message did not have any listener/handler accept it
            if (!success) {
                if (!DeadMessage.class.equals(messageClass.getClass())) {
                    // Dead Event
                    subscriptions = getSubscriptionsByMessageType(DeadMessage.class);
                    DeadMessage deadMessage = new DeadMessage(message);

                    for (Subscription sub : subscriptions) {
                        sub.publishToSubscription(this, deadMessage);
                    }
                }
            }
        }
    }


    // TODO: convert this to have N number of message types

    // obtain the set of subscriptions for the given message type
    // Note: never returns null!
    protected Collection<Subscription> getSubscriptionsByMessageType(Class<?> messageType) {
        return this.subscriptionManager.getSubscriptionsByMessageType(messageType);
    }


    @Override
    public final void handlePublicationError(PublicationError error) {
        for (IPublicationErrorHandler errorHandler : this.errorHandlers) {
            errorHandler.handleError(error);
        }
    }
}
