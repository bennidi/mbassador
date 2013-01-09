package net.engio.mbassy.listeners;

import net.engio.mbassy.events.TestEvent;
import net.engio.mbassy.events.TestEvent2;
import net.engio.mbassy.listener.Enveloped;
import net.engio.mbassy.listener.Filter;
import net.engio.mbassy.listener.Filters;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.Mode;
import net.engio.mbassy.subscription.MessageEnvelope;

/**
 * Todo: Add javadoc
 *
 * @author bennidi
 *         Date: 12/12/12
 */
public class MultiEventHandler {


    @Listener(dispatch = Mode.Synchronous)
    @Enveloped(messages = {TestEvent.class, TestEvent2.class})
    public void handleEvents(MessageEnvelope envelope) {
        if(TestEvent.class.isAssignableFrom(envelope.getMessage().getClass())){
            TestEvent event = envelope.getMessage();
            event.counter.incrementAndGet();
        }
        if(envelope.getMessage().getClass().equals(TestEvent2.class)){
            TestEvent2 event = envelope.getMessage();
            event.counter.incrementAndGet();
        }
    }

    @Listener(dispatch = Mode.Synchronous, filters = @Filter(Filters.RejectSubtypes.class))
    @Enveloped(messages = {TestEvent.class, TestEvent2.class})
    public void handleSuperTypeEvents(MessageEnvelope envelope) {
        if(TestEvent.class.isAssignableFrom(envelope.getMessage().getClass())){
            TestEvent event = envelope.getMessage();
            event.counter.incrementAndGet();
        }
        if(envelope.getMessage().getClass().equals(TestEvent2.class)){
            TestEvent2 event = envelope.getMessage();
            event.counter.incrementAndGet();
        }
    }

}
