package net.engio.mbassy.bus;

import java.util.Collection;

import net.engio.mbassy.bus.common.DeadMessage;
import net.engio.mbassy.subscription.Subscription;

/**
 * A message publication is created for each asynchronous message dispatch. It reflects the state
 * of the corresponding message publication process, i.e. provides information whether the
 * publication was successfully scheduled, is currently running etc.
 * <p/>
 * A message publication lives within a single thread. It is not designed in a thread-safe manner -> not eligible to
 * be used in multiple threads simultaneously .
 *
 * @author bennidi
 *         Date: 11/16/12
 */
public class MessagePublication implements IMessagePublication {

    private final Collection<Subscription> subscriptions;
    private final Object message;
    // message publications can be referenced by multiple threads to query publication progress
    private volatile State state = State.Initial;
    private volatile boolean delivered = false;
    private final BusRuntime runtime;

    protected MessagePublication(BusRuntime runtime, Collection<Subscription> subscriptions, Object message, State initialState) {
        this.runtime = runtime;
        this.subscriptions = subscriptions;
        this.message = message;
        this.state = initialState;
    }

    @Override
    public boolean add(Subscription subscription) {
        return this.subscriptions.add(subscription);
    }

    /*
    TODO: document state transitions
     */
    @Override
    public void execute() {
        this.state = State.Running;
        for (Subscription sub : this.subscriptions) {
           sub.publish(this, this.message);
        }
        this.state = State.Finished;
        // if the message has not been marked delivered by the dispatcher
        if (!this.delivered) {
            if (!isDeadEvent()) {
                this.runtime.getProvider().publish(new DeadMessage(this.message));
            }
        }
    }

    @Override
    public boolean isFinished() {
        return this.state.equals(State.Finished);
    }

    @Override
    public boolean isRunning() {
        return this.state.equals(State.Running);
    }

    @Override
    public boolean isScheduled() {
        return this.state.equals(State.Scheduled);
    }

    @Override
    public void markDelivered() {
        this.delivered = true;
    }

    @Override
    public MessagePublication markScheduled() {
        if (this.state.equals(State.Initial)) {
            this.state = State.Scheduled;
        }
        return this;
    }


    @Override
    public boolean isDeadEvent() {
        return DeadMessage.class.equals(this.message.getClass());
    }

    @Override
    public Object getMessage() {
        return this.message;
    }

    private enum State {
        Initial, Scheduled, Running, Finished, Error
    }

    public static class Factory {

        public IMessagePublication createPublication(BusRuntime runtime, Collection<Subscription> subscriptions, Object message) {
            return new MessagePublication(runtime, subscriptions, message, State.Initial);
        }

    }

}
