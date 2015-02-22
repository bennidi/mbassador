package net.engio.mbassy.bus;

import net.engio.mbassy.bus.common.ErrorHandlingSupport;
import net.engio.mbassy.bus.common.GenericMessagePublicationSupport;
import net.engio.mbassy.bus.common.PubSubSupport;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.bus.error.PublicationError;
import net.engio.mbassy.bus.publication.IPublicationCommand;

/**
 * A message bus implementation that offers only synchronous message publication. Using this bus
 * will not create any new threads.
 *
 */
public class SyncMessageBus<T> extends AbstractPubSubSupport<T> implements PubSubSupport<T>, ErrorHandlingSupport, GenericMessagePublicationSupport<T, SyncMessageBus.SyncPostCommand>{


    public SyncMessageBus(IBusConfiguration configuration) {
        super(configuration);
    }

    @Override
    public void publish(T message) {
        try {
            IMessagePublication publication = createMessagePublication(message);
            publication.execute();
        } catch (Throwable e) {
            handlePublicationError(new PublicationError()
                    .setMessage("Error during publication of message")
                    .setCause(e)
                    .setPublishedMessage(message));
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
        public void now() {
            publish(message);
        }
    }
}
