package net.engio.mbassy.bus.common;

/**
 *
 * @author Brian Groenke [groenke.5@osu.edu]
 *
 * @param <T>
 */
public interface AsyncPubSubPauseSupport<T> extends PubSubPauseSupport<T> {

    boolean resume(PublishMode publishMode, FlushMode flushMode);

    public enum PublishMode {
        SYNC,
        ASYNC;
    }
}
