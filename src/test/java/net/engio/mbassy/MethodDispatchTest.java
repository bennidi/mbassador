package net.engio.mbassy;

import net.engio.mbassy.bus.common.IMessageBus;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.common.MessageBusTest;
import net.engio.mbassy.listener.Handler;
import org.junit.Test;

/**
 * Very simple test to verify dispatch to correct message handler
 *
 * @author bennidi
 *         Date: 1/17/13
 */
public class MethodDispatchTest extends MessageBusTest{

   private boolean listener1Called = false;
   private boolean listener2Called = false;



    // a simple event listener
    public class EventListener1 {

        @Handler
        public void handleString(String s) {
             listener1Called = true;
        }

    }

    // the same handlers as its super class
    public class EventListener2 extends EventListener1 {

        // redefine handler implementation (not configuration)
        public void handleString(String s) {
           listener2Called = true;
        }

    }

    @Test
    public void testDispatch1(){
        IMessageBus bus = getBus(BusConfiguration.SyncAsync());
        EventListener2 listener2 = new EventListener2();
        bus.subscribe(listener2);
        bus.post("jfndf").now();
        assertTrue(listener2Called);
        assertFalse(listener1Called);

        EventListener1 listener1 = new EventListener1();
        bus.subscribe(listener1);
        bus.post("jfndf").now();
        assertTrue(listener1Called);
    }

}
