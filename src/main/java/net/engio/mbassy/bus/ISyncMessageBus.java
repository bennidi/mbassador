package net.engio.mbassy.bus;

import net.engio.mbassy.IPublicationErrorHandler;

import java.util.Collection;

/**
 *
 *
 * @author bennidi
 *         Date: 3/29/13
 */
public interface ISyncMessageBus<T, P extends IMessageBus.ISyncPostCommand> {


    /**
     * Subscribe all listeners of the given message to receive message publications.
     * Any message may only be subscribed once (subsequent subscriptions of an already subscribed
     * message will be silently ignored)
     *
     * @param listener
     */
    void subscribe(Object listener);

    /**
     * Immediately remove all registered message handlers (if any) of the given listener. When this call returns all handlers
     * have effectively been removed and will not receive any message publications (including asynchronously scheduled
     * publications that have been published when the message listener was still subscribed).
     * <p/>
     * A call to this method passing null, an already unsubscribed listener or any object that does not define any message
     * handlers will not have any effect and is silently ignored.
     *
     * @param listener
     * @return true, if the listener was found and successfully removed
     *         false otherwise
     */
    boolean unsubscribe(Object listener);

    /**
     * @param message
     * @return
     */
    P post(T message);

    /**
     * Publication errors may occur at various points of time during message delivery. A handler may throw an exception,
     * may not be accessible due to security constraints or is not annotated properly.
     * In any of all possible cases a publication error is created and passed to each of the registered error handlers.
     * A call to this method will add the given error handler to the chain
     *
     * @param errorHandler
     */
    void addErrorHandler(IPublicationErrorHandler errorHandler);

    /**
     * Returns an immutable collection containing all the registered error handlers
     *
     * @return
     */
    Collection<IPublicationErrorHandler> getRegisteredErrorHandlers();



    /**
     * A post command is used as an intermediate object created by a call to the message bus' post method.
     * It encapsulates the functionality provided by the message bus that created the command.
     * Subclasses may extend this interface and add functionality, e.g. different dispatch schemes.
     */
    interface ISyncPostCommand {

        /**
         * Execute the message publication immediately. This call blocks until every matching message handler
         * has been invoked.
         */
        void now();
    }
}
