package net.engio.mbassy;

import net.engio.mbassy.bus.BusConfiguration;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.common.ConcurrentSet;
import net.engio.mbassy.common.DeadMessage;
import net.engio.mbassy.common.MessageBusTest;
import net.engio.mbassy.listener.Handler;
import org.junit.Test;

/**
 * Verify correct behaviour in case of message publications that do not have any matching subscriptions
 *
 * @author bennidi
 *         Date: 1/18/13
 */
public class DeadEventTest extends MessageBusTest{


    @Test
    public void testDeadEvent(){
        MBassador bus = getBus(BusConfiguration.Default());
        DeadEventHandler deadEventHandler = new DeadEventHandler();
        bus.subscribe(deadEventHandler);
        assertEquals(0, deadEventHandler.getDeadEventCount());
        bus.post(new Object()).now();
        assertEquals(1, deadEventHandler.getDeadEventCount());
        bus.post(323).now();
        assertEquals(2, deadEventHandler.getDeadEventCount());
        bus.publish("fkdfdk");
        assertEquals(3, deadEventHandler.getDeadEventCount());
    }

    public class DeadEventHandler{

         private ConcurrentSet deadEvents = new ConcurrentSet();

        @Handler
         public void handle(DeadMessage message){
             deadEvents.add(message);
         }


        public int getDeadEventCount(){
            return deadEvents.size();
        }

    }

}
