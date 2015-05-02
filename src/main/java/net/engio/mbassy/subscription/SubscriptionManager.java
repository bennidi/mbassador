package net.engio.mbassy.subscription;

import net.engio.mbassy.bus.BusRuntime;
import net.engio.mbassy.common.ConcurrentHashMapV8;
import net.engio.mbassy.common.ISetEntry;
import net.engio.mbassy.common.ReflectionUtils;
import net.engio.mbassy.common.StrongConcurrentSet;
import net.engio.mbassy.listener.MessageHandler;
import net.engio.mbassy.listener.MetadataReader;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * The subscription managers responsibility is to consistently handle and synchronize the message listener subscription process.
 * It provides fast lookup of existing subscriptions when another instance of an already known
 * listener is subscribed and takes care of creating new set of subscriptions for any unknown class that defines
 * message handlers.
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
    private final Map<Class, StrongConcurrentSet<Subscription>> subscriptionsPerMessage;

    // all subscriptions per messageHandler type
    // this map provides fast access for subscribing and unsubscribing
    // write access is synchronized and happens very infrequently
    // once a collection of subscriptions is stored it does not change
    private final Map<Class, StrongConcurrentSet<Subscription>> subscriptionsPerListener;


    // remember already processed classes that do not contain any message handlers
    private final StrongConcurrentSet<Class> nonListeners = new StrongConcurrentSet<Class>();

    // this factory is used to create specialized subscriptions based on the given message handler configuration
    // it can be customized by implementing the getSubscriptionFactory() method
    private final SubscriptionFactory subscriptionFactory;

    // synchronize read/write acces to the subscription maps
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();

    private final BusRuntime runtime;

    public SubscriptionManager(MetadataReader metadataReader, SubscriptionFactory subscriptionFactory, BusRuntime runtime) {
        this.metadataReader = metadataReader;
        this.subscriptionFactory = subscriptionFactory;
        this.runtime = runtime;

        // ConcurrentHashMapV8 is 15%-20% faster than regular ConcurrentHashMap, which is also faster than HashMap.
        subscriptionsPerMessage = new ConcurrentHashMapV8<Class, StrongConcurrentSet<Subscription>>(50);
        subscriptionsPerListener = new ConcurrentHashMapV8<Class, StrongConcurrentSet<Subscription>>(50);
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


    private StrongConcurrentSet<Subscription> getSubscriptionsByListener(Object listener) {
        StrongConcurrentSet<Subscription> subscriptions;
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
            StrongConcurrentSet<Subscription> subscriptionsByListener = getSubscriptionsByListener(listener);
            // a listener is either subscribed for the first time
            if (subscriptionsByListener == null) {
                StrongConcurrentSet<MessageHandler> messageHandlers = metadataReader.getMessageListener(listener.getClass()).getHandlers();
                if (messageHandlers.isEmpty()) {  // remember the class as non listening class if no handlers are found
                    nonListeners.add(listener.getClass());
                    return;
                }
                subscriptionsByListener = new StrongConcurrentSet<Subscription>(messageHandlers.size()); // it's safe to use non-concurrent collection here (read only)

                // create subscriptions for all detected message handlers

                ISetEntry<MessageHandler> current = messageHandlers.head;
                MessageHandler handler;
                while (current != null) {
                    handler = current.getValue();
                    current = current.next();

                    // create the subscription
                    subscriptionsByListener.add(subscriptionFactory.createSubscription(runtime, handler));
                }
                // this will acquire a write lock and handle the case when another thread already subscribed
                // this particular listener in the mean-time
                subscribe(listener, subscriptionsByListener);
            } // or the subscriptions already exist and must only be updated
            else {
                ISetEntry<Subscription> current = subscriptionsByListener.head;
                Subscription sub;
                while (current != null) {
                    sub = current.getValue();
                    current = current.next();

                    sub.subscribe(listener);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private void subscribe(Object listener, StrongConcurrentSet<Subscription> subscriptions) {
        try {
            readWriteLock.writeLock().lock();
            // basically this is a deferred double check
            // it's an ugly pattern but necessary because atomic upgrade from read to write lock
            // is not possible
            // the alternative of using a write lock from the beginning would decrease performance dramatically
            // because of the huge number of reads compared to writes
            StrongConcurrentSet<Subscription> subscriptionsByListener = getSubscriptionsByListener(listener);

            if (subscriptionsByListener == null) {
                ISetEntry<Subscription> current = subscriptions.head;
                Subscription subscription;
                while (current != null) {
                    subscription = current.getValue();
                    current = current.next();

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
                ISetEntry<Subscription> current = subscriptionsByListener.head;
                Subscription existingSubscription;
                while (current != null) {
                    existingSubscription = current.getValue();
                    current = current.next();

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
        try{
            readWriteLock.readLock().lock();

            if (subscriptionsPerMessage.get(messageType) != null) {
                subscriptions.addAll(subscriptionsPerMessage.get(messageType));
            }
            for (Class eventSuperType : ReflectionUtils.getSuperTypes(messageType)) {
                StrongConcurrentSet<Subscription> subs = subscriptionsPerMessage.get(eventSuperType);
                if (subs != null) {
                    ISetEntry<Subscription> current = subs.head;
                    Subscription sub;
                    while (current != null) {
                        sub = current.getValue();
                        current = current.next();

                        if (sub.handlesMessageType(messageType)) {
                            subscriptions.add(sub);
                        }
                    }
                }
            }
        }finally{
            readWriteLock.readLock().unlock();
        }
        return subscriptions;
    }


    // associate a subscription with a message type
    // NOTE: Not thread-safe! must be synchronized in outer scope
    private void addMessageTypeSubscription(Class messageType, Subscription subscription) {
        StrongConcurrentSet<Subscription> subscriptions = subscriptionsPerMessage.get(messageType);
        if (subscriptions == null) {
            subscriptions = new StrongConcurrentSet<Subscription>();
            subscriptionsPerMessage.put(messageType, subscriptions);
        }
        subscriptions.add(subscription);
    }
}
