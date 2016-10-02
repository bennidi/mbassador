package net.engio.mbassy.bus;

import net.engio.mbassy.bus.error.PublicationError;
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

    void execute();

    boolean isFinished();

    boolean isRunning();

    boolean isScheduled();

    boolean hasError();

    PublicationError getError();

    boolean isDeadMessage();

    boolean isFilteredMessage();

    Object getMessage();

}
