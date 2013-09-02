package net.engio.mbassy.bus;

import net.engio.mbassy.PublicationError;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.publication.SyncAsyncPostCommand;

import java.util.concurrent.TimeUnit;


public class MBassador<T> extends AbstractSyncAsyncMessageBus<T, SyncAsyncPostCommand<T>> implements IMBassador<T> {

    public MBassador(BusConfiguration configuration) {
        super(configuration);
    }


    @Override
    public MessagePublication publishAsync(T message) {
        return addAsynchronousDeliveryRequest(createMessagePublication(message));
    }

    @Override
    public MessagePublication publishAsync(T message, long timeout, TimeUnit unit) {
        return addAsynchronousDeliveryRequest(createMessagePublication(message), timeout, unit);
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
    public SyncAsyncPostCommand<T> post(T message) {
        return new SyncAsyncPostCommand<T>(this, message);
    }

}
