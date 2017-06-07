package net.engio.mbassy.bus;

import java.util.concurrent.TimeUnit;

import net.engio.mbassy.bus.common.IMessageBus;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;
import net.engio.mbassy.bus.publication.SyncAsyncPostCommand;

public class MBassador<T> extends AbstractPauseSyncAsyncMessageBus<T, SyncAsyncPostCommand<T>>
                      implements IMessageBus<T, SyncAsyncPostCommand<T>> {

    /**
     * Default constructor using default setup. super() will also add a default
     * publication error logger
     */
    public MBassador() {
        this(
            new BusConfiguration()
                .addFeature(Feature.SyncPubSub.Default())
                .addFeature(Feature.AsynchronousHandlerInvocation.Default())
                .addFeature(Feature.AsynchronousMessageDispatch.Default()));
    }

    /**
     * Construct with default settings and specified publication error handler
     *
     * @param errorHandler
     */
    public MBassador(final IPublicationErrorHandler errorHandler) {
        super(
            new BusConfiguration()
                .addFeature(Feature.SyncPubSub.Default())
                .addFeature(Feature.AsynchronousHandlerInvocation.Default())
                .addFeature(Feature.AsynchronousMessageDispatch.Default())
                .addPublicationErrorHandler(errorHandler));
    }

    /**
     * Construct with fully specified configuration
     *
     * @param configuration
     */
    public MBassador(final IBusConfiguration configuration) {
        super(configuration);
    }

    @Override
    public IMessagePublication publishAsync(final T message) {
        final MessagePublication publication = createMessagePublication(message);
        if (isPaused()) {
            super.enqueueMessageOnPause(message);
            return publication;
        }

        return addAsynchronousPublication(publication);
    }

    public IMessagePublication publishAsync(final T message, final long timeout, final TimeUnit unit) {
        final MessagePublication publication = createMessagePublication(message);
        if (isPaused()) {
            super.enqueueMessageOnPause(message);
            return publication;
        }

        return addAsynchronousPublication(publication, timeout, unit);
    }

    /**
     * Synchronously publish a message to all registered listeners (this
     * includes listeners defined for super types) The call blocks until every
     * messageHandler has processed the message.
     *
     * @param message
     */
    @Override
    public IMessagePublication publish(final T message) {
        final MessagePublication publication = createMessagePublication(message);
        if (isPaused()) {
            super.enqueueMessageOnPause(message);
            return publication;
        }

        try {
            publication.execute();
        } catch (final Throwable e) {
            handlePublicationError(
                new PublicationError()
                    .setMessage("Error during publication of message")
                    .setCause(e)
                    .setPublishedMessage(message));
        }

        return publication;
    }

    @Override
    public SyncAsyncPostCommand<T> post(final T message) {
        return new SyncAsyncPostCommand<T>(this, message);
    }

}
