package net.engio.mbassy;

import net.engio.mbassy.common.ConcurrentSet;
import net.engio.mbassy.common.DeadEvent;
import net.engio.mbassy.common.UnitTest;
import net.engio.mbassy.listener.Listener;
import org.junit.Test;

/**
 * Verify correct behaviour in case of empty message publications
 *
 * @author bennidi
 *         Date: 1/18/13
 */
public class DeadEventTest extends UnitTest{


    @Test
    public void testDeadEvent(){
        MBassador bus = new MBassador(BusConfiguration.Default());
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

        @Listener
         public void handle(DeadEvent event){
             deadEvents.add(event);
         }


        public int getDeadEventCount(){
            return deadEvents.size();
        }

    }

}
