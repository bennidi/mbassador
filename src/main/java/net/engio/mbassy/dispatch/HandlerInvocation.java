package net.engio.mbassy.dispatch;

import net.engio.mbassy.bus.ISyncMessageBus;
import net.engio.mbassy.subscription.AbstractSubscriptionContextAware;
import net.engio.mbassy.subscription.SubscriptionContext;

/**
 * Todo: Add javadoc
 *
 * @author bennidi
 *         Date: 3/29/13
 */
public abstract class HandlerInvocation<Listener, Message> extends AbstractSubscriptionContextAware<ISyncMessageBus> implements IHandlerInvocation<Listener, Message,ISyncMessageBus>{


    public HandlerInvocation(SubscriptionContext context) {
        super(context);
    }
}
