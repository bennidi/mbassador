package org.mbassy.subscription;

import org.mbassy.IMessageBus;
import org.mbassy.IPublicationErrorHandler;
import org.mbassy.MBassador;
import org.mbassy.listener.MessageFilter;
import org.mbassy.listener.MessageHandlerMetadata;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Iterator;

/**
* Created with IntelliJ IDEA.
* @author bennidi
* Date: 11/14/12
* Time: 3:48 PM
* To change this template use File | Settings | File Templates.
*/
public abstract class FilteredSubscription extends Subscription{

    private final MessageFilter[] filter;


    public FilteredSubscription(IMessageBus mBassador, MessageHandlerMetadata messageHandler) {
        super(mBassador, messageHandler);
        this.filter = messageHandler.getFilter();
    }

    private boolean passesFilter(Object message, Object listener) {

        if (filter == null) {
            return true;
        }
        else {
            for (int i = 0; i < filter.length; i++) {
                if (!filter[i].accepts(message, listener)) return false;
            }
            return true;
        }
    }

    public void publish(Object message) {

        Iterator<Object> iterator = listeners.iterator();
        Object listener = null;
        while ((listener = iterator.next()) != null) {
            if(passesFilter(message, listener)) {
                dispatch(message, listener);
            }
        }
    }
}
