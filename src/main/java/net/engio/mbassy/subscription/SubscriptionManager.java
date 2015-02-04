package net.engio.mbassy.subscription;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.engio.mbassy.bus.error.MessageBusException;
import net.engio.mbassy.common.ReflectionUtils;
import net.engio.mbassy.common.WeakConcurrentSet;
import net.engio.mbassy.dispatch.IHandlerInvocation;
import net.engio.mbassy.dispatch.ReflectiveHandlerInvocation;
import net.engio.mbassy.dispatch.SynchronizedHandlerInvocation;
import net.engio.mbassy.listener.MessageHandler;
import net.engio.mbassy.listener.MetadataReader;

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
    private final Map<Class<?>, Collection<Subscription>> subscriptionsPerMessage
            = new HashMap<Class<?>, Collection<Subscription>>(50);

    // all subscriptions per messageHandler type
    // this map provides fast access for subscribing and unsubscribing
    // write access is synchronized and happens very infrequently
    // once a collection of subscriptions is stored it does not change
    private final Map<Class<?>, Collection<Subscription>> subscriptionsPerListener
            = new HashMap<Class<?>, Collection<Subscription>>(50);

    // remember already processed classes that do not contain any message handlers
    private final ConcurrentHashMap<Class<?>, Object> nonListeners = new ConcurrentHashMap<Class<?>, Object>();

    // synchronize read/write acces to the subscription maps
    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();


    public SubscriptionManager(MetadataReader metadataReader) {
        this.metadataReader = metadataReader;
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
            this.readWriteLock.readLock().lock();
            subscriptions = this.subscriptionsPerListener.get(listener.getClass());
        } finally {
            this.readWriteLock.readLock().unlock();
        }
        return subscriptions;
    }

    public void subscribe(Object listener) {
        try {
            Class<? extends Object> listenerClass = listener.getClass();

            if (this.nonListeners.contains(listenerClass)) {
                return; // early reject of known classes that do not define message handlers
            }

            Collection<Subscription> subscriptionsByListener = getSubscriptionsByListener(listener);
            // a listener is either subscribed for the first time
            if (subscriptionsByListener == null) {
                List<MessageHandler> messageHandlers = this.metadataReader.getMessageListener(listenerClass).getHandlers();
                if (messageHandlers.isEmpty()) {  // remember the class as non listening class if no handlers are found
                    this.nonListeners.put(listenerClass, this.nonListeners);
                    return;
                }
                subscriptionsByListener = new ArrayList<Subscription>(messageHandlers.size()); // it's safe to use non-concurrent collection here (read only)

                // create subscriptions for all detected message handlers
                for (MessageHandler messageHandler : messageHandlers) {
                    // create the subscription

                    try {
                        IHandlerInvocation invocation = new ReflectiveHandlerInvocation();

                        if (messageHandler.isSynchronized()){
                            invocation = new SynchronizedHandlerInvocation(invocation);
                        }

                        Subscription subscription = new Subscription(messageHandler, invocation, new WeakConcurrentSet<Object>());
                        subscriptionsByListener.add(subscription);
                    } catch (Exception e) {
                        throw new MessageBusException(e);
                    }
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
            this.readWriteLock.writeLock().lock();
            // basically this is a deferred double check
            // it's an ugly pattern but necessary because atomic upgrade from read to write lock
            // is not possible
            // the alternative of using a write lock from the beginning would decrease performance dramatically
            // because of the huge number of reads compared to writes
            Collection<Subscription> subscriptionsByListener = getSubscriptionsByListener(listener);

            if (subscriptionsByListener == null) {
                for (Subscription subscription : subscriptions) {
                    subscription.subscribe(listener);
                    for (Class<?> messageType : subscription.getHandledMessageTypes()) {
                        addMessageTypeSubscription(messageType, subscription);
                    }
                }
                this.subscriptionsPerListener.put(listener.getClass(), subscriptions);
            }
            // the rare case when multiple threads concurrently subscribed the same class for the first time
            // one will be first, all others will have to subscribe to the existing instead the generated subscriptions
            else {
                for (Subscription existingSubscription : subscriptionsByListener) {
                    existingSubscription.subscribe(listener);
                }
            }
        } finally {
            this.readWriteLock.writeLock().unlock();
        }


    }

    // obtain the set of subscriptions for the given message type
    // Note: never returns null!
    public Collection<Subscription> getSubscriptionsByMessageType(Class<?> messageType) {
        // thread safe publication
        Collection<Subscription> subscriptions = new LinkedList<Subscription>();

        try{
            this.readWriteLock.readLock().lock();

            Collection<Subscription> collection = this.subscriptionsPerMessage.get(messageType);
            if (collection != null) {
                subscriptions.addAll(collection);
            }

            // also add all subscriptions that match super types
            for (Class<?> eventSuperType : ReflectionUtils.getSuperTypes(messageType)) {
                Collection<Subscription> subs = this.subscriptionsPerMessage.get(eventSuperType);
                if (subs != null) {
                    for (Subscription sub : subs) {
                        if (sub.handlesMessageType(messageType)) {
                            subscriptions.add(sub);
                        }
                    }
                }
            }
        }finally{
            this.readWriteLock.readLock().unlock();
        }

        return subscriptions;
    }


    // associate a subscription with a message type
    // NOTE: Not thread-safe! must be synchronized in outer scope
    private void addMessageTypeSubscription(Class<?> messageType, Subscription subscription) {
        Collection<Subscription> subscriptions = this.subscriptionsPerMessage.get(messageType);
        if (subscriptions == null) {
            subscriptions = new LinkedList<Subscription>();
            this.subscriptionsPerMessage.put(messageType, subscriptions);
        }
        subscriptions.add(subscription);
    }
}
