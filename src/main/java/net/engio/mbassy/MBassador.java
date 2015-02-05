package net.engio.mbassy;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.LockSupport;

import net.engio.mbassy.bus.AbstractPubSubSupport;
import net.engio.mbassy.bus.error.PublicationError;
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

    /**
     * The stack size is arbitrary based on JVM implementation. Default is 0
     * 8k is the size of the android stack. Depending on the version of android, this can either change, or will always be 8k
     *<p>
     * To be honest, 8k is pretty reasonable for an asynchronous/event based system (32bit) or 16k (64bit)
     * Setting the size MAY or MAY NOT have any effect!!!
     * <p>
     * Stack size must be specified in bytes. Default is 8k
     */
    public static int stackSizeForThreads = 8192;
    private static final ThreadFactory namedThreadFactory = new ThreadFactory() {
        private final AtomicInteger threadID = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("MessageBus-");
            stringBuilder.append(this.threadID.getAndIncrement());

            // stack size is arbitrary based on JVM implementation. Default is 0
            // 8k is the size of the android stack. Depending on the version of android, this can either change, or will always be 8k
            // To be honest, 8k is pretty reasonable for an asynchronous/event based system (32bit) or 16k (64bit)
            // Setting the size MAY or MAY NOT have any effect!!!
            Thread t = new Thread(Thread.currentThread().getThreadGroup(), r, stringBuilder.toString(), stackSizeForThreads);
            t.setDaemon(true);// do not prevent the JVM from exiting
            t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    };


    // any new thread will be 'NON-DAEMON', so that it will be allowed to finish it's task before permitting the JVM to shut down
    private final ExecutorService executor = Executors.newCachedThreadPool(namedThreadFactory);

    private final int ringBufferSize = 2048;

    private final Disruptor<MessageHolder> disruptor;
    private final RingBuffer<MessageHolder> ringBuffer;


    public MBassador() {
        this(Runtime.getRuntime().availableProcessors() - 1);
    }

    // must be power of 2.
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

        // tell the disruptor to handle procs first, then results. IN ORDER.
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
                    .setPublishedObject(message));
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
            handlePublicationError(new PublicationError(e, "Error while adding an asynchronous message", message));
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
                handlePublicationError(new PublicationError(new Exception("Timeout"), "Error while adding an asynchronous message", message));
                return;
            }
        }

        // setup the job
        final long seq = ringBuffer.next();
        try {
            MessageHolder eventJob = ringBuffer.get(seq);
            eventJob.message = message;
        } catch (Exception e) {
            handlePublicationError(new PublicationError(e, "Error while adding an asynchronous message", message));
        } finally {
            // always publish the job
            ringBuffer.publish(seq);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        shutdown();
    }

    @Override
    public void shutdown() {
        this.disruptor.shutdown();
        this.executor.shutdown();
    }

    @Override
    public boolean hasPendingMessages() {
        return this.ringBuffer.remainingCapacity() != this.ringBufferSize;
    }
}
