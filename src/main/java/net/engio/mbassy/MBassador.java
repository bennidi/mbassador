package net.engio.mbassy;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import net.engio.mbassy.bus.AbstractPubSubSupport;
import net.engio.mbassy.bus.error.PublicationError;
import net.engio.mbassy.common.DisruptorThreadFactory;
import net.engio.mbassy.disruptor.EventBusFactory;
import net.engio.mbassy.disruptor.EventProcessor;
import net.engio.mbassy.disruptor.MessageHolder;

import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.SleepingWaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

/**
 * The base class for all message bus implementations with support for asynchronous message dispatch
 */
public class MBassador extends AbstractPubSubSupport implements IMessageBus {

    // any new thread will be 'NON-DAEMON', so that it will be forced to finish it's task before permitting the JVM to shut down
    private final ExecutorService executor = Executors.newCachedThreadPool(new DisruptorThreadFactory());

    // must be power of 2.
    private final int ringBufferSize = 2048;

    private final Disruptor<MessageHolder> disruptor;
    private final RingBuffer<MessageHolder> ringBuffer;


    public MBassador() {
        this(Runtime.getRuntime().availableProcessors() - 1);
    }


    public MBassador(int numberOfThreads) {
        super();

        if (numberOfThreads < 1) {
            numberOfThreads = 1; // at LEAST 1 thread.
        }

        EventBusFactory factory = new EventBusFactory();
        EventProcessor procs[] = new EventProcessor[numberOfThreads];
        for (int i = 0; i < procs.length; i++) {
            procs[i] = new EventProcessor(this, i, procs.length);
        }

        this.disruptor = new Disruptor<MessageHolder>(factory, this.ringBufferSize, this.executor, ProducerType.MULTI, new SleepingWaitStrategy());

        // tell the disruptor to handle procs first
        this.disruptor.handleEventsWith(procs);
        this.ringBuffer = this.disruptor.start();
    }


    @Override
    public void publish(Object message) {
        try {
            publishMessage(message);
        } catch (Throwable e) {
            handlePublicationError(new PublicationError()
                    .setMessage("Error during publication of message")
                    .setCause(e)
                    .setPublishedObject(new Object[] {message}));
        }
    }

    @Override
    public void publish(Object message1, Object message2) {
        try {
            publishMessage(message1, message2);
        } catch (Throwable e) {
            handlePublicationError(new PublicationError()
                    .setMessage("Error during publication of message")
                    .setCause(e)
                    .setPublishedObject(new Object[] {message1, message2}));
        }
    }

    @Override
    public void publish(Object message1, Object message2, Object message3) {
        try {
            publishMessage(message1, message2, message3);
        } catch (Throwable e) {
            handlePublicationError(new PublicationError()
            .setMessage("Error during publication of message")
            .setCause(e)
            .setPublishedObject(new Object[] {message1, message2, message3}));
        }
    }

    @Override
    public void publish(Object... messages) {
        try {
            publishMessage(messages);
        } catch (Throwable e) {
            handlePublicationError(new PublicationError()
                    .setMessage("Error during publication of message")
                    .setCause(e)
                    .setPublishedObject(messages));
        }
    }






    @Override
    public void publishAsync(Object message) {
        // put this on the disruptor ring buffer
        final RingBuffer<MessageHolder> ringBuffer = this.ringBuffer;

        // setup the job
        final long seq = ringBuffer.next();
        try {
            MessageHolder eventJob = ringBuffer.get(seq);
            eventJob.message = message;
        } catch (Exception e) {
            handlePublicationError(new PublicationError()
                                        .setMessage("Error while adding an asynchronous message")
                                        .setCause(e)
                                        .setPublishedObject(new Object[] {message}));
        } finally {
            // always publish the job
            ringBuffer.publish(seq);
        }
    }

    @Override
    public void publishAsync(long timeout, TimeUnit unit, Object message) {
        // put this on the disruptor ring buffer
        final RingBuffer<MessageHolder> ringBuffer = this.ringBuffer;
        final long expireTimestamp = TimeUnit.MILLISECONDS.convert(timeout, unit) + System.currentTimeMillis();

        // Inserts the specified element into this buffer, waiting up to the specified wait time if necessary for space
        // to become available.
        while (!ringBuffer.hasAvailableCapacity(1)) {
            LockSupport.parkNanos(10L);
            if (expireTimestamp <= System.currentTimeMillis()) {
                handlePublicationError(new PublicationError()
                                            .setMessage("Error while adding an asynchronous message")
                                            .setCause(new Exception("Timeout"))
                                            .setPublishedObject(new Object[] {message}));
                return;
            }
        }

        // setup the job
        final long seq = ringBuffer.next();
        try {
            MessageHolder eventJob = ringBuffer.get(seq);
            eventJob.message = message;
        } catch (Exception e) {
            handlePublicationError(new PublicationError()
                                        .setMessage("Error while adding an asynchronous message")
                                        .setCause(e)
                                        .setPublishedObject(new Object[] {message}));
        } finally {
            // always publish the job
            ringBuffer.publish(seq);
        }
    }

    @Override
    public boolean hasPendingMessages() {
        return this.ringBuffer.remainingCapacity() != this.ringBufferSize;
    }

    @Override
    public void shutdown() {
        this.disruptor.shutdown();
        this.executor.shutdown();
    }
}
