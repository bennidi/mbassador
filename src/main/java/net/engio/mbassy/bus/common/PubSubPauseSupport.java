package net.engio.mbassy.bus.common;

public interface PubSubPauseSupport<T> extends PubSubSupport<T> {

    /**
     * Pauses event publishing. All messages submitted via
     * {@link #publish(Object)} will be stored in a queue until
     * {@link #resume()} is called. Any subsequent calls to this method before a
     * call to {@link #resume()} will have no effect. Calls to this method will
     * block until the message queue has been flushed if an atomic resume is in
     * progress.
     */
    void pause();

    /**
     * Resumes event publishing. All messages enqueued since the first call to
     * {@link #pause()} will be subsequently flushed and published via
     * {@link #publish(Object)} in the order that they arrived. Does nothing if
     * the bus is not currently in a paused state from a call to
     * {@link #pause()}.
     *
     * @return true if the queue was flushed completely, false otherwise
     */
    boolean resume(FlushMode flushMode);

    /**
     * Equivalent to <code>resume(FlushMode.ATOMIC);</code>
     */
    void resume();

    /**
     * @return true if this PubSubPauseSupport is currently paused, false
     *         otherwise.
     */
    boolean isPaused();

    /**
     * @return the number of messages currently waiting in the pause queue
     */
    int countInQueue();

    public enum FlushMode {
        ATOMIC,
        NONATOMIC;
    }
}
