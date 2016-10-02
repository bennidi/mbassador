package net.engio.mbassy.bus;

import net.engio.mbassy.bus.common.DeadMessage;
import net.engio.mbassy.bus.common.FilteredMessage;
import net.engio.mbassy.bus.error.PublicationError;
import net.engio.mbassy.subscription.Subscription;

import java.util.Collection;

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
    private volatile boolean dispatched = false;
    private final BusRuntime runtime;
    private PublicationError error = null;


    protected MessagePublication(BusRuntime runtime, Collection<Subscription> subscriptions, Object message, State initialState) {
        this.runtime = runtime;
        this.subscriptions = subscriptions;
        this.message = message;
        this.state = initialState;
    }

    public boolean add(Subscription subscription) {
        return subscriptions.add(subscription);
    }

    /*
    TODO: document state transitions
     */
    public void execute() {
        state = State.Running;
        for (Subscription sub : subscriptions) {
           sub.publish(this, message);
        }
        state = State.Finished;
        // This part is necessary to support the feature of publishing a DeadMessage or FilteredMessage
        // in case that the original message has not made it to any listener.
        // This happens if subscriptions are empty (due to GC of weak listeners or explicit desubscription)
        // or if configured filters do not let a message pass. The flag is set by the dispatchers.
        // META: This seems to be a suboptimal design
        if (!dispatched) {
            if (!isFilteredMessage() && !isDeadMessage()) {
                runtime.getProvider().publish(new FilteredMessage(message));
            } else if (!isDeadMessage()) {
                runtime.getProvider().publish(new DeadMessage(message));
            }

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

    public boolean hasError() {
        return this.error != null;
    }

    @Override
    public PublicationError getError() {
        return error;
    }

    public void markDispatched() {
        dispatched = true;
    }
    public void markError(PublicationError error) {
        this.error = error;
    }

    public MessagePublication markScheduled() {
        if (state.equals(State.Initial)) {
            state = State.Scheduled;
        }
        return this;
    }

    public boolean isDeadMessage() {
        return DeadMessage.class.equals(message.getClass());
    }

    public boolean isFilteredMessage() {
        return FilteredMessage.class.equals(message.getClass());
    }

    public Object getMessage() {
        return message;
    }

    private enum State {
        Initial, Scheduled, Running, Finished
    }

    public static class Factory {

        public MessagePublication createPublication(BusRuntime runtime, Collection<Subscription> subscriptions, Object message) {
            return new MessagePublication(runtime, subscriptions, message, State.Initial);
        }

    }

}
