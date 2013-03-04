package net.engio.mbassy.common;

/**
 * A wrapped event is created when various conditions are matched (these depend on the concrete
 * (sub)type of wrapped event).
 *
 * @author bennidi
 *         Date: 3/1/13
 */
public abstract class PublicationEvent {

    private Object event;

    public PublicationEvent(Object message) {
        this.event = message;
    }

    public Object getMessage() {
        return event;
    }
}
