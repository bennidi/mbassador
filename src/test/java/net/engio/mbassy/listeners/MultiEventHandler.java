package net.engio.mbassy.listeners;

import net.engio.mbassy.messages.TestMessage;
import net.engio.mbassy.messages.TestMessage2;
import net.engio.mbassy.listener.*;
import net.engio.mbassy.listener.Invoke;
import net.engio.mbassy.subscription.MessageEnvelope;

/**
 * Todo: Add javadoc
 *
 * @author bennidi
 *         Date: 12/12/12
 */
public class MultiEventHandler {


    @Handler(delivery = Invoke.Synchronously)
    @Enveloped(messages = {TestMessage.class, TestMessage2.class})
    public void handleEvents(MessageEnvelope envelope) {
        if(TestMessage.class.isAssignableFrom(envelope.getMessage().getClass())){
            TestMessage message = envelope.getMessage();
            message.counter.incrementAndGet();
        }
        if(envelope.getMessage().getClass().equals(TestMessage2.class)){
            TestMessage2 message = envelope.getMessage();
            message.counter.incrementAndGet();
        }
    }

    @Handler(delivery = Invoke.Synchronously, filters = @Filter(Filters.RejectSubtypes.class))
    @Enveloped(messages = {TestMessage.class, TestMessage2.class})
    public void handleSuperTypeEvents(MessageEnvelope envelope) {
        if(TestMessage.class.isAssignableFrom(envelope.getMessage().getClass())){
            TestMessage message = envelope.getMessage();
            message.counter.incrementAndGet();
        }
        if(envelope.getMessage().getClass().equals(TestMessage2.class)){
            TestMessage2 message = envelope.getMessage();
            message.counter.incrementAndGet();
        }
    }

}
