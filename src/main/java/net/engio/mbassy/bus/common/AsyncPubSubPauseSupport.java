package net.engio.mbassy.bus.common;


public interface AsyncPubSubPauseSupport<T> extends PubSubPauseSupport<T> {

    boolean resume(PublishMode publishMode, FlushMode flushMode);

    public enum PublishMode {
        SYNC,
        ASYNC;
    }
}
