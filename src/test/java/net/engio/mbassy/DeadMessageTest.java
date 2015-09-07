package net.engio.mbassy;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.common.DeadMessage;
import net.engio.mbassy.common.ConcurrentExecutor;
import net.engio.mbassy.common.ListenerFactory;
import net.engio.mbassy.common.MessageBusTest;
import net.engio.mbassy.common.TestUtil;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listeners.IMessageListener;
import net.engio.mbassy.listeners.MessagesTypeListener;
import net.engio.mbassy.listeners.ObjectListener;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Verify correct behaviour in case of message publications that do not have any matching subscriptions
 *
 * @author bennidi
 *         Date: 1/18/13
 */
public class DeadMessageTest extends MessageBusTest{

    @Before
    public void beforeTest(){
        DeadMessagHandler.deadMessages.set(0);
    }


    @Test
    public void testDeadMessage(){
        final MBassador bus = createBus(SyncAsync());
        ListenerFactory listeners = new ListenerFactory()
                .create(InstancesPerListener, IMessageListener.DefaultListener.class)
                .create(InstancesPerListener, IMessageListener.AsyncListener.class)
                .create(InstancesPerListener, IMessageListener.DisabledListener.class)
                .create(InstancesPerListener, MessagesTypeListener.DefaultListener.class)
                .create(InstancesPerListener, MessagesTypeListener.AsyncListener.class)
                .create(InstancesPerListener, MessagesTypeListener.DisabledListener.class)
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



    @Test
    public void testUnsubscribingAllListeners() {
        final MBassador bus = createBus(SyncAsync());
        ListenerFactory deadMessageListener = new ListenerFactory()
                .create(InstancesPerListener, DeadMessagHandler.class)
                .create(InstancesPerListener, Object.class);
        ListenerFactory objectListener = new ListenerFactory()
                .create(InstancesPerListener, ObjectListener.class);
        ConcurrentExecutor.runConcurrent(TestUtil.subscriber(bus, deadMessageListener), ConcurrentUnits);

        // Only dead message handlers available
        bus.post(new Object()).now();

        // The message should be caught as dead message since there are no subscribed listeners
        assertEquals(InstancesPerListener, DeadMessagHandler.deadMessages.get());

        // Clear deadmessage for future tests
        DeadMessagHandler.deadMessages.set(0);

        // Add object listeners and publish again
        ConcurrentExecutor.runConcurrent(TestUtil.subscriber(bus, objectListener), ConcurrentUnits);
        bus.post(new Object()).now();

        // verify that no dead message events were produced
        assertEquals(0, DeadMessagHandler.deadMessages.get());

        // Unsubscribe all object listeners
        ConcurrentExecutor.runConcurrent(TestUtil.unsubscriber(bus, objectListener), ConcurrentUnits);

        // Only dead message handlers available
        bus.post(new Object()).now();

        // The message should be caught, as it's the only listener
        assertEquals(InstancesPerListener, DeadMessagHandler.deadMessages.get());
    }

    public static class DeadMessagHandler {

        private static final AtomicInteger deadMessages = new AtomicInteger(0);

        @Handler
        public void handle(DeadMessage message){
            deadMessages.incrementAndGet();
        }

    }

}
