package net.engio.mbassy.subscription;

import net.engio.mbassy.bus.BusRuntime;
import net.engio.mbassy.common.ReflectionUtils;
import net.engio.mbassy.common.StrongConcurrentSet;
import net.engio.mbassy.listener.MessageHandler;
import net.engio.mbassy.listener.MetadataReader;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;

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
    private final Map<Class, ArrayList<Subscription>> subscriptionsPerMessage;

    // all subscriptions per messageHandler type
    // this map provides fast access for subscribing and unsubscribing
    // write access is synchronized and happens very infrequently
    // once a collection of subscriptions is stored it does not change
    private final Map<Class, Subscription[]> subscriptionsPerListener;


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

        subscriptionsPerMessage = new HashMap<Class, ArrayList<Subscription>>(64);
        subscriptionsPerListener = new HashMap<Class, Subscription[]>(64);
    }


    public boolean unsubscribe(Object listener) {
        if (listener == null) {
            return false;
        }
        Subscription[] subscriptions = getSubscriptionsByListener(listener);
        if (subscriptions == null) {
            return false;
        }
        boolean isRemoved = true;
        for (Subscription subscription : subscriptions) {
            isRemoved &= subscription.unsubscribe(listener);
        }
        return isRemoved;
    }


    private Subscription[] getSubscriptionsByListener(Object listener) {
        Subscription[] subscriptions;
        ReadLock readLock = readWriteLock.readLock();
        try {
            readLock.lock();
            subscriptions = subscriptionsPerListener.get(listener.getClass());
        } finally {
            readLock.unlock();
        }
        return subscriptions;
    }

    public void subscribe(Object listener) {
        try {
            Class<?> listenerClass = listener.getClass();

            if (nonListeners.contains(listenerClass)) {
                return; // early reject of known classes that do not define message handlers
            }

            Subscription[] subscriptionsByListener = getSubscriptionsByListener(listener);
            // a listener is either subscribed for the first time
            if (subscriptionsByListener == null) {
                MessageHandler[] messageHandlers = metadataReader.getMessageListener(listenerClass).getHandlers();
                int length = messageHandlers.length;

                if (length == 0) {  // remember the class as non listening class if no handlers are found
                    nonListeners.add(listenerClass);
                    return;
                }
                subscriptionsByListener = new Subscription[length]; // it's safe to use non-concurrent collection here (read only)

                // create subscriptions for all detected message handlers
                MessageHandler messageHandler;
                for (int i=0; i<length; i++) {
                    messageHandler = messageHandlers[i];
                    subscriptionsByListener[i] = subscriptionFactory.createSubscription(runtime, messageHandler);
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


    private void subscribe(Object listener, Subscription[] subscriptions) {
        WriteLock writeLock = readWriteLock.writeLock();
        try {
            writeLock.lock();
            // basically this is a deferred double check
            // it's an ugly pattern but necessary because atomic upgrade from read to write lock
            // is not possible
            // the alternative of using a write lock from the beginning would decrease performance dramatically
            // because of the huge number of reads compared to writes
            Subscription[] subscriptionsByListener = getSubscriptionsByListener(listener);

            if (subscriptionsByListener == null) {
                for (Subscription subscription : subscriptions) {
                    subscription.subscribe(listener);

                    for (Class<?> messageType : subscription.getHandledMessageTypes()) {
                        // associate a subscription with a message type
                        ArrayList<Subscription> subscriptions2 = subscriptionsPerMessage.get(messageType);
                        if (subscriptions2 == null) {
                            subscriptions2 = new ArrayList<Subscription>(8);
                            subscriptionsPerMessage.put(messageType, subscriptions2);
                        }
                        subscriptions2.add(subscription);
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
            writeLock.unlock();
        }


    }

    // obtain the set of subscriptions for the given message type
    // Note: never returns null!
    public Collection<Subscription> getSubscriptionsByMessageType(Class messageType) {
        Set<Subscription> subscriptions = new TreeSet<Subscription>(Subscription.SubscriptionByPriorityDesc);
        ReadLock readLock = readWriteLock.readLock();
        try {
            readLock.lock();

            Subscription subscription;
            ArrayList<Subscription> subsPerMessage = subscriptionsPerMessage.get(messageType);

            if (subsPerMessage != null) {
                subscriptions.addAll(subsPerMessage);
            }

            for (Class eventSuperType : ReflectionUtils.getSuperTypes(messageType)) {
                ArrayList<Subscription> subs = subscriptionsPerMessage.get(eventSuperType);
                if (subs != null) {
                    for (int i = 0; i < subs.size(); i++) {
                        subscription = subs.get(i);

                        if (subscription.handlesMessageType(messageType)) {
                            subscriptions.add(subscription);
                        }
                    }
                }
            }
        }finally{
            readLock.unlock();
        }
        return subscriptions;
    }
}
