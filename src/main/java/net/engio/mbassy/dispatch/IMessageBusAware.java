package net.engio.mbassy.dispatch;

import net.engio.mbassy.bus.ISyncMessageBus;

/**
 * This interface marks components that have access to the message bus that they belong to.
 *
 * @author bennidi
 *         Date: 3/1/13
 */
public interface IMessageBusAware<Bus extends ISyncMessageBus> {

    Bus getBus();
}
