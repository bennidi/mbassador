package net.engio.mbassy.subscription;

import net.engio.mbassy.bus.IMessagePublication;
import net.engio.mbassy.common.IConcurrentSet;
import net.engio.mbassy.dispatch.IMessageDispatcher;

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
    public boolean belongsTo(Class<?> listener){
        return this.context.getHandlerMetadata().isFromListener(listener);
    }

    /**
     * Check whether this subscriptions manages the given listener instance
     * @param listener
     * @return
     */
    public boolean contains(Object listener){
        return this.listeners.contains(listener);
    }

    /**
     * Check whether this subscription manages a message handler
     * @param messageType
     * @return
     */
    public boolean handlesMessageType(Class<?> messageType) {
        return this.context.getHandlerMetadata().handlesMessage(messageType);
    }

    public Class<?>[] getHandledMessageTypes(){
        return this.context.getHandlerMetadata().getHandledMessages();
    }


    public void publish(IMessagePublication publication, Object message){
        if(this.listeners.size() > 0) {
            this.dispatcher.dispatch(publication, message, this.listeners);
        }
    }

    public void subscribe(Object o) {
        this.listeners.add(o);
    }


    public boolean unsubscribe(Object existingListener) {
        return this.listeners.remove(existingListener);
    }

    public int size() {
        return this.listeners.size();
    }
}
