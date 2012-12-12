package org.mbassy.dispatch;

import org.mbassy.common.ConcurrentSet;
import org.mbassy.subscription.MessageEnvelope;

/**
 * Todo: Add javadoc
 *
 * @author bennidi
 *         Date: 12/12/12
 */
public class EnvelopedMessageDispatcher implements IMessageDispatcher {

    private IMessageDispatcher del;

    public EnvelopedMessageDispatcher(IMessageDispatcher dispatcher) {
        this.del = dispatcher;
    }

    @Override
    public void dispatch(Object message, ConcurrentSet listeners) {
        del.dispatch(new MessageEnvelope(message), listeners);
    }

    @Override
    public MessagingContext getContext() {
        return del.getContext();
    }

    @Override
    public IHandlerInvocation getInvocation() {
        return del.getInvocation();
    }
}
