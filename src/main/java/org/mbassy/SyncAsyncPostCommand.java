package org.mbassy;

/**
 * This post command provides access to standard synchronous and asynchronous dispatch
 *
 * @author bennidi
 *         Date: 11/12/12
 */
public class SyncAsyncPostCommand<T> implements IMessageBus.IPostCommand {

    private T message;
    private MBassador<T> mBassador;

    public SyncAsyncPostCommand(MBassador<T> mBassador, T message) {
        this.mBassador = mBassador;
        this.message = message;
    }

    @Override
    public void now() {
        mBassador.publish(message);
    }

    @Override
    public void asynchronously() {
        mBassador.publishAsync(message);
    }
}
