package net.engio.mbassy.subscription;

import net.engio.mbassy.dispatch.AsynchronousHandlerInvocation;
import net.engio.mbassy.dispatch.EnvelopedMessageDispatcher;
import net.engio.mbassy.dispatch.FilteredMessageDispatcher;
import net.engio.mbassy.dispatch.IHandlerInvocation;
import net.engio.mbassy.dispatch.IMessageDispatcher;
import net.engio.mbassy.dispatch.MessageDispatcher;
import net.engio.mbassy.dispatch.MessagingContext;
import net.engio.mbassy.dispatch.ReflectiveHandlerInvocation;

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
