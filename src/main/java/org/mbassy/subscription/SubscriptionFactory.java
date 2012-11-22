package org.mbassy.subscription;

import org.mbassy.IMessageBus;
import org.mbassy.IPublicationErrorHandler;
import org.mbassy.listener.MessageHandlerMetadata;

import java.util.Collection;

/**
 * Created with IntelliJ IDEA.
 * @author bennidi
 * Date: 11/16/12
 * Time: 10:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class SubscriptionFactory {

    private IMessageBus owner;


    public SubscriptionFactory(IMessageBus owner) {
        this.owner = owner;
    }

    public Subscription createSubscription(MessageHandlerMetadata messageHandlerMetadata){
        if(messageHandlerMetadata.isFiltered()){
            if(messageHandlerMetadata.isAsynchronous()){
                return new UnfilteredAsynchronousSubscription(owner, messageHandlerMetadata);
            }
            else{
                return new UnfilteredSynchronousSubscription(owner, messageHandlerMetadata);
            }
        }
        else{
            if(messageHandlerMetadata.isAsynchronous()){
                return new FilteredAsynchronousSubscription(owner, messageHandlerMetadata);
            }
            else{
                return new FilteredSynchronousSubscription(owner, messageHandlerMetadata);
            }
        }
    }
}
