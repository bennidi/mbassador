package net.engio.mbassy.bus;

import net.engio.mbassy.IPublicationErrorHandler;
import net.engio.mbassy.PublicationError;
import net.engio.mbassy.common.ReflectionUtils;
import net.engio.mbassy.listener.MessageHandlerMetadata;
import net.engio.mbassy.listener.MetadataReader;
import net.engio.mbassy.subscription.Subscription;
import net.engio.mbassy.subscription.SubscriptionContext;
import net.engio.mbassy.subscription.SubscriptionFactory;

import java.util.*;
import java.util.concurrent.*;

/**
 * The base class for all message bus implementations.
 *
 * @param <T>
 * @param <P>
 */
public abstract class AbstractSyncMessageBus<T, P extends ISyncMessageBus.ISyncPostCommand> implements ISyncMessageBus<T, P> {


    // the metadata reader that is used to parse objects passed to the subscribe method
    private final MetadataReader metadataReader;

    // all subscriptions per message type
    // this is the primary list for dispatching a specific message
    // write access is synchronized and happens very infrequently
    private final Map<Class, Collection<Subscription>> subscriptionsPerMessage
            = new HashMap<Class, Collection<Subscription>>(50);

    // all subscriptions per messageHandler type
    // this list provides fast access for subscribing and unsubscribing
    // write access is synchronized and happens very infrequently
    private final Map<Class, Collection<Subscription>> subscriptionsPerListener
            = new HashMap<Class, Collection<Subscription>>(50);

    // remember already processed classes that do not contain any listeners
    private final Collection<Class> nonListeners = new HashSet<Class>();

    // this handler will receive all errors that occur during message dispatch or message handling
    private final List<IPublicationErrorHandler> errorHandlers = new CopyOnWriteArrayList<IPublicationErrorHandler>();

    // this factory is used to create specialized subscriptions based on the given message handler configuration
    // it can be customized by implementing the getSubscriptionFactory() method
    private final SubscriptionFactory subscriptionFactory;

    private final MessagePublication.Factory publicationFactory;


    public AbstractSyncMessageBus(SyncBusConfiguration configuration) {
        subscriptionFactory = configuration.getSubscriptionFactory();
        this.metadataReader = configuration.getMetadataReader();
        this.publicationFactory = configuration.getMessagePublicationFactory();
        addErrorHandler(new IPublicationErrorHandler.ConsoleLogger());
    }

    protected MessagePublication.Factory getPublicationFactory() {
        return publicationFactory;
    }

    @Override
    public Collection<IPublicationErrorHandler> getRegisteredErrorHandlers() {
        return Collections.unmodifiableCollection(errorHandlers);
    }

    public boolean unsubscribe(Object listener) {
        if (listener == null) {
            return false;
        }
        Collection<Subscription> subscriptions = subscriptionsPerListener.get(listener.getClass());
        if (subscriptions == null) {
            return false;
        }
        boolean isRemoved = true;
        for (Subscription subscription : subscriptions) {
            isRemoved = isRemoved && subscription.unsubscribe(listener);
        }
        return isRemoved;
    }


    public void subscribe(Object listener) {
        try {
            Class listeningClass = listener.getClass();
            if (nonListeners.contains(listeningClass)) {
                return; // early reject of known classes that do not participate in eventing
            }
            Collection<Subscription> subscriptionsByListener = subscriptionsPerListener.get(listeningClass);
            if (subscriptionsByListener == null) { // if the type is registered for the first time
                synchronized (this) { // new subscriptions must be processed sequentially
                    subscriptionsByListener = subscriptionsPerListener.get(listeningClass);
                    if (subscriptionsByListener == null) {  // double check (a bit ugly but works here)
                        List<MessageHandlerMetadata> messageHandlers = metadataReader.getMessageHandlers(listeningClass);
                        if (messageHandlers.isEmpty()) {  // remember the class as non listening class if no handlers are found
                            nonListeners.add(listeningClass);
                            return;
                        }
                        subscriptionsByListener = new ArrayList<Subscription>(messageHandlers.size()); // it's safe to use non-concurrent collection here (read only)
                        // create subscriptions for all detected listeners
                        for (MessageHandlerMetadata messageHandler : messageHandlers) {
                            // create the subscription
                            Subscription subscription = subscriptionFactory
                                    .createSubscription(new SubscriptionContext(this, messageHandler));
                            subscription.subscribe(listener);
                            subscriptionsByListener.add(subscription);// add it for the listener type (for future subscriptions)

                            List<Class<?>> messageTypes = messageHandler.getHandledMessages();
                            for (Class<?> messageType : messageTypes) {
                                addMessageTypeSubscription(messageType, subscription);
                            }
                            //updateMessageTypeHierarchy(eventType);
                        }
                        subscriptionsPerListener.put(listeningClass, subscriptionsByListener);
                    }
                }
            }
            // register the listener to the existing subscriptions
            for (Subscription sub : subscriptionsByListener) {
                sub.subscribe(listener);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public final void addErrorHandler(IPublicationErrorHandler handler) {
        errorHandlers.add(handler);
    }



    // obtain the set of subscriptions for the given message type
    // Note: never returns null!
    protected Collection<Subscription> getSubscriptionsByMessageType(Class messageType) {
        Set<Subscription> subscriptions = new TreeSet<Subscription>(Subscription.SubscriptionByPriorityDesc);

        if (subscriptionsPerMessage.get(messageType) != null) {
            subscriptions.addAll(subscriptionsPerMessage.get(messageType));
        }
        // TODO: get superclasses is eligible for caching
        for (Class eventSuperType : ReflectionUtils.getSuperclasses(messageType)) {
            Collection<Subscription> subs = subscriptionsPerMessage.get(eventSuperType);
            if (subs != null) {
                for (Subscription sub : subs) {
                    if (sub.handlesMessageType(messageType)) {
                        subscriptions.add(sub);
                    }
                }
            }
        }
        return subscriptions;
    }


    // associate a suscription with a message type
    // NOTE: Not thread-safe! must be synchronized in outer scope
    private void addMessageTypeSubscription(Class messageType, Subscription subscription) {
        Collection<Subscription> subscriptions = subscriptionsPerMessage.get(messageType);
        if (subscriptions == null) {
            subscriptions = new LinkedList<Subscription>();
            subscriptionsPerMessage.put(messageType, subscriptions);
        }
        subscriptions.add(subscription);
    }


    public void handlePublicationError(PublicationError error) {
        for (IPublicationErrorHandler errorHandler : errorHandlers) {
            errorHandler.handleError(error);
        }
    }

}
