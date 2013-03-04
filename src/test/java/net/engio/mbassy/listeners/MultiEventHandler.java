package net.engio.mbassy.listeners;

import net.engio.mbassy.events.TestMessage;
import net.engio.mbassy.events.TestMessage2;
import net.engio.mbassy.listener.Enveloped;
import net.engio.mbassy.listener.Filter;
import net.engio.mbassy.listener.Filters;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Mode;
import net.engio.mbassy.subscription.MessageEnvelope;

/**
 * Todo: Add javadoc
 *
 * @author bennidi
 *         Date: 12/12/12
 */
public class MultiEventHandler {


    @Handler(delivery = Mode.Sequential)
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

    @Handler(delivery = Mode.Sequential, filters = @Filter(Filters.RejectSubtypes.class))
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
