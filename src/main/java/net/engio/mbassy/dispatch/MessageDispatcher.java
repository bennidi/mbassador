package net.engio.mbassy.dispatch;

import java.lang.reflect.Method;

import net.engio.mbassy.bus.IMessagePublication;
import net.engio.mbassy.common.AbstractConcurrentSet;
import net.engio.mbassy.common.ISetEntry;
import net.engio.mbassy.common.StrongConcurrentSet;
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

    private final IHandlerInvocation invocation;

    public MessageDispatcher(SubscriptionContext context, IHandlerInvocation invocation) {
        super(context);
        this.invocation = invocation;
    }

    @Override
    public void dispatch(final IMessagePublication publication, final Object message, final AbstractConcurrentSet listeners){
        publication.markDelivered();
        IHandlerInvocation invocation = getInvocation();

        ISetEntry<Object> current = listeners.head;
        Object listener;
        while (current != null) {
            listener = current.getValue();
            current = current.next();

            invocation.invoke(listener, message);
        }
    }

    @Override
    public IHandlerInvocation getInvocation() {
        return invocation;
    }
}
