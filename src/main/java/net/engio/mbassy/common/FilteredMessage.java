package net.engio.mbassy.common;

/**
 * A filtered event is published when there have been matching subscriptions for a given
 * message publication but configured filters prevented the message from being delivered to
 * any of the handlers.
 *
 * @author bennidi
 *         Date: 3/1/13
 */
public class FilteredMessage extends PublicationEvent {


    public FilteredMessage(Object event) {
        super(event);
    }
}
