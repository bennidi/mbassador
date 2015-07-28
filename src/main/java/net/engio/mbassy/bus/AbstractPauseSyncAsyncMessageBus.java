package net.engio.mbassy.bus;

import net.engio.mbassy.bus.common.AsyncPubSubPauseSupport;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.bus.publication.ISyncAsyncPublicationCommand;

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
        if (!isPaused() || countInQueue() == 0) return countInQueue() == 0;

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
    public void resume() {
        resume(PublishMode.SYNC, FlushMode.ATOMIC);
    }

    /**
     * Equivalent to <code>resume(PublishMode.ASYNC, FlushMode.ATOMIC)</code>
     */
    public void resumeAsync() {
        resume(PublishMode.ASYNC, FlushMode.ATOMIC);
    }
}
