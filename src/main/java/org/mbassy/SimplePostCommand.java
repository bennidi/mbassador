package org.mbassy;

/**
* Created with IntelliJ IDEA.
* User: benni
* Date: 11/12/12
* Time: 8:44 PM
* To change this template use File | Settings | File Templates.
*/
public class SimplePostCommand<T> implements IMessageBus.IPostCommand {

    private T message;
    private MBassador mBassador;

    public SimplePostCommand(MBassador mBassador, T message) {
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
