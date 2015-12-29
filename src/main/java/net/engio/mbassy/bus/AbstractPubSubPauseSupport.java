package net.engio.mbassy.bus;

import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import net.engio.mbassy.bus.common.PubSubPauseSupport;
import net.engio.mbassy.bus.config.IBusConfiguration;

/**
 * Top level implementation for message bus pause/resume support. It is the responsibility of all implementation
 * classes to check the current pause state via {@link #isPaused()} and enqueue messages during a pause via
 * {@link #enqueueMessageOnPause(Object)}.
 *
 * @author Brian Groenke [groenke.5@osu.edu]
 *
 * @param <T> the message type
 */
public abstract class AbstractPubSubPauseSupport<T> extends AbstractPubSubSupport<T> implements PubSubPauseSupport<T> {

    private final ConcurrentLinkedQueue<T> msgPauseQueue = new ConcurrentLinkedQueue<T>();
    private final AtomicBoolean paused = new AtomicBoolean();
    private final ReentrantLock pauseLock = new ReentrantLock();

    protected final Publisher<T> syncResumePublisher = new Publisher<T>() {

        @Override
        public void onResume(final T msg) {
            publish(msg);
        }
    };

    public AbstractPubSubPauseSupport(final IBusConfiguration configuration) {
        super(configuration);
    }

    @Override
    public void pause() {
        // return immeidately if already paused
        if (paused.get()) return;
        pauseLock.lock();
        paused.compareAndSet(false, true);
        pauseLock.unlock();
    }

    @Override
    public boolean resume() {
        return resume(FlushMode.ATOMIC);
    }

    @Override
    public boolean resume(final FlushMode flushMode) {
        if (!paused.get() && msgPauseQueue.isEmpty()) return true;

        return resumeAndPublish(flushMode, syncResumePublisher);
    }

    @Override
    public boolean isPaused() {
        return paused.get();
    }

    @Override
    public int countInQueue() {
        return msgPauseQueue.size();
    }

    /**
     * Resumes and publishes all events in the queue according the given
     * <code>flushMode</code> and using the given publisher.
     *
     * @return true if the queue was flushed completely, false otherwise
     */
    protected final boolean resumeAndPublish(final FlushMode flushMode, final Publisher<T> publisher) {
        switch (flushMode) {
        case ATOMIC:
            pauseLock.lock();
            paused.set(false);
            flushPauseQueue(publisher);
            pauseLock.unlock();
            break;
        case NONATOMIC:
            paused.set(false);
            flushPauseQueue(publisher);
            break;
        default:
            throw new IllegalArgumentException("Unrecognized value for " + FlushMode.class.getSimpleName() + ": "
                            + flushMode);
        }

        return msgPauseQueue.isEmpty();
    }

    protected final void flushPauseQueue(final Publisher<T> publisher) {
        while (!paused.get() && !msgPauseQueue.isEmpty()) {
            publisher.onResume(msgPauseQueue.poll());
        }
    }

    protected final void enqueueMessageOnPause(final T msg) {
        if (!isPaused()) return;
        msgPauseQueue.offer(msg);
    }

    protected interface Publisher<T> {

        void onResume(T msg);
    }
}
