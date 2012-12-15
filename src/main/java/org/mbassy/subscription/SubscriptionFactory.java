package org.mbassy.subscription;

import org.mbassy.dispatch.AsynchronousHandlerInvocation;
import org.mbassy.dispatch.EnvelopedMessageDispatcher;
import org.mbassy.dispatch.FilteredMessageDispatcher;
import org.mbassy.dispatch.IHandlerInvocation;
import org.mbassy.dispatch.IMessageDispatcher;
import org.mbassy.dispatch.MessageDispatcher;
import org.mbassy.dispatch.MessagingContext;
import org.mbassy.dispatch.ReflectiveHandlerInvocation;

/**
 * Created with IntelliJ IDEA.
 * @author bennidi
 * Date: 11/16/12
 * Time: 10:39 AM
 * To change this template use File | Settings | File Templates.
 */
public class SubscriptionFactory {

    public Subscription createSubscription(MessagingContext context){
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
       if(context.getHandlerMetadata().isEnveloped()){
          dispatcher = new EnvelopedMessageDispatcher(dispatcher);
       }
       if(context.getHandlerMetadata().isFiltered()){
          dispatcher = new FilteredMessageDispatcher(dispatcher);
       }
       return dispatcher;
    }
}
