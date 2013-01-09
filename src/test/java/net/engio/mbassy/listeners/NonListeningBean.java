package net.engio.mbassy.listeners;

import net.engio.mbassy.events.SubTestEvent;
import net.engio.mbassy.events.TestEvent;

/**
 * This bean overrides all the handlers defined in its superclass. Since it does not specify any annotations
 * it should not be considered a message listener
 *
 * @author bennidi
 * Date: 11/22/12
 */
public class NonListeningBean extends EventingTestBean{


    @Override
    public void handleTestEvent(TestEvent event) {
        event.counter.incrementAndGet();   // should never be called
    }

    @Override
    public void handleSubTestEvent(SubTestEvent event) {
        event.counter.incrementAndGet();   // should never be called
    }

    @Override
    public void handleFiltered(SubTestEvent event) {
        event.counter.incrementAndGet();   // should never be called
    }
}
