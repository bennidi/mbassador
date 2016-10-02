package net.engio.mbassy.listeners;

import net.engio.mbassy.bus.MessagePublication;
import net.engio.mbassy.dispatch.HandlerInvocation;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;
import net.engio.mbassy.messages.StandardMessage;
import net.engio.mbassy.subscription.SubscriptionContext;

/**
 * @author bennidi
 *         Date: 5/25/13
 */
@Listener(references = References.Strong)
public class CustomInvocationListener {

    @Handler(invocation = HandleSubTestEventInvocation.class)
    public void handle(StandardMessage message) {
        message.handled(this.getClass());
        message.handled(this.getClass());
    }

    public static class HandleSubTestEventInvocation extends HandlerInvocation<CustomInvocationListener, StandardMessage> {

        public HandleSubTestEventInvocation(SubscriptionContext context) {
            super(context);
        }

        @Override
        public void invoke(CustomInvocationListener listener, StandardMessage message, MessagePublication publication) {
            listener.handle(message);
        }
    }

}
