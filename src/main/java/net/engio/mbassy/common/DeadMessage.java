package net.engio.mbassy.common;

/**
 * The DeadEvent is delivered to all subscribed handlers (if any) whenever no message
 * handlers could be found for a given message publication.
 *
 * @author bennidi
 *         Date: 1/18/13
 */
public final class DeadMessage extends PublicationEvent {

    public DeadMessage(Object message) {
        super(message);
    }

}
