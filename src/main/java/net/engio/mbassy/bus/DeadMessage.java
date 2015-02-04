package net.engio.mbassy.bus;

import net.engio.mbassy.bus.common.PublicationEvent;

/**
 * The dead message event is published whenever no message
 * handlers could be found for a given message publication.
 *
 * @author bennidi
 *         Date: 1/18/13
 */
public final class DeadMessage extends PublicationEvent {

    DeadMessage(Object message) {
        super(message);
    }
}
