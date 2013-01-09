package org.mbassy;

import org.mbassy.subscription.Subscription;

import java.util.Collection;

/**
 * A message publication is created for each asynchronous message dispatch. It reflects the state
 * of the corresponding message publication process, i.e. provides information whether the
 * publication was successfully scheduled, is currently running etc.
 *
 * @author bennidi
 * Date: 11/16/12
 */
public class MessagePublication<T> {

    public static <T> MessagePublication<T> Create(Collection<Subscription> subscriptions, T message, boolean isDeadEvent){
        return new MessagePublication<T>(subscriptions, message, State.Initial, isDeadEvent);
    }

    private Collection<Subscription> subscriptions;

    private T message;

    private State state = State.Scheduled;
    
    private boolean isDeadEvent;

    private MessagePublication(Collection<Subscription> subscriptions, T message, State initialState, boolean isDeadEvent) {
        this.subscriptions = subscriptions;
        this.message = message;
        this.state = initialState;
        this.isDeadEvent = isDeadEvent;
    }

    public boolean add(Subscription subscription) {
        return subscriptions.add(subscription);
    }

    protected void execute(){
        state = State.Running;
        Object curMessage = isDeadEvent ? new DeadEvent(message) : message; 
        for(Subscription sub : subscriptions){
            sub.publish(curMessage);
        }
        state = State.Finished;
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

    public MessagePublication<T> markScheduled(){
        if(!state.equals(State.Initial))
            return this;
        state = State.Scheduled;
        return this;
    }

    public MessagePublication<T> setError(){
        state = State.Error;
        return this;
    }

    private enum State{
        Initial,Scheduled,Running,Finished,Error;
    }

}
