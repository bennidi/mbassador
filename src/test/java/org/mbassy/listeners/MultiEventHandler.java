package org.mbassy.listeners;

import org.mbassy.events.SubTestEvent;
import org.mbassy.events.TestEvent;
import org.mbassy.events.TestEvent2;
import org.mbassy.listener.*;
import org.mbassy.subscription.MessageEnvelope;

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
