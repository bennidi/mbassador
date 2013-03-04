package net.engio.mbassy.listeners;

import net.engio.mbassy.events.SubTestMessage;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Mode;

/**
 * @author bennidi
 * Date: 11/22/12
 */
public class EventingTestBean2 extends EventingTestBean{

    // redefine the configuration for this handler
    @Handler(delivery = Mode.Sequential)
    public void handleSubTestEvent(SubTestMessage message) {
        super.handleSubTestEvent(message);
    }
}
