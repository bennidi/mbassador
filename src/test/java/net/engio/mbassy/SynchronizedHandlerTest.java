package net.engio.mbassy;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import net.engio.mbassy.annotations.Handler;
import net.engio.mbassy.annotations.Synchronized;
import net.engio.mbassy.bus.common.IMessageBus;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.common.MessageBusTest;

import org.junit.Test;

/**
 * Todo: Add javadoc
 *
 * @author bennidi
 *         Date: 3/31/13
 */
public class SynchronizedHandlerTest extends MessageBusTest {

    private static AtomicInteger counter = new AtomicInteger(0);
    private static int numberOfMessages = 1000;
    private static int numberOfListeners = 1000;

    @Test
    public void testSynchronizedWithSynchronousInvocation(){
        List<SynchronizedWithSynchronousDelivery> handlers = new LinkedList<SynchronizedWithSynchronousDelivery>();
        IBusConfiguration config = SyncAsync();
        config.getFeature(Feature.AsynchronousMessageDispatch.class).setNumberOfMessageDispatchers(6);

        IMessageBus bus = createBus(config);
        for(int i = 0; i < numberOfListeners; i++){
            SynchronizedWithSynchronousDelivery handler = new SynchronizedWithSynchronousDelivery();
            handlers.add(handler);
            bus.subscribe(handler);
        }

        for(int i = 0; i < numberOfMessages; i++){
           bus.publishAsync(new Object());
        }

        int totalCount = numberOfListeners * numberOfMessages;
        int expireCount = 1000;

        // wait for last publication
        while (expireCount-- > 0 && counter.get() < totalCount){
            pause(100);
        }

        if (expireCount <= 0) {
            fail("Count '" + counter.get() + "' was incorrect!");
        }
    }

    public static class SynchronizedWithSynchronousDelivery {

        @Handler
        @Synchronized
        public void handleMessage(Object o){
           counter.getAndIncrement();
        }

    }
}
