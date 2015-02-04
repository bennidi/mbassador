package net.engio.mbassy.bus;


/**
 * The dead message event is published whenever no message
 * handlers could be found for a given message publication.
 *
 * @author bennidi
 *         Date: 1/18/13
 */
public final class DeadMessage {

    private Object relatedMessage;


    DeadMessage(Object message) {
        this.relatedMessage = message;
    }

    public Object getMessage() {
        return this.relatedMessage;
    }
}
