package net.engio.mbassy.listeners;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;

/**
 * @author bennidi
 *         Date: 5/25/13
 */
@Listener(references = References.Strong)
public class ExceptionThrowingListener {


    @Handler()
    public void handle(Object message) {
        throw new RuntimeException("This is an expected exception");
    }


}
