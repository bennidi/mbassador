package net.engio.mbassy.bus.common;

import net.engio.mbassy.bus.publication.IPublicationCommand;

/**
 * This interface is meant to be implemented by different bus implementations to offer a consistent way
 * to plugin different methods of message publication.
 *
 * The parametrization of the IPostCommand influences which publication methods (asynchronous, synchronous or
 * conditional etc.) are available.
 *
 */
public interface GenericMessagePublicationSupport<T, P extends IPublicationCommand> extends PubSubSupport<T>, ErrorHandlingSupport{

    /**
     * Publish a message to the bus using on of its supported message publication mechanisms. The supported
     * mechanisms depend on the available implementation and are exposed as subclasses of IPublicationCommand.
     * The standard mechanism is the synchronous dispatch which will publish the message in the current thread
     * and returns after every matching handler has been invoked.
     *
     * @param message - Any subtype of T welcome
     * @return An object that provides access to the available publication methods supported by the message bus.
     */
    P post(T message);

}
