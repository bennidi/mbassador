package org.mbassy.subscription;

import java.util.Comparator;
import java.util.UUID;

import org.mbassy.common.ConcurrentSet;
import org.mbassy.dispatch.IMessageDispatcher;
import org.mbassy.dispatch.MessagingContext;

/**
 * A subscription is a thread safe container for objects that contain message handlers
 */
public class Subscription {

    private UUID id = UUID.randomUUID();

    protected ConcurrentSet<Object> listeners = new ConcurrentSet<Object>();

    private IMessageDispatcher dispatcher;

    private MessagingContext context;

    public Subscription(MessagingContext context, IMessageDispatcher dispatcher) {
        this.context = context;
        this.dispatcher = dispatcher;
    }


    public void publish(Object message){
          dispatcher.dispatch(message, listeners);
    }

    public MessagingContext getContext(){
        return context;
    }

    public int getPriority(){
        return context.getHandlerMetadata().getPriority();
    }


    public void subscribe(Object o) {
        listeners.add(o);
    }


    public boolean unsubscribe(Object existingListener) {
        return listeners.remove(existingListener);
    }

    public int size(){
        return listeners.size();
    }


    public static final Comparator<Subscription> SubscriptionByPriorityDesc = new Comparator<Subscription>() {
        @Override
        public int compare(Subscription o1, Subscription o2) {
            int result =  o1.getPriority() - o2.getPriority();
            return result == 0 ? o1.id.compareTo(o2.id): result;
        }
    };

}
