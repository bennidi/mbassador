package org.mbassy.subscription;

import org.mbassy.IMessageBus;
import org.mbassy.IPublicationErrorHandler;
import org.mbassy.MBassador;
import org.mbassy.listener.MessageHandlerMetadata;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;

/**
* Created with IntelliJ IDEA.
* @author bennidi
* Date: 11/14/12
* Time: 3:45 PM
* To change this template use File | Settings | File Templates.
*/
public abstract class UnfilteredSubscription extends Subscription{


    public UnfilteredSubscription(IMessageBus mBassador, MessageHandlerMetadata messageHandler) {
        super(mBassador, messageHandler);
    }

    public void publish(Object message) {

        Iterator<Object> iterator = listeners.iterator();
        Object listener = null;
        while ((listener = iterator.next()) != null) {
            dispatch(message, listener);
        }
    }
}
