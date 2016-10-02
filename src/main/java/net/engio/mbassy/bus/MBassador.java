package net.engio.mbassy.bus;

import net.engio.mbassy.bus.common.IMessageBus;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;
import net.engio.mbassy.bus.publication.SyncAsyncPostCommand;

import java.util.concurrent.TimeUnit;


public class MBassador<T> extends AbstractSyncAsyncMessageBus<T, SyncAsyncPostCommand<T>> implements IMessageBus<T, SyncAsyncPostCommand<T>> {


    /**
     * Default constructor using default setup. super() will also add a default publication error logger
     */
    public MBassador(){
        this(new BusConfiguration()
                .addFeature(Feature.SyncPubSub.Default())
                .addFeature(Feature.AsynchronousHandlerInvocation.Default())
                .addFeature(Feature.AsynchronousMessageDispatch.Default()));
    }

    /**
     * Construct with default settings and specified publication error handler
     *
     * @param errorHandler
     */
    public MBassador(IPublicationErrorHandler errorHandler) {
        super(new BusConfiguration().addFeature(Feature.SyncPubSub.Default())
                                    .addFeature(Feature.AsynchronousHandlerInvocation.Default())
                                    .addFeature(Feature.AsynchronousMessageDispatch.Default())
                                    .addPublicationErrorHandler(errorHandler));
    }

    /**
     * Construct with fully specified configuration
     *
     * @param configuration
     */
    public MBassador(IBusConfiguration configuration) {
        super(configuration);
    }

    public IMessagePublication publishAsync(T message) {
        return addAsynchronousPublication(createMessagePublication(message));
    }

    public IMessagePublication publishAsync(T message, long timeout, TimeUnit unit) {
        return addAsynchronousPublication(createMessagePublication(message), timeout, unit);
    }


    /**
     * Synchronously publish a message to all registered listeners (this includes listeners defined for super types)
     * The call blocks until every messageHandler has processed the message.
     *
     * @param message
     */
    public IMessagePublication publish(T message) {
        IMessagePublication publication = createMessagePublication(message);
        try {
            publication.execute();
        } catch (Throwable e) {
            handlePublicationError(new PublicationError()
                    .setMessage("Error during publication of message")
                    .setCause(e)
                    .setPublication(publication));
        }
        finally{
            return publication;
        }
    }


    @Override
    public SyncAsyncPostCommand<T> post(T message) {
        return new SyncAsyncPostCommand<T>(this, message);
    }

}
