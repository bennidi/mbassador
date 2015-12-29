package net.engio.mbassy.bus;

import net.engio.mbassy.bus.common.AsyncPubSubPauseSupport;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.bus.publication.ISyncAsyncPublicationCommand;

/**
 *
 * @author Brian Groenke [groenke.5@osu.edu]
 *
 */
public abstract class AbstractPauseSyncAsyncMessageBus<T, P extends ISyncAsyncPublicationCommand>
                                                      extends AbstractSyncAsyncMessageBus<T, P>
                                                      implements AsyncPubSubPauseSupport<T> {

    private final Publisher<T> asyncResumePublisher = new Publisher<T>() {
        @Override
        public void onResume(final T msg) {
            publishAsync(msg);
        }
    };

    protected AbstractPauseSyncAsyncMessageBus(final IBusConfiguration configuration) {
        super(configuration);
    }

    protected abstract IMessagePublication publishAsync(T msg);

    @Override
    public boolean resume(final PublishMode publishMode, final FlushMode flushMode) {
        if (!isPaused() && countInQueue() == 0) return true;

        switch (publishMode) {
        case SYNC:
            return resumeAndPublish(flushMode, syncResumePublisher);
        case ASYNC:
            return resumeAndPublish(flushMode, asyncResumePublisher);
        default:
            throw new IllegalArgumentException("Unrecognized value for " + PublishMode.class.getSimpleName() + ": "
                            + publishMode);
        }
    }

    /**
     * Equivalent to <code>resume(PublishMode.SYNC, FlushMode.ATOMIC)</code>
     */
    @Override
    public boolean resume() {
        return resume(PublishMode.SYNC, FlushMode.ATOMIC);
    }

    /**
     * Equivalent to <code>resume(PublishMode.ASYNC, FlushMode.ATOMIC)</code>
     */
    public void resumeAsync() {
        resume(PublishMode.ASYNC, FlushMode.ATOMIC);
    }
}
