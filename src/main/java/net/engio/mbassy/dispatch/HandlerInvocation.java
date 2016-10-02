package net.engio.mbassy.dispatch;

import net.engio.mbassy.bus.MessagePublication;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;
import net.engio.mbassy.subscription.AbstractSubscriptionContextAware;
import net.engio.mbassy.subscription.SubscriptionContext;

import java.util.Collection;

/**
 * This is the base class for handler invocations that already implements all context related methods only leaving the implementation of the actual invocation mechanism to the concrete subclass.
 *
 * @author bennidi
 *         Date: 3/29/13
 */
public abstract class HandlerInvocation<HANDLER, MESSAGE> extends AbstractSubscriptionContextAware implements IHandlerInvocation<HANDLER, MESSAGE>{


    public HandlerInvocation(SubscriptionContext context) {
        super(context);
    }

    protected final void handlePublicationError(MessagePublication publication, PublicationError error){
        publication.markError(error);
        getContext().handleError(error);
    }
}
