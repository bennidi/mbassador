package net.engio.mbassy;

import junit.framework.Assert;
import net.engio.mbassy.bus.IMessagePublication;
import net.engio.mbassy.bus.common.IMessageBus;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.common.MessageBusTest;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Invoke;
import net.engio.mbassy.listener.Synchronized;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author bennidi
 *         Date: 3/31/13
 */
public class SynchronizedHandlerTest extends MessageBusTest {


    private static int incrementsPerMessage = 1000;
    private static int numberOfMessages = 1000;
    private static int numberOfListeners = 1000;

    @Test
    public void testSynchronizedWithSynchronousInvocation(){
        List<SynchronizedWithSynchronousDelivery> handlers = new LinkedList<SynchronizedWithSynchronousDelivery>();
        IBusConfiguration config = SyncAsync(true);
        config.getFeature(Feature.AsynchronousMessageDispatch.class)
                .setNumberOfMessageDispatchers(6);
        IMessageBus bus = createBus(config);
        for(int i = 0; i < numberOfListeners; i++){
            SynchronizedWithSynchronousDelivery handler = new SynchronizedWithSynchronousDelivery();
            handlers.add(handler);
            bus.subscribe(handler);
        }

        IMessagePublication publication = null;
        for(int i = 0; i < numberOfMessages; i++){
           publication =  bus.post(new Object()).asynchronously();
        }
        // wait for last publication
        while (!publication.isFinished()){
            pause(100);
        }

        for(SynchronizedWithSynchronousDelivery handler : handlers){
            assertEquals(incrementsPerMessage * numberOfMessages, handler.counter);
        }

    }

    @Test
    public void testSynchronizedWithAsSynchronousInvocation(){
        List<SynchronizedWithAsynchronousDelivery> handlers = new LinkedList<SynchronizedWithAsynchronousDelivery>();
        IBusConfiguration config = SyncAsync(true);
        config.getFeature(Feature.AsynchronousMessageDispatch.class)
                .setNumberOfMessageDispatchers(6);
        IMessageBus bus = createBus(config);
        for(int i = 0; i < numberOfListeners; i++){
            SynchronizedWithAsynchronousDelivery handler = new SynchronizedWithAsynchronousDelivery();
            handlers.add(handler);
            bus.subscribe(handler);
        }

        for(int i = 0; i < numberOfMessages; i++){
            track(bus.post(new Object()).asynchronously());
        }

        // Check the handlers processing status
        // Define timeframe in which processing should be finished
        // If not then an error is assumed
        long timeElapsed = 0;
        long timeOut = 30000; // 30 seconds
        long begin =  System.currentTimeMillis();
        while (timeElapsed < timeOut) {
            boolean successful = true;
            for (SynchronizedWithAsynchronousDelivery handler : handlers) {
                successful &= incrementsPerMessage * numberOfMessages ==  handler.counter;
            }
            if(successful)
                break;
            timeElapsed = System.currentTimeMillis() - begin;
        }
        if(timeElapsed >= timeOut) Assert.fail("Processing of handlers unfinished after timeout");

    }



    public static class SynchronizedWithSynchronousDelivery {

        private int counter = 0;

        @Handler
        @Synchronized
        public void handleMessage(Object o){
           for(int i = 0; i < incrementsPerMessage; i++){
               counter++;
           }
        }

    }

    public static class SynchronizedWithAsynchronousDelivery {

        private int counter = 0;

        @Handler(delivery = Invoke.Asynchronously)
        @Synchronized
        public void handleMessage(Object o){
            for(int i = 0; i < incrementsPerMessage; i++){
                counter++;
            }
        }

    }
}
