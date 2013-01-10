package net.engio.mbassy.listeners;

import net.engio.mbassy.events.SubTestEvent;
import net.engio.mbassy.events.TestEvent;
import net.engio.mbassy.listener.*;

/**
 * Basic bean that defines some event handlers to be used for different unit testting scenarios
 *
 * @author bennidi
 * Date: 11/22/12
 */
public class EventingTestBean {

    // every event of type TestEvent or any subtype will be delivered
    // to this listener
    @Listener
    public void handleTestEvent(TestEvent event) {
        event.counter.incrementAndGet();
    }

    // this handler will be invoked asynchronously
    @Listener(priority = 0, dispatch = Mode.Asynchronous)
    public void handleSubTestEvent(SubTestEvent event) {
        event.counter.incrementAndGet();
    }

    // this handler will receive events of type SubTestEvent
    // or any subtabe and that passes the given filter
    @Listener(
            priority = 10,
            dispatch = Mode.Synchronous,
            filters = {@Filter(Filters.RejectAll.class), @Filter(Filters.AllowAll.class)})
    public void handleFiltered(SubTestEvent event) {
        event.counter.incrementAndGet();
    }


}
