package net.engio.mbassy.bus.common;

import net.engio.mbassy.bus.publication.IPublicationCommand;

/**
 * @author bennidi
 *         Date: 3/29/13
 */
public interface ISyncMessageBus<T, P extends IPublicationCommand> extends PubSubSupport<T>, ErrorHandlingSupport, GenericMessagePublicationSupport<T, P>{


}
