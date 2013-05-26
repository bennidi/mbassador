package net.engio.mbassy.listeners;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;
import net.engio.mbassy.messages.StandardMessage;

/**
 * @author bennidi
 *         Date: 5/25/13
 */
@Listener(references = References.Strong)
public class ExceptionThrowingListener {


    // this handler will be invoked asynchronously
    @Handler()
    public void handle(StandardMessage message) {
        throw new RuntimeException("This is an expected exception");
    }


}
