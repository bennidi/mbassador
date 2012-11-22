package org.mbassy.listeners;

import org.mbassy.events.SubTestEvent;
import org.mbassy.listener.Listener;
import org.mbassy.listener.Mode;

/**
 * @author bennidi
 * Date: 11/22/12
 */
public class EventingTestBean3 extends EventingTestBean2{


    // this handler will be invoked asynchronously
    @Listener(priority = 0, dispatch = Mode.Synchronous)
    public void handleSubTestEventAgain(SubTestEvent event) {
        event.counter.incrementAndGet();
    }

}
