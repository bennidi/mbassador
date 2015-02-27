package net.engio.mbassy.subscription;

import net.engio.mbassy.bus.IMessagePublication;
import net.engio.mbassy.dispatch.IMessageDispatcher;

import java.util.Collection;
import java.util.Comparator;
import java.util.UUID;

/**
 * A subscription is a thread-safe container that manages exactly one message handler of all registered
 * message listeners of the same class, i.e. all subscribed instances (excluding subclasses) of a SingleMessageHandler.class
 * will be referenced in the subscription created for SingleMessageHandler.class.
 *
 * There will be as many unique subscription objects per message listener class as there are message handlers
 * defined in the message listeners class hierarchy.
 *
 * The subscription provides functionality for message publication by means of delegation to the respective
 * message dispatcher.
 *
 */
public class Subscription {

    private final UUID id = UUID.randomUUID();

    protected final Collection<Object> listeners;

    private final IMessageDispatcher dispatcher;

    private final SubscriptionContext context;

    Subscription(SubscriptionContext context, IMessageDispatcher dispatcher, Collection<Object> listeners) {
        this.context = context;
        this.dispatcher = dispatcher;
        this.listeners = listeners;
    }

    /**
     * Check whether this subscription manages a message handler of the given listener class.
     */
    public boolean belongsTo(Class listener){
        return context.getHandler().isFromListener(listener);
    }

    /**
     * Check whether this subscriptions manages the given listener instance.
     */
    public boolean contains(Object listener){
        return listeners.contains(listener);
    }

    /**
     * Check whether this subscription manages a specific message type.
     */
    public boolean handlesMessageType(Class<?> messageType) {
        return context.getHandler().handlesMessage(messageType);
    }

    public Class[] getHandledMessageTypes(){
        return context.getHandler().getHandledMessages();
    }


    public void publish(IMessagePublication publication, Object message){
        if(!listeners.isEmpty())
            dispatcher.dispatch(publication, message, listeners);
    }

    public int getPriority() {
        return context.getHandler().getPriority();
    }


    public void subscribe(Object o) {
        listeners.add(o);
    }


    public boolean unsubscribe(Object existingListener) {
        return listeners.remove(existingListener);
    }

    public int size() {
        return listeners.size();
    }


    public static final Comparator<Subscription> SubscriptionByPriorityDesc = new Comparator<Subscription>() {
        @Override
        public int compare(Subscription o1, Subscription o2) {
            int byPriority = ((Integer)o2.getPriority()).compareTo(o1.getPriority());
            return byPriority == 0 ? o2.id.compareTo(o1.id) : byPriority;
        }
    };



}
