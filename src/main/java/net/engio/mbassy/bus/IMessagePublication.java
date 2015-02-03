package net.engio.mbassy.bus;

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
public interface IMessagePublication {

    public boolean add(Subscription subscription);

    /*
    TODO: document state transitions
     */
    public void execute();

    public boolean isFinished();

    public boolean isRunning();

    public boolean isScheduled();

    public void markDelivered();

    public IMessagePublication markScheduled();

    public boolean isDeadEvent();

    public Object getMessage();
}
