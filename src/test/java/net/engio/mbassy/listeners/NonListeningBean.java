package net.engio.mbassy.listeners;

import net.engio.mbassy.events.SubTestMessage;
import net.engio.mbassy.events.TestMessage;
import net.engio.mbassy.listener.Handler;

/**
 * This bean overrides all the handlers defined in its superclass. Since it does not specify any annotations
 * it should not be considered a message listener
 *
 * @author bennidi
 * Date: 11/22/12
 */
public class NonListeningBean extends EventingTestBean{


    @Override
    @Handler(enabled = false)
    public void handleTestEvent(TestMessage message) {
        message.counter.incrementAndGet();   // should never be called
    }

    @Override
    @Handler(enabled = false)
    public void handleSubTestEvent(SubTestMessage message) {
        message.counter.incrementAndGet();   // should never be called
    }

    @Override
    @Handler(enabled = false)
    public void handleFiltered(SubTestMessage message) {
        message.counter.incrementAndGet();   // should never be called
    }
}
