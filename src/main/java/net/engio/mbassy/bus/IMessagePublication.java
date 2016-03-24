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

    boolean add(Subscription subscription);  // TODO: this method should not be part of the interface

    /*
    TODO: document state transitions
     */
    void execute();

    boolean isFinished();

    boolean isRunning();

    boolean isScheduled();

    void markDelivered(); // TODO: this method should not be part of the interface

    IMessagePublication markScheduled(); // TODO: this method should not be part of the interface

    void markCancelled();

    boolean isDeadMessage();

    boolean isFilteredMessage();

    Object getMessage();


    // TODO: This interface should only be used as return type to public API calls (clients). Internally the
    // implementation
    // of the interface should be used. This would allow to remove the unwanted methods from this interface.
}
