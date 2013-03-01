package net.engio.mbassy.common;

/**
 * The DeadEvent is delivered to all subscribed handlers (if any) whenever no message
 * handlers could be found for a given message publication.
 *
 * @author bennidi
 *         Date: 1/18/13
 */
public class DeadEvent extends PublicationEvent {

    public DeadEvent(Object message) {
        super(message);
    }

}
