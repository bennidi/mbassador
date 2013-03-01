package net.engio.mbassy;

import net.engio.mbassy.common.DeadEvent;
import net.engio.mbassy.common.FilteredEvent;
import net.engio.mbassy.subscription.Subscription;

import java.util.Collection;

/**
 * A message publication is created for each asynchronous message dispatch. It reflects the state
 * of the corresponding message publication process, i.e. provides information whether the
 * publication was successfully scheduled, is currently running etc.
 *
 * A message publication lives within a single thread. It is not designed in a thread-safe manner -> not eligible to
 * be used in multiple threads simultaneously .
 *
 * @author bennidi
 * Date: 11/16/12
 */
public class MessagePublication {

    public static  MessagePublication Create(IMessageBus bus, Collection<Subscription> subscriptions, Object message){
        return new MessagePublication(bus,subscriptions, message, State.Initial);
    }

    private Collection<Subscription> subscriptions;

    private Object message;

    private State state = State.Scheduled;

    private boolean delivered = false;

    private IMessageBus bus;

    private MessagePublication(IMessageBus bus, Collection<Subscription> subscriptions, Object message, State initialState) {
        this.bus = bus;
        this.subscriptions = subscriptions;
        this.message = message;
        this.state = initialState;
    }

    public boolean add(Subscription subscription) {
        return subscriptions.add(subscription);
    }

    protected void execute(){
        state = State.Running;
        for(Subscription sub : subscriptions){
            sub.publish(this, message);
        }
        state = State.Finished;
        if(!delivered && !isFilteredEvent() && !isDeadEvent()){
            bus.post(new FilteredEvent(message)).now();
        }
    }

    public boolean isFinished() {
        return state.equals(State.Finished);
    }

    public boolean isRunning() {
        return state.equals(State.Running);
    }

    public boolean isScheduled() {
        return state.equals(State.Scheduled);
    }

    public void markDelivered(){
        delivered = true;
    }

    public MessagePublication markScheduled(){
        if(!state.equals(State.Initial))
            return this;
        state = State.Scheduled;
        return this;
    }

    public MessagePublication setError(){
        state = State.Error;
        return this;
    }

    public boolean isDeadEvent(){
        return DeadEvent.class.isAssignableFrom(message.getClass());
    }

    public boolean isFilteredEvent(){
        return FilteredEvent.class.isAssignableFrom(message.getClass());
    }

    private enum State{
        Initial,Scheduled,Running,Finished,Error;
    }

}
