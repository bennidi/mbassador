package net.engio.mbassy.subscription;

import net.engio.mbassy.IPublicationErrorHandler;
import net.engio.mbassy.bus.BusRuntime;
import net.engio.mbassy.bus.RuntimeProvider;
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
    private final MessageHandler handlerMetadata;

    // error handling is first-class functionality
    private final Collection<IPublicationErrorHandler> errorHandlers;

    private BusRuntime runtime;

    public SubscriptionContext(BusRuntime runtime, MessageHandler handlerMetadata,
                               Collection<IPublicationErrorHandler> errorHandlers) {
        this.runtime = runtime;
        this.handlerMetadata = handlerMetadata;
        this.errorHandlers = errorHandlers;
    }

    /**
     * Get the meta data that specifies the characteristics of the message handler
     * that is associated with this context
     *
     * @return
     */
    public MessageHandler getHandlerMetadata() {
        return handlerMetadata;
    }

    /**
     * Get the error handlers registered with the enclosing bus.
     * @return
     */
    public Collection<IPublicationErrorHandler> getErrorHandlers(){
        return errorHandlers;
    }

    @Override
    public BusRuntime getRuntime() {
        return runtime;
    }

}
