package org.mbassy.subscription;

import org.mbassy.IMessageBus;
import org.mbassy.IPublicationErrorHandler;
import org.mbassy.listener.MessageFilter;
import org.mbassy.listener.MessageHandlerMetadata;

import java.lang.reflect.Method;
import java.util.Collection;

/**
* Created with IntelliJ IDEA.
* @author bennidi
* Date: 11/14/12
* Time: 3:50 PM
* To change this template use File | Settings | File Templates.
*/
public class FilteredAsynchronousSubscription extends FilteredSubscription{

    public FilteredAsynchronousSubscription(IMessageBus mBassador, MessageHandlerMetadata messageHandler) {
        super(mBassador, messageHandler);
    }

    protected void dispatch(final Object message, final Object listener){

        getMessageBus().getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                invokeHandler(message, listener);
            }
        });
    }
}
