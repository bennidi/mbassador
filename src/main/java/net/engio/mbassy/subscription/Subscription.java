package net.engio.mbassy.subscription;

import net.engio.mbassy.bus.MessagePublication;
import net.engio.mbassy.common.IConcurrentSet;
import net.engio.mbassy.dispatch.IMessageDispatcher;

import java.util.Comparator;
import java.util.UUID;

/**
 * A subscription is a thread-safe container that manages exactly one message handler of all registered
 * message listeners of the same class, i.e. all subscribed instances (exlcuding subclasses) of a SingleMessageHandler.class
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

    protected final IConcurrentSet<Object> listeners;

    private final IMessageDispatcher dispatcher;

    private final SubscriptionContext context;

    Subscription(SubscriptionContext context, IMessageDispatcher dispatcher, IConcurrentSet<Object> listeners) {
        this.context = context;
        this.dispatcher = dispatcher;
        this.listeners = listeners;
    }

    /**
     * Check whether this subscription manages a message handler of the given message listener class
     *
     * @param listener
     * @return
     */
    public boolean belongsTo(Class listener){
        return context.getHandlerMetadata().isFromListener(listener);
    }

    /**
     * Check whether this subscriptions manages the given listener instance
     * @param listener
     * @return
     */
    public boolean contains(Object listener){
        return listeners.contains(listener);
    }

    /**
     * Check whether this subscription manages a message handler
     * @param messageType
     * @return
     */
    public boolean handlesMessageType(Class<?> messageType) {
        return context.getHandlerMetadata().handlesMessage(messageType);
    }

    public Class[] getHandledMessageTypes(){
        return context.getHandlerMetadata().getHandledMessages();
    }


    public void publish(MessagePublication publication, Object message){
        if(listeners.size() > 0)
            dispatcher.dispatch(publication, message, listeners);
    }

    public int getPriority() {
        return context.getHandlerMetadata().getPriority();
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
