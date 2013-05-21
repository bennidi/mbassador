package net.engio.mbassy.subscription;

import net.engio.mbassy.common.ReflectionUtils;
import net.engio.mbassy.common.StrongConcurrentSet;
import net.engio.mbassy.listener.MessageHandlerMetadata;
import net.engio.mbassy.listener.MetadataReader;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Todo: Add javadoc
 *
 * @author bennidi
 *         Date: 5/11/13
 */
public class SubscriptionManager {

    // the metadata reader that is used to inspect objects passed to the subscribe method
    private final MetadataReader metadataReader;

    // all subscriptions per message type
    // this is the primary list for dispatching a specific message
    // write access is synchronized and happens only when a listener of a specific class is registered the first time
    private final Map<Class, Collection<Subscription>> subscriptionsPerMessage
            = new HashMap<Class, Collection<Subscription>>(50);

    // all subscriptions per messageHandler type
    // this map provides fast access for subscribing and unsubscribing
    // write access is synchronized and happens very infrequently
    // once a collection of subscriptions is stored it does not change
    private final Map<Class, Collection<Subscription>> subscriptionsPerListener
            = new HashMap<Class, Collection<Subscription>>(50);

    // remember already processed classes that do not contain any message handlers
    private final StrongConcurrentSet<Class> nonListeners = new StrongConcurrentSet<Class>();

    // this factory is used to create specialized subscriptions based on the given message handler configuration
    // it can be customized by implementing the getSubscriptionFactory() method
    private final SubscriptionFactory subscriptionFactory;

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    public SubscriptionManager(MetadataReader metadataReader, SubscriptionFactory subscriptionFactory) {
        this.metadataReader = metadataReader;
        this.subscriptionFactory = subscriptionFactory;
    }


    public boolean unsubscribe(Object listener) {
        if (listener == null) {
            return false;
        }
        Collection<Subscription> subscriptions = getSubscriptionsByListener(listener);
        if (subscriptions == null) {
            return false;
        }
        boolean isRemoved = true;
        for (Subscription subscription : subscriptions) {
            isRemoved &= subscription.unsubscribe(listener);
        }
        return isRemoved;
    }


    private Collection<Subscription> getSubscriptionsByListener(Object listener) {
        Collection<Subscription> subscriptions;
        try {
            readWriteLock.readLock().lock();
            subscriptions = subscriptionsPerListener.get(listener.getClass());
        } finally {
            readWriteLock.readLock().unlock();
        }
        return subscriptions;
    }

    public void subscribe(Object listener) {
        try {
            if (isKnownNonListener(listener)) {
                return; // early reject of known classes that do not define message handlers
            }
            Collection<Subscription> subscriptionsByListener = getSubscriptionsByListener(listener);
            // a listener is either subscribed for the first time
            if (subscriptionsByListener == null) {
                List<MessageHandlerMetadata> messageHandlers = metadataReader.getMessageListener(listener.getClass()).getHandlers();
                if (messageHandlers.isEmpty()) {  // remember the class as non listening class if no handlers are found
                    nonListeners.add(listener.getClass());
                    return;
                }
                subscriptionsByListener = new ArrayList<Subscription>(messageHandlers.size()); // it's safe to use non-concurrent collection here (read only)
                // create subscriptions for all detected message handlers
                for (MessageHandlerMetadata messageHandler : messageHandlers) {
                    // create the subscription
                    subscriptionsByListener.add(subscriptionFactory.createSubscription(messageHandler));
                }
                // this will acquire a write lock and handle the case when another thread already subscribed
                // this particular listener in the mean-time
                subscribe(listener, subscriptionsByListener);
            } // or the subscriptions already exist and must only be updated
            else {
                for (Subscription sub : subscriptionsByListener) {
                    sub.subscribe(listener);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void subscribe(Object listener, Collection<Subscription> subscriptions) {
        try {
            readWriteLock.writeLock().lock();
            // basically this is a deferred double check
            // it's an ugly pattern but necessary because atomic upgrade from read to write lock
            // is not possible and using a write lock from the beginning with will dramatically decrease performance
            Collection<Subscription> subscriptionsByListener = getSubscriptionsByListener(listener);

            if (subscriptionsByListener == null) {
                for (Subscription subscription : subscriptions) {
                    subscription.subscribe(listener);
                    for (Class<?> messageType : subscription.getHandledMessageTypes()) {
                        addMessageTypeSubscription(messageType, subscription);
                    }
                }
                subscriptionsPerListener.put(listener.getClass(), subscriptions);
            }
            // the rare case when multiple threads concurrently subscribed the same class for the first time
            // one will be first, all others will have to subscribe to the existing instead the generated subscriptions
            else {
                for (Subscription existingSubscription : subscriptionsByListener) {
                    existingSubscription.subscribe(listener);
                }
            }
        } finally {
            readWriteLock.writeLock().unlock();
        }


    }

    private boolean isKnownNonListener(Object listener) {
        Class listeningClass = listener.getClass();
        return nonListeners.contains(listeningClass);
    }

    // obtain the set of subscriptions for the given message type
    // Note: never returns null!
    public Collection<Subscription> getSubscriptionsByMessageType(Class messageType) {
        Set<Subscription> subscriptions = new TreeSet<Subscription>(Subscription.SubscriptionByPriorityDesc);
        readWriteLock.readLock().lock();
        if (subscriptionsPerMessage.get(messageType) != null) {
            subscriptions.addAll(subscriptionsPerMessage.get(messageType));
        }
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
        readWriteLock.readLock().unlock();
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
}
