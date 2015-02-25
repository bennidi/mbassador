package net.engio.mbassy.bus;

import net.engio.mbassy.bus.common.DeadMessage;
import net.engio.mbassy.bus.common.Properties;
import net.engio.mbassy.bus.common.PubSubSupport;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;
import net.engio.mbassy.subscription.Subscription;
import net.engio.mbassy.subscription.SubscriptionManager;

import java.util.*;

import static net.engio.mbassy.bus.common.Properties.Handler.PublicationError;

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

    private final BusRuntime runtime;


    public AbstractPubSubSupport(IBusConfiguration configuration) {
        if(!configuration.hasProperty(Properties.Handler.PublicationError)){
            System.out.println("WARN: No error handler configured to handle exceptions during publication.\n" +
                    "Error handlers can be added to any instance of  AbstractPubSubSupport or via BusConfiguration. \n" +
                    "Falling back to console logger.");
        }
        this.errorHandlers.add(configuration.getProperty(Properties.Handler.PublicationError, new IPublicationErrorHandler.ConsoleLogger()));
        this.runtime = new BusRuntime(this)
            .add(PublicationError, getRegisteredErrorHandlers())
            .add(Properties.Common.Id, UUID.randomUUID().toString());
        // configure the pub sub feature
        Feature.SyncPubSub pubSubFeature = configuration.getFeature(Feature.SyncPubSub.class);
        this.subscriptionManager = pubSubFeature.getSubscriptionManagerProvider()
                .createManager(pubSubFeature.getMetadataReader(),
                        pubSubFeature.getSubscriptionFactory(), runtime);
        this.publicationFactory = pubSubFeature.getPublicationFactory();
    }

    protected MessagePublication.Factory getPublicationFactory() {
        return publicationFactory;
    }


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

    protected IMessagePublication createMessagePublication(T message) {
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{ " + runtime.get(Properties.Common.Id) + "}";
    }
}
