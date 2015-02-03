package net.engio.mbassy.bus;

import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import net.engio.mbassy.bus.common.IMessageBus;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.bus.error.PublicationError;

/**
 * A message bus implementation that offers only synchronous message publication. Using this bus
 * will not create any new threads.
 *
 */
public class SyncMessageBus<T> extends AbstractPubSubSupport<T> implements IMessageBus<T> {


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
                    .setPublishedObject(message));
        }
    }

    @Override
    public IMessagePublication publishAsync(T message) {
        publish(message);
        return null;
    }

    @Override
    public IMessagePublication publishAsync(T message, long timeout, TimeUnit unit) {
        publish(message);
        return null;
    }

    @Override
    public Executor getExecutor() {
        return null;
    }

    @Override
    public boolean hasPendingMessages() {
        return false;
    }

    @Override
    public void shutdown() {
    }
}
