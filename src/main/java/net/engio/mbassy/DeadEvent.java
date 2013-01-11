package net.engio.mbassy;

/**
 * the DeadEvent which may be catched if needed
 */
public class DeadEvent {
    private Object event;

    public DeadEvent(Object event) {
	this.event = event;
    }

    public Object getEvent() {
	return this.event;
    }
}
