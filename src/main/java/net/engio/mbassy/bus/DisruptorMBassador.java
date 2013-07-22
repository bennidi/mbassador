package net.engio.mbassy.bus;

import net.engio.mbassy.PublicationError;

import java.util.concurrent.TimeUnit;


public class DisruptorMBassador<T> extends AbstractSyncAsyncDisruptorMessageBus<T,IMessageBus.IPostCommand> {

    public DisruptorMBassador(BusConfiguration configuration) {
        super(configuration);
    }


    public MessagePublication publishAsync(T message) {
        return addAsynchronousDeliveryRequest(createMessagePublication(message));
    }

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
    public IPostCommand post(final T message) {
        return new IPostCommand() {
            @Override
            public MessagePublication asynchronously() {
                return publishAsync(message);
            }

            @Override
            public MessagePublication asynchronously(final long timeout, final TimeUnit unit) {
                return publishAsync(message, timeout, unit);
            }

            @Override
            public void now() {
                publish(message);
            }
        };
    }
}
