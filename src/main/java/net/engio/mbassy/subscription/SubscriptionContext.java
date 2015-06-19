package net.engio.mbassy.subscription;

import net.engio.mbassy.bus.BusRuntime;
import net.engio.mbassy.bus.common.RuntimeProvider;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;
import net.engio.mbassy.listener.MessageHandler;

import java.util.Collection;

/**
 * The subscription context holds all (meta)data/objects that are relevant to successfully publish
 * a message within a subscription. A one-to-one relation between a subscription and
 * subscription context holds -> a subscription context is created for each distinct subscription
 * managed by the subscription manager.
 *
 * @author bennidi
 *         Date: 11/23/12
 */
public class SubscriptionContext implements RuntimeProvider {

    // the handler's metadata -> for each handler in a listener, a unique subscription context is created
    private final MessageHandler handler;

    // error handling is first-class functionality
    private final Collection<IPublicationErrorHandler> errorHandlers;

    private final BusRuntime runtime;

    public SubscriptionContext(final BusRuntime runtime, final MessageHandler handler,
                               final Collection<IPublicationErrorHandler> errorHandlers) {
        this.runtime = runtime;
        this.handler = handler;
        this.errorHandlers = errorHandlers;
    }

    /**
     * Get the meta data that specifies the characteristics of the message handler
     * that is associated with this context
     */
    public MessageHandler getHandler() {
        return handler;
    }

    /**
     * Get the error handlers registered with the enclosing bus.
     */
    public Collection<IPublicationErrorHandler> getErrorHandlers(){
        return errorHandlers;
    }

    @Override
    public BusRuntime getRuntime() {
        return runtime;
    }

    public final void handleError(PublicationError error){
        for (IPublicationErrorHandler errorHandler : errorHandlers) {
            errorHandler.handleError(error);
        }
    }

}
