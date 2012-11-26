package org.mbassy.subscription;

import org.mbassy.IMessageBus;
import org.mbassy.IPublicationErrorHandler;
import org.mbassy.dispatch.*;
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
        MessagingContext context = new MessagingContext(owner, messageHandlerMetadata);
        IHandlerInvocation invocation = buildInvocationForHandler(context);
        IMessageDispatcher dispatcher = buildDispatcher(context, invocation);
        return new Subscription(context, dispatcher);
    }

    protected IHandlerInvocation buildInvocationForHandler(MessagingContext context){
        IHandlerInvocation invocation = new ReflectiveHandlerInvocation(context);
        if(context.getHandlerMetadata().isAsynchronous()){
            invocation = new AsynchronousHandlerInvocation(invocation);
        }
        return invocation;
    }

    protected IMessageDispatcher buildDispatcher(MessagingContext context, IHandlerInvocation invocation){
       IMessageDispatcher dispatcher = new MessageDispatcher(context, invocation);
       if(context.getHandlerMetadata().isFiltered()){
          dispatcher = new FilteredMessageDispatcher(dispatcher);
       }
       return dispatcher;
    }
}
