package net.engio.mbassy.bus.common;

/**
 * A filtered message event is published when there have been matching subscriptions for a given
 * message publication but configured filters prevented the message from being delivered to
 * any of the handlers.
 *
 * @author bennidi
 *         Date: 3/1/13
 */
public final class FilteredMessage extends PublicationEvent {


    public FilteredMessage(Object event) {
        super(event);
    }
}
