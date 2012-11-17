package org.mbassy.subscription;

import org.mbassy.IMessageBus;
import org.mbassy.IPublicationErrorHandler;
import org.mbassy.MBassador;
import org.mbassy.listener.MessageHandlerMetadata;

import java.lang.reflect.Method;
import java.util.Collection;

/**
* Created with IntelliJ IDEA.
* User: benni
* Date: 11/14/12
* Time: 3:49 PM
* To change this template use File | Settings | File Templates.
*/
public class UnfilteredSynchronousSubscription extends UnfilteredSubscription{

    public UnfilteredSynchronousSubscription(IMessageBus mBassador, MessageHandlerMetadata messageHandler) {
        super(mBassador, messageHandler);
    }

    protected void dispatch(final Object message, final Object listener){
        invokeHandler(message, listener);
    }
}
