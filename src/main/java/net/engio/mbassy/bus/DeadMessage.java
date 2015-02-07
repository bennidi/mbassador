package net.engio.mbassy.bus;

/**
 * The dead message event is published whenever no message
 * handlers could be found for a given message publication.
 *
 * @author bennidi
 *         Date: 1/18/13
 * @author dorkbox, llc
 *         Date: 2/2/15
 */
public final class DeadMessage {

    private Object[] relatedMessages;


    DeadMessage(Object[] messages) {
        this.relatedMessages = messages;
    }

    public Object[] getMessages() {
        return this.relatedMessages;
    }
}
