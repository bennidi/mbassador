package org.mbassy.listeners;

import org.mbassy.events.SubTestEvent;
import org.mbassy.listener.Listener;
import org.mbassy.listener.Mode;

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
