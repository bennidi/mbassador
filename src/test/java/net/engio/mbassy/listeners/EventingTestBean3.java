package net.engio.mbassy.listeners;

import net.engio.mbassy.events.SubTestMessage;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Invoke;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;

/**
 * @author bennidi
 * Date: 11/22/12
 */
@Listener(references = References.Strong)
public class EventingTestBean3 extends EventingTestBean2{


    // this handler will be invoked asynchronously
    @Handler(priority = 0, delivery = Invoke.Synchronously)
    public void handleSubTestEventAgain(SubTestMessage message) {
        message.counter.incrementAndGet();
    }

}
