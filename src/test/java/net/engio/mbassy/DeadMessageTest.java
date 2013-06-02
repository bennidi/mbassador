package net.engio.mbassy;

import net.engio.mbassy.bus.BusConfiguration;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.common.*;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listeners.IMessageListener;
import net.engio.mbassy.common.ListenerFactory;
import net.engio.mbassy.listeners.MessagesListener;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Verify correct behaviour in case of message publications that do not have any matching subscriptions
 *
 * @author bennidi
 *         Date: 1/18/13
 */
public class DeadMessageTest extends MessageBusTest{


    @Test
    public void testDeadMessage(){
        final MBassador bus = getBus(BusConfiguration.Default());
        ListenerFactory listeners = new ListenerFactory()
                .create(InstancesPerListener, IMessageListener.DefaultListener.class)
                .create(InstancesPerListener, IMessageListener.AsyncListener.class)
                .create(InstancesPerListener, IMessageListener.DisabledListener.class)
                .create(InstancesPerListener, MessagesListener.DefaultListener.class)
                .create(InstancesPerListener, MessagesListener.AsyncListener.class)
                .create(InstancesPerListener, MessagesListener.DisabledListener.class)
                .create(InstancesPerListener, DeadMessagHandler.class)
                .create(InstancesPerListener, Object.class);


        ConcurrentExecutor.runConcurrent(TestUtil.subscriber(bus, listeners), ConcurrentUnits);

        Runnable publishUnhandledMessage = new Runnable() {
            @Override
            public void run() {
                for(int i=0; i < IterationsPerThread; i++){
                    int variation = i % 3;
                    switch (variation){
                        case 0:bus.publish(new Object());break;
                        case 1:bus.publish(i);break;
                        case 2:bus.publish(String.valueOf(i));break;
                    }
                }

            }
        };

        ConcurrentExecutor.runConcurrent(publishUnhandledMessage, ConcurrentUnits);

        assertEquals(InstancesPerListener * IterationsPerThread * ConcurrentUnits, DeadMessagHandler.deadMessages.get());
    }

    public static class DeadMessagHandler {

        private static final AtomicInteger deadMessages = new AtomicInteger(0);


        @Handler
         public void handle(DeadMessage message){
             deadMessages.incrementAndGet();
         }

    }

}
