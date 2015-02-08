package net.engio.mbassy.listeners;

import net.engio.mbassy.annotations.Handler;
import net.engio.mbassy.annotations.Listener;
import net.engio.mbassy.messages.StandardMessage;

/**
 * @author bennidi
 *         Date: 5/25/13
 */
@Listener()
public class ExceptionThrowingListener {


    // this handler will be invoked asynchronously
    @Handler()
    public void handle(StandardMessage message) {
        throw new RuntimeException("This is an expected exception");
    }


}
