package net.engio.mbassy.bus.common;

/**
 * This interface defines the necessary semantics for implementing synchronous pause/resume support in a message bus.
 * Pause/resume allows for publication to be suspended (on calls to {@link #pause()}) from live execution until a
 * subsequent call to {@link #resume()}. Resuming the message bus will publish all pending messages via the
 * implementation's {@link #publish(Object)} method.
 *
 * @author Brian Groenke [groenke.5@osu.edu]
 *
 * @param <T>
 *            the message type
 */
public interface PubSubPauseSupport<T> extends PubSubSupport<T> {

    /**
     * Pauses event publishing. All messages submitted via {@link #publish(Object)} will be stored in a queue until
     * {@link #resume()} is called. Any subsequent calls to this method before a call to {@link #resume()} will have no
     * effect. Calls to this method will block until the message queue has been flushed if an atomic resume is in
     * progress.
     */
    void pause();

    /**
     * Resumes event publishing. All messages enqueued since the first call to {@link #pause()} will be subsequently
     * flushed and published via {@link #publish(Object)} in the order that they arrived. Atomic mode will cause all
     * subsequent calls to <code>pause()</code> to block until all pending messages in the queue are published;
     * non-atomic mode will flush the queue until a) another thread calls the {@link #pause()} method or b) all messages
     * in the queue are published. This method does nothing if the bus is not currently in a paused state from a call to
     * {@link #pause()}.
     *
     * @param flushMode
     *            the synchronization mode to use when flushing the pause queue
     * @return true if the queue was flushed completely, false otherwise
     */
    boolean resume(FlushMode flushMode);

    /**
     * Equivalent to <code>resume(FlushMode.ATOMIC);</code>
     */
    boolean resume();

    /**
     * @return true if this PubSubPauseSupport is currently paused, false otherwise.
     */
    boolean isPaused();

    /**
     * @return the number of messages currently waiting in the pause queue
     */
    int countInQueue();

    /**
     * FlushMode defines the flushing behavior of the <code>resume(FlushMode)</code> method.
     */
    public enum FlushMode {
        ATOMIC,
        NONATOMIC;
    }
}
