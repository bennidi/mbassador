package net.engio.mbassy;

import net.engio.mbassy.annotations.Handler;
import net.engio.mbassy.common.MessageBusTest;

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
             MethodDispatchTest.this.listener1Called = true;
        }

    }

    // the same handlers as its super class
    public class EventListener2 extends EventListener1 {

        // redefine handler implementation (not configuration)
        @Override
        public void handleString(String s) {
           MethodDispatchTest.this.listener2Called = true;
        }

    }

    @Test
    public void testDispatch1(){
        IMessageBus bus = createBus(SyncAsync());
        EventListener2 listener2 = new EventListener2();
        bus.subscribe(listener2);
        bus.publish("jfndf");
        assertTrue(this.listener2Called);
        assertFalse(this.listener1Called);

        EventListener1 listener1 = new EventListener1();
        bus.subscribe(listener1);
        bus.publish("jfndf");
        assertTrue(this.listener1Called);
    }

}
