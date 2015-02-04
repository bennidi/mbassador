package net.engio.mbassy.bus;

import java.util.concurrent.TimeUnit;

import net.engio.mbassy.IMessageBus;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.bus.error.PublicationError;


public class MBassador<T> extends AbstractSyncAsyncMessageBus<T> implements IMessageBus<T> {

    public MBassador(IBusConfiguration configuration) {
        super(configuration);
    }

    public MBassador(){
        super(new BusConfiguration()
            .addFeature(Feature.SyncPubSub.Default())
            .addFeature(Feature.AsynchronousHandlerInvocation.Default())
            .addFeature(Feature.AsynchronousMessageDispatch.Default()));
    }

    /**
     * Synchronously publish a message to all registered listeners (this includes listeners defined for super types)
     * The call blocks until every messageHandler has processed the message.
     *
     * @param message
     */
    @Override
    public void publish(T message) {
        try {
            publishMessage(message);
        } catch (Throwable e) {
            handlePublicationError(new PublicationError()
                    .setMessage("Error during publication of message")
                    .setCause(e)
                    .setPublishedObject(message));
        }
    }


    /**
     * Execute the message publication asynchronously. The behaviour of this method depends on the
     * configured queuing strategy:
     * <p/>
     * If an unbound queuing strategy is used the call returns immediately.
     * If a bounded queue is used the call might block until the message can be placed in the queue.
     *
     * @return A message publication that can be used to access information about it's state
     */
    @Override
    public void publishAsync(T message) {
        addAsynchronousPublication(message);
    }


    /**
     * Execute the message publication asynchronously. The behaviour of this method depends on the
     * configured queuing strategy:
     * <p/>
     * If an unbound queuing strategy is used the call returns immediately.
     * If a bounded queue is used the call will block until the message can be placed in the queue
     * or the timeout is reached.
     *
     * @return A message publication that wraps up the publication request
     */
    @Override
    public void publishAsync(T message, long timeout, TimeUnit unit) {
        addAsynchronousPublication(message, timeout, unit);
    }
}
