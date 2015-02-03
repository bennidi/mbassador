package net.engio.mbassy;

import java.util.LinkedList;
import java.util.List;

import net.engio.mbassy.annotations.Handler;
import net.engio.mbassy.annotations.Synchronized;
import net.engio.mbassy.bus.IMessagePublication;
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


    private static int incrementsPerMessage = 10000;
    private static int numberOfMessages = 1000;
    private static int numberOfListeners = 1000;

    @Test
    public void testSynchronizedWithSynchronousInvocation(){
        List<SynchronizedWithSynchronousDelivery> handlers = new LinkedList<SynchronizedWithSynchronousDelivery>();
        IBusConfiguration config = SyncAsync();
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
           publication =  bus.publishAsync(new Object());
        }
        // wait for last publication
        while (!publication.isFinished()){
            pause(100);
        }

        for(SynchronizedWithSynchronousDelivery handler : handlers){
            assertEquals(incrementsPerMessage * numberOfMessages, handler.counter);
        }

    }

    public static class SynchronizedWithSynchronousDelivery {

        private int counter = 0;

        @Handler
        @Synchronized
        public void handleMessage(Object o){
           for(int i = 0; i < incrementsPerMessage; i++){
               this.counter++;
           }
        }

    }
}
