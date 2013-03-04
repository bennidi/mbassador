package net.engio.mbassy.listeners;

import net.engio.mbassy.events.SubTestMessage;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Mode;

/**
 * @author bennidi
 * Date: 11/22/12
 */
public class EventingTestBean3 extends EventingTestBean2{


    // this handler will be invoked asynchronously
    @Handler(priority = 0, delivery = Mode.Sequential)
    public void handleSubTestEventAgain(SubTestMessage message) {
        message.counter.incrementAndGet();
    }

}
