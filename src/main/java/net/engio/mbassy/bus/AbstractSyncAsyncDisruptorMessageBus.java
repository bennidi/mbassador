package net.engio.mbassy.bus;

import com.lmax.disruptor.BatchEventProcessor;
import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.BusySpinWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.EventTranslator;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SequenceBarrier;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;
import net.engio.mbassy.PublicationError;
import net.engio.mbassy.subscription.Subscription;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * The base class for all async message bus implementations.
 *
 * @param <T>
 * @param <P>
 */
public abstract class AbstractSyncAsyncDisruptorMessageBus<T, P extends IMessageBus.IPostCommand> extends AbstractSyncMessageBus<T, P> implements IMessageBus<T, P> {

    // executor for asynchronous message handlers
    private final ExecutorService executor;

    // all pending messages scheduled for asynchronous dispatch are queued here
    private final Disruptor<MessagePublication[]> disruptor;

    public AbstractSyncAsyncDisruptorMessageBus(BusConfiguration configuration) {
        super(configuration);
        this.executor = configuration.getExecutor();
        disruptor = new Disruptor<MessagePublication[]>(new EventFactory<MessagePublication[]>() {
            @Override
            public MessagePublication[] newInstance() {
                return new MessagePublication[1];
            }
        }, Math.min(256, configuration.getMaximumNumberOfPendingMessages()),
                configuration.getExecutor(),
                ProducerType.MULTI,
                new BusySpinWaitStrategy());
        disruptor.handleEventsWith(new EventHandler<MessagePublication[]>() {
            @Override
            public void onEvent(final MessagePublication[] event, final long sequence, final boolean endOfBatch) throws Exception {
                event[0].execute();
            }
        });
        disruptor.start();
    }

    // this method enqueues a message delivery request
    protected MessagePublication addAsynchronousDeliveryRequest(MessagePublication request) {
        final MessagePublication publication = request.markScheduled();
        disruptor.publishEvent(new EventTranslator<MessagePublication[]>() {
            @Override
            public void translateTo(final MessagePublication[] event, final long sequence) {
                event[0] = publication;
            }
        });
        return publication;
    }

    // this method queues a message delivery request
    protected MessagePublication addAsynchronousDeliveryRequest(MessagePublication request, long timeout, TimeUnit unit) {
        return addAsynchronousDeliveryRequest(request);
    }

    @Override
    protected void finalize() throws Throwable {
        shutdown();
        super.finalize();
    }

    public void shutdown() {
        executor.shutdown();
        disruptor.shutdown();
    }

    public boolean hasPendingMessages() {
        return false;
    }

    @Override
    public Executor getExecutor() {
        return executor;
    }

}
