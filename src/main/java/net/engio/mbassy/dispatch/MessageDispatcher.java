package net.engio.mbassy.dispatch;

import java.lang.reflect.Method;

import net.engio.mbassy.common.ConcurrentSet;

/**
 * Standard implementation for direct, unfiltered message delivery.
 *
 * For each message delivery, this dispatcher iterates over the listeners
 * and uses the previously provided handler invocation to deliver the message
 * to each listener
 *
 * @author bennidi
 *         Date: 11/23/12
 */
public class MessageDispatcher implements IMessageDispatcher {

    private MessagingContext context;

    private IHandlerInvocation invocation;

    public MessageDispatcher(MessagingContext context, IHandlerInvocation invocation) {
        this.context = context;
        this.invocation = invocation;
    }

    @Override
    public void dispatch(Object message, ConcurrentSet listeners) {
        Method handler = getContext().getHandlerMetadata().getHandler();
        for(Object listener: listeners){
            getInvocation().invoke(handler, listener, message);
        }
    }

    public MessagingContext getContext() {
        return context;
    }

    @Override
    public IHandlerInvocation getInvocation() {
        return invocation;
    }
}
