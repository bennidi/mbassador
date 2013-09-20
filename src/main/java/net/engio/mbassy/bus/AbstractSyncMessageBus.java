package net.engio.mbassy.bus;

import net.engio.mbassy.IPublicationErrorHandler;
import net.engio.mbassy.PublicationError;
import net.engio.mbassy.bus.config.ISyncBusConfiguration;
import net.engio.mbassy.bus.publication.IPublicationCommand;
import net.engio.mbassy.common.DeadMessage;
import net.engio.mbassy.subscription.Subscription;
import net.engio.mbassy.subscription.SubscriptionManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The base class for all message bus implementations.
 *
 * @param <T>
 * @param <P>
 */
public abstract class AbstractSyncMessageBus<T, P extends IPublicationCommand> implements ISyncMessageBus<T, P>{


    // this handler will receive all errors that occur during message dispatch or message handling
    private final List<IPublicationErrorHandler> errorHandlers = new ArrayList<IPublicationErrorHandler>();

    private final MessagePublication.Factory publicationFactory;

    private final SubscriptionManager subscriptionManager;

    private final BusRuntime runtime;


    public AbstractSyncMessageBus(ISyncBusConfiguration configuration) {
        this.runtime = new BusRuntime(this);
        this.runtime.add("error.handlers", getRegisteredErrorHandlers());
        this.subscriptionManager = new SubscriptionManager(configuration.getMetadataReader(),
                configuration.getSubscriptionFactory(), runtime);
        this.publicationFactory = configuration.getMessagePublicationFactory();
    }

    protected MessagePublication.Factory getPublicationFactory() {
        return publicationFactory;
    }

    @Override
    public Collection<IPublicationErrorHandler> getRegisteredErrorHandlers() {
        return Collections.unmodifiableCollection(errorHandlers);
    }

    public boolean unsubscribe(Object listener) {
        return subscriptionManager.unsubscribe(listener);
    }


    public void subscribe(Object listener) {
        subscriptionManager.subscribe(listener);
    }


    public final void addErrorHandler(IPublicationErrorHandler handler) {
        synchronized (this){
            errorHandlers.add(handler);
        }
    }

    @Override
    public BusRuntime getRuntime() {
        return runtime;
    }

    protected MessagePublication createMessagePublication(T message) {
        Collection<Subscription> subscriptions = getSubscriptionsByMessageType(message.getClass());
        if ((subscriptions == null || subscriptions.isEmpty()) && !message.getClass().equals(DeadMessage.class)) {
            // Dead Event
            subscriptions = getSubscriptionsByMessageType(DeadMessage.class);
            return getPublicationFactory().createPublication(runtime, subscriptions, new DeadMessage(message));
        } else {
            return getPublicationFactory().createPublication(runtime, subscriptions, message);
        }
    }

    // obtain the set of subscriptions for the given message type
    // Note: never returns null!
    protected Collection<Subscription> getSubscriptionsByMessageType(Class messageType) {
        return subscriptionManager.getSubscriptionsByMessageType(messageType);
    }


    public void handlePublicationError(PublicationError error) {
        for (IPublicationErrorHandler errorHandler : errorHandlers) {
            errorHandler.handleError(error);
        }
    }

}
