package net.engio.mbassy.listeners;

import net.engio.mbassy.events.SubTestEvent;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.Mode;

/**
 * @author bennidi
 * Date: 11/22/12
 */
public class EventingTestBean2 extends EventingTestBean{

    // redefine the configuration for this handler
    @Listener(dispatch = Mode.Synchronous)
    public void handleSubTestEvent(SubTestEvent event) {
        super.handleSubTestEvent(event);
    }
}
