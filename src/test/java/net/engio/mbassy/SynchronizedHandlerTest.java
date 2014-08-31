package net.engio.mbassy;

import net.engio.mbassy.bus.MessagePublication;
import net.engio.mbassy.bus.common.IMessageBus;
import net.engio.mbassy.bus.config.BusConfiguration;
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
 * Todo: Add javadoc
 *
 * @author bennidi
 *         Date: 3/31/13
 */
public class SynchronizedHandlerTest extends MessageBusTest {


    private static int incrementsPerMessage = 10000;
    private static int numberOfMessages = 1000;
    private static int numberOfListeners = 1000;

    @Test
    public void testSynchronizedWithSynchronousInvocation(){
        List<SynchronizedWithSynchronousDelivery> handlers = new LinkedList<SynchronizedWithSynchronousDelivery>();
        IBusConfiguration config = BusConfiguration.SyncAsync();
        config.getFeature(Feature.AsynchronousMessageDispatch.class)
                .setNumberOfMessageDispatchers(6);
        IMessageBus bus = getBus(config);
        for(int i = 0; i < numberOfListeners; i++){
            SynchronizedWithSynchronousDelivery handler = new SynchronizedWithSynchronousDelivery();
            handlers.add(handler);
            bus.subscribe(handler);
        }

        MessagePublication publication = null;
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
        IBusConfiguration config = BusConfiguration.SyncAsync();
        config.getFeature(Feature.AsynchronousMessageDispatch.class)
                .setNumberOfMessageDispatchers(6);
        IMessageBus bus = getBus(config);
        for(int i = 0; i < numberOfListeners; i++){
            SynchronizedWithAsynchronousDelivery handler = new SynchronizedWithAsynchronousDelivery();
            handlers.add(handler);
            bus.subscribe(handler);
        }

        for(int i = 0; i < numberOfMessages; i++){
            bus.post(new Object()).asynchronously();
        }

        // TODO: wait for publication to finish
        pause(10000);

        for(SynchronizedWithAsynchronousDelivery handler : handlers){
            assertEquals(incrementsPerMessage * numberOfMessages, handler.counter);
        }

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
