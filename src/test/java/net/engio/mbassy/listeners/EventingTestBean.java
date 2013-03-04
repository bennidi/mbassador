package net.engio.mbassy.listeners;

import net.engio.mbassy.events.SubTestMessage;
import net.engio.mbassy.events.TestMessage;
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
    @Handler
    public void handleTestEvent(TestMessage message) {
        message.counter.incrementAndGet();
    }

    // this handler will be invoked asynchronously
    @Handler(priority = 0, delivery = Mode.Concurrent)
    public void handleSubTestEvent(SubTestMessage message) {
        message.counter.incrementAndGet();
    }

    // this handler will receive events of type SubTestEvent
    // or any subtabe and that passes the given filter
    @Handler(
            priority = 10,
            delivery = Mode.Sequential,
            filters = {@Filter(Filters.RejectAll.class), @Filter(Filters.AllowAll.class)})
    public void handleFiltered(SubTestMessage message) {
        message.counter.incrementAndGet();
    }


}
