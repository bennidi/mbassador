package net.engio.mbassy.subscription;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import net.engio.mbassy.common.ObjectTree;
import net.engio.mbassy.common.ReflectionUtils;
import net.engio.mbassy.common.WeakConcurrentSet;
import net.engio.mbassy.dispatch.IHandlerInvocation;
import net.engio.mbassy.dispatch.ReflectiveHandlerInvocation;
import net.engio.mbassy.dispatch.SynchronizedHandlerInvocation;
import net.engio.mbassy.error.MessageBusException;
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
 * @author dorkbox, llc
 *         Date: 2/2/15
 */
public class SubscriptionManager {

    // the metadata reader that is used to inspect objects passed to the subscribe method
    private final MetadataReader metadataReader = new MetadataReader();

    // all subscriptions per message type
    // this is the primary list for dispatching a specific message
    // write access is synchronized and happens only when a listener of a specific class is registered the first time
    private final Map<Class<?>, Collection<Subscription>> subscriptionsPerMessageSingle = new HashMap<Class<?>, Collection<Subscription>>(50);
    private final ObjectTree<Class<?>, Collection<Subscription>> subscriptionsPerMessageMulti =
                    new ObjectTree<Class<?>, Collection<Subscription>>();


    // all subscriptions per messageHandler type
    // this map provides fast access for subscribing and unsubscribing
    // write access is synchronized and happens very infrequently
    // once a collection of subscriptions is stored it does not change
    private final Map<Class<?>, Collection<Subscription>> subscriptionsPerListener = new HashMap<Class<?>, Collection<Subscription>>(50);

    // remember already processed classes that do not contain any message handlers
    private final ConcurrentHashMap<Class<?>, Object> nonListeners = new ConcurrentHashMap<Class<?>, Object>();

    // synchronize read/write acces to the subscription maps
    private final ReentrantReadWriteLock LOCK = new ReentrantReadWriteLock();


    public SubscriptionManager() {
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
            this.LOCK.readLock().lock();
            subscriptions = this.subscriptionsPerListener.get(listener.getClass());
        } finally {
            this.LOCK.readLock().unlock();
        }
        return subscriptions;
    }

    public void subscribe(Object listener) {
        try {
            Class<? extends Object> listenerClass = listener.getClass();

            if (this.nonListeners.contains(listenerClass)) {
                // early reject of known classes that do not define message handlers
                return;
            }

            Collection<Subscription> subscriptionsByListener = getSubscriptionsByListener(listener);
            // a listener is either subscribed for the first time
            if (subscriptionsByListener == null) {
                List<MessageHandler> messageHandlers = this.metadataReader.getMessageListener(listenerClass).getHandlers();
                if (messageHandlers.isEmpty()) {
                    // remember the class as non listening class if no handlers are found
                    this.nonListeners.put(listenerClass, this.nonListeners);
                    return;
                }

                // it's safe to use non-concurrent collection here (read only)
                subscriptionsByListener = new ArrayList<Subscription>(messageHandlers.size());

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
            }
            else {
                // or the subscriptions already exist and must only be updated
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
            this.LOCK.writeLock().lock();
            // basically this is a deferred double check
            // it's an ugly pattern but necessary because atomic upgrade from read to write lock
            // is not possible
            // the alternative of using a write lock from the beginning would decrease performance dramatically
            // because of the huge number of reads compared to writes
            Collection<Subscription> subscriptionsByListener = getSubscriptionsByListener(listener);

            if (subscriptionsByListener == null) {
                for (Subscription subscription : subscriptions) {
                    subscription.subscribe(listener);

                    // single or multi?
                    Class<?>[] handledMessageTypes = subscription.getHandledMessageTypes();
                    int size = handledMessageTypes.length;
                    if (size == 1) {
                        // single
                        Class<?> clazz = handledMessageTypes[0];

                        // NOTE: Not thread-safe! must be synchronized in outer scope
                        Collection<Subscription> subs = this.subscriptionsPerMessageSingle.get(clazz);
                        if (subs == null) {
                            subs = new LinkedList<Subscription>();
                            this.subscriptionsPerMessageSingle.put(clazz, subs);
                        }
                        subs.add(subscription);
                    } else {
                        // multi (is thread safe)
                        ObjectTree<Class<?>, Collection<Subscription>> tree = this.subscriptionsPerMessageMulti.createLeaves(handledMessageTypes);
                        Collection<Subscription> subs = tree.getValue();
                        if (subs == null) {
                            subs = new LinkedList<Subscription>();
                            tree.putValue(subs);
                        }
                        subs.add(subscription);
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
            this.LOCK.writeLock().unlock();
        }
    }












    // obtain the set of subscriptions for the given message type
    // Note: never returns null!
    public Collection<Subscription> getSubscriptionsByMessageType(Class<?> messageType) {
        // thread safe publication
        Collection<Subscription> subscriptions = new LinkedList<Subscription>();

        try {
            this.LOCK.readLock().lock();

            Set<Class<?>> types1 = ReflectionUtils.getSuperTypes(messageType);
            types1.add(messageType);

            // also add all subscriptions that match super types
            for (Class<?> eventSuperType : types1) {
                Collection<Subscription> subs = this.subscriptionsPerMessageSingle.get(eventSuperType);
                if (subs != null) {
                    for (Subscription sub : subs) {
                        if (sub.handlesMessageType(eventSuperType)) {
                            subscriptions.add(sub);
                        }
                    }
                }
            }

            ///////////////
            // a var-arg handler might match
            ///////////////
            // tricky part. We have to check the ARRAY version,
            for (Class<?> eventSuperType : types1) {
                // messy, but the ONLY way to do it.
                // NOTE: this will NEVER be an array to begin with, since that will call a DIFFERENT method
                eventSuperType = Array.newInstance(eventSuperType, 1).getClass();

                // also add all subscriptions that match super types
                Collection<Subscription> subs = this.subscriptionsPerMessageSingle.get(eventSuperType);
                if (subs != null) {
                    for (Subscription sub : subs) {
                        subscriptions.add(sub);
                    }
                }
            }
        } finally {
            this.LOCK.readLock().unlock();
        }

        return subscriptions;
    }

    // obtain the set of subscriptions for the given message types
    // Note: never returns null!
    public Collection<Subscription> getSubscriptionsByMessageType(Class<?> messageType1, Class<?> messageType2) {
        // thread safe publication
        Collection<Subscription> subscriptions = new LinkedList<Subscription>();

        try {
            this.LOCK.readLock().lock();

            Set<Class<?>> types1 = ReflectionUtils.getSuperTypes(messageType1);
            types1.add(messageType1);

            Set<Class<?>> types2 = ReflectionUtils.getSuperTypes(messageType2);
            types2.add(messageType2);

            // also add all subscriptions that match super types
            for (Class<?> eventSuperType1 : types1) {
                ObjectTree<Class<?>, Collection<Subscription>> leaf1 = this.subscriptionsPerMessageMulti.getLeaf(eventSuperType1);

                if (leaf1 != null) {
                    for (Class<?> eventSuperType2 : types2) {
                        ObjectTree<Class<?>, Collection<Subscription>> leaf2 = leaf1.getLeaf(eventSuperType2);

                        if (leaf2 != null) {
                            Collection<Subscription> subs = leaf2.getValue();
                            if (subs != null) {
                                for (Subscription sub : subs) {
                                    if (sub.handlesMessageType(eventSuperType1, eventSuperType2)) {
                                        subscriptions.add(sub);
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ///////////////
            // if they are ALL the same type, a var-arg handler might match
            ///////////////
            if (messageType1 == messageType2) {
                // tricky part. We have to check the ARRAY version
                for (Class<?> eventSuperType : types1) {
                    // messy, but the ONLY way to do it.
                    // NOTE: this will NEVER be an array to begin with, since that will call a DIFFERENT method
                    eventSuperType = Array.newInstance(eventSuperType, 1).getClass();

                    // also add all subscriptions that match super types
                    Collection<Subscription> subs = this.subscriptionsPerMessageSingle.get(eventSuperType);
                    if (subs != null) {
                        for (Subscription sub : subs) {
                            subscriptions.add(sub);
                        }
                    }
                }
            }
        } finally {
            this.LOCK.readLock().unlock();
        }

        return subscriptions;
    }


    // obtain the set of subscriptions for the given message types
    // Note: never returns null!
    public Collection<Subscription> getSubscriptionsByMessageType(Class<?> messageType1, Class<?> messageType2, Class<?> messageType3) {
        // thread safe publication
        Collection<Subscription> subscriptions = new LinkedList<Subscription>();

        try {
            this.LOCK.readLock().lock();

            Set<Class<?>> types1 = ReflectionUtils.getSuperTypes(messageType1);
            types1.add(messageType1);

            Set<Class<?>> types2 = ReflectionUtils.getSuperTypes(messageType2);
            types2.add(messageType2);

            Set<Class<?>> types3 = ReflectionUtils.getSuperTypes(messageType3);
            types3.add(messageType3);

            // also add all subscriptions that match super types
            for (Class<?> eventSuperType1 : types1) {
                ObjectTree<Class<?>, Collection<Subscription>> leaf1 = this.subscriptionsPerMessageMulti.getLeaf(eventSuperType1);

                if (leaf1 != null) {
                    for (Class<?> eventSuperType2 : types2) {
                        ObjectTree<Class<?>, Collection<Subscription>> leaf2 = leaf1.getLeaf(eventSuperType2);

                        if (leaf2 != null) {
                            for (Class<?> eventSuperType3 : types3) {
                                ObjectTree<Class<?>, Collection<Subscription>> leaf3 = leaf2.getLeaf(eventSuperType3);

                                if (leaf3 != null) {
                                    Collection<Subscription> subs = leaf3.getValue();
                                    if (subs != null) {
                                        for (Subscription sub : subs) {
                                            if (sub.handlesMessageType(eventSuperType1, eventSuperType2, eventSuperType3)) {
                                                subscriptions.add(sub);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            ///////////////
            // if they are ALL the same type, a var-arg handler might match
            ///////////////
            if (messageType1 == messageType2 && messageType2 == messageType3) {
                // tricky part. We have to check the ARRAY version
                for (Class<?> eventSuperType : types1) {
                    // messy, but the ONLY way to do it.
                    // NOTE: this will NEVER be an array to begin with, since that will call a DIFFERENT method
                    eventSuperType = Array.newInstance(eventSuperType, 1).getClass();

                    // also add all subscriptions that match super types
                    Collection<Subscription> subs = this.subscriptionsPerMessageSingle.get(eventSuperType);
                    if (subs != null) {
                        for (Subscription sub : subs) {
                            subscriptions.add(sub);
                        }
                    }
                }
            }
        } finally {
            this.LOCK.readLock().unlock();
        }

        return subscriptions;
    }

    // obtain the set of subscriptions for the given message types
    // Note: never returns null!
    public Collection<Subscription> getSubscriptionsByMessageType(Class<?>... messageTypes) {
        // thread safe publication
        Collection<Subscription> subscriptions = new LinkedList<Subscription>();

        try {
            this.LOCK.readLock().lock();

            int size = messageTypes.length;
            if (size == 0) {
                this.LOCK.readLock().unlock();
                return subscriptions;
            }

            boolean allSameType = true;
            Class<?> firstType = messageTypes[0];

            @SuppressWarnings("unchecked")
            Set<Class<?>>[] types = new Set[size];
            for (int i=0;i<size;i++) {
                Class<?> from = messageTypes[i];
                types[i] = ReflectionUtils.getSuperTypes(from);
                types[i].add(from);
                if (from != firstType) {
                    allSameType = false;
                }
            }


            // add all subscriptions that match super types combinations
            // have to use recursion for this. BLEH
            getSubsVarArg(subscriptions, types, size-1, 0, this.subscriptionsPerMessageMulti, messageTypes);

            ///////////////
            // if they are ALL the same type, a var-arg handler might match
            ///////////////
            if (allSameType) {
                // do we have a var-arg (it shows as an array) subscribed?

                // tricky part. We have to check the ARRAY version
                for (Class<?> eventSuperType : types[0]) {
                    // messy, but the ONLY way to do it.
                    // NOTE: this will NEVER be an array to begin with, since that will call a DIFFERENT method
                    eventSuperType = Array.newInstance(eventSuperType, 1).getClass();

                    // also add all subscriptions that match super types
                    Collection<Subscription> subs = this.subscriptionsPerMessageSingle.get(eventSuperType);
                    if (subs != null) {
                        for (Subscription sub : subs) {
                            subscriptions.add(sub);
                        }
                    }
                }
            }
        } finally {
            this.LOCK.readLock().unlock();
        }

        return subscriptions;
    }


    private void getSubsVarArg(Collection<Subscription> subscriptions, Set<Class<?>>[] types, int length, int index,
                               ObjectTree<Class<?>, Collection<Subscription>> tree, Class<?>[] messageTypes) {

        for (Class<?> eventSuperType : types[index]) {
            ObjectTree<Class<?>, Collection<Subscription>> leaf = tree.getLeaf(eventSuperType);
            if (leaf != null) {
                int newIndex = index+1;
                if (index == length) {
                    Collection<Subscription> subs = leaf.getValue();
                    if (subs != null) {
                        for (Subscription sub : subs) {
                            if (sub.handlesMessageType(messageTypes)) {
                                subscriptions.add(sub);
                            }
                        }
                    }
                } else {
                    getSubsVarArg(subscriptions, types, length, newIndex, leaf, messageTypes);
                }
            }
        }
    }
}
