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
public class EventingTestBean2 extends EventingTestBean{

    // redefine the configuration for this handler
    @Handler(delivery = Invoke.Synchronously)
    public void handleSubTestEvent(SubTestMessage message) {
        super.handleSubTestEvent(message);
    }

}
