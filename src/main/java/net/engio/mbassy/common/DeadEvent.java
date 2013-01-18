package net.engio.mbassy.common;

/**
 * The DeadEvent is delivered to all subscribed handlers (if any) whenever no message
 * handlers could be found for a given message publication.
 *
 * @author bennidi
 *         Date: 1/18/13
 */
public class DeadEvent {

    private Object event;

    public DeadEvent(Object event) {
        this.event = event;
    }

    public Object getEvent() {
        return event;
    }
}
