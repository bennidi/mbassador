package net.engio.mbassy.bus;

import net.engio.mbassy.bus.common.ErrorHandlingSupport;
import net.engio.mbassy.bus.common.GenericMessagePublicationSupport;
import net.engio.mbassy.bus.common.PubSubSupport;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;
import net.engio.mbassy.bus.publication.IPublicationCommand;

/**
 * A message bus implementation that offers only synchronous message publication. Using this bus
 * will not create any new threads.
 */
public class SyncMessageBus<T> extends AbstractPubSubSupport<T> implements PubSubSupport<T>, ErrorHandlingSupport, GenericMessagePublicationSupport<T,
        SyncMessageBus.SyncPostCommand> {

    /**
     * Default constructor using default setup. super() will also add a default publication error logger
     */
    public SyncMessageBus() {
        super(new BusConfiguration().addFeature(Feature.SyncPubSub.Default()));
    }

    /**
     * Construct with default settings and specified publication error handler
     * @param errorHandler
     */
    public SyncMessageBus(IPublicationErrorHandler errorHandler) {
        super(new BusConfiguration().addFeature(Feature.SyncPubSub.Default()).addPublicationErrorHandler(errorHandler));
    }

    /**
     * Construct with fully specified configuration
     *
     * @param configuration
     */
    public SyncMessageBus(IBusConfiguration configuration) {
        super(configuration);
    }

    @Override
    public IMessagePublication publish(T message) {
        IMessagePublication publication = createMessagePublication(message);
        try {
            publication.execute();
        } catch (Throwable e) {
            handlePublicationError(new PublicationError().setMessage("Error during publication of message")
                                                         .setCause(e)
                                                         .setPublication(publication));
        }
        finally{
            return publication;
        }
    }

    @Override
    public SyncPostCommand post(T message) {
        return new SyncPostCommand(message);
    }

    public class SyncPostCommand implements IPublicationCommand {

        private T message;

        public SyncPostCommand(T message) {
            this.message = message;
        }

        @Override
        public IMessagePublication now() {
            return publish(message);
        }
    }
}
