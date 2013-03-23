package net.engio.mbassy.dispatch;

import net.engio.mbassy.bus.MessagePublication;
import net.engio.mbassy.common.ConcurrentSet;
import net.engio.mbassy.subscription.AbstractSubscriptionContextAware;
import net.engio.mbassy.subscription.SubscriptionContext;

/**
 * Standard implementation for direct, unfiltered message delivery.
 * <p/>
 * For each message delivery, this dispatcher iterates over the listeners
 * and uses the previously provided handler invocation to deliver the message
 * to each listener
 *
 * @author bennidi
 *         Date: 11/23/12
 */
public class MessageDispatcher extends AbstractSubscriptionContextAware implements IMessageDispatcher {

    private IHandlerInvocation invocation;

    public MessageDispatcher(SubscriptionContext context, IHandlerInvocation invocation) {
        super(context);
        this.invocation = invocation;
    }

    @Override
    public void dispatch(MessagePublication publication, Object message, ConcurrentSet listeners) {
        publication.markDelivered();
        for (Object listener : listeners) {
            getInvocation().invoke(listener, message);
        }
    }

    @Override
    public IHandlerInvocation getInvocation() {
        return invocation;
    }
}
