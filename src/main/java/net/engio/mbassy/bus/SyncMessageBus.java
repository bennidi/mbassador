package net.engio.mbassy.bus;

import net.engio.mbassy.PublicationError;

/**
 * Created with IntelliJ IDEA.
 * User: benjamin
 * Date: 4/3/13
 * Time: 9:02 AM
 * To change this template use File | Settings | File Templates.
 */
public class SyncMessageBus<T> extends AbstractSyncMessageBus<T, SyncMessageBus.SyncPostCommand>{


    public SyncMessageBus(SyncBusConfiguration configuration) {
        super(configuration);
    }

    /**
     * Synchronously publish a message to all registered listeners (this includes listeners defined for super types)
     * The call blocks until every messageHandler has processed the message.
     *
     * @param message
     */
    public void publish(T message) {
        try {
            MessagePublication publication = createMessagePublication(message);
            publication.execute();
        } catch (Throwable e) {
            handlePublicationError(new PublicationError()
                    .setMessage("Error during publication of message")
                    .setCause(e)
                    .setPublishedObject(message));
        }

    }

    @Override
    public SyncPostCommand post(T message) {
        return new SyncPostCommand(message);
    }

    public class SyncPostCommand implements ISyncMessageBus.ISyncPostCommand{


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
