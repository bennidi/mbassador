package net.engio.mbassy.dispatch;

import net.engio.mbassy.IMessageBus;

/**
 * This interface marks components that have access to the message bus that they belong to.
 *
 * @author bennidi
 *         Date: 3/1/13
 */
public interface IMessageBusAware {

    public IMessageBus getBus();
}
