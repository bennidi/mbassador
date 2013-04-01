package net.engio.mbassy;

import net.engio.mbassy.bus.BusConfiguration;
import net.engio.mbassy.bus.IMessageBus;
import net.engio.mbassy.bus.MessagePublication;
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


    private static int incrementsPerHandler = 10000;
    private static int numberOfMessages = 1000;
    private static int numberOfHandlers = 1000;

    @Test
    public void testSynchronizedWithSynchronousInvocation(){
        List<SynchronizedMessageHandlerSync> handlers = new LinkedList<SynchronizedMessageHandlerSync>();
        IMessageBus bus = getBus(BusConfiguration.Default()
                .setNumberOfMessageDispatchers(6));
        for(int i = 0; i < numberOfHandlers; i++){
            SynchronizedMessageHandlerSync handler = new SynchronizedMessageHandlerSync();
            handlers.add(handler);
            bus.subscribe(handler);
        }

        MessagePublication publication = null;
        for(int i = 0; i < numberOfMessages; i++){
           publication =  bus.post(new Object()).asynchronously();
        }
        while (!publication.isFinished()){
            pause(2000);
        }

        for(SynchronizedMessageHandlerSync handler : handlers){
            assertEquals(incrementsPerHandler * numberOfMessages, handler.Counter);
        }

    }

    @Test
    public void testSynchronizedWithAsSynchronousInvocation(){
        List<SynchronizedMessageHandlerAsyn> handlers = new LinkedList<SynchronizedMessageHandlerAsyn>();
        IMessageBus bus = getBus(BusConfiguration.Default()
                .setNumberOfMessageDispatchers(6));
        for(int i = 0; i < numberOfHandlers; i++){
            SynchronizedMessageHandlerAsyn handler = new SynchronizedMessageHandlerAsyn();
            handlers.add(handler);
            bus.subscribe(handler);
        }

        for(int i = 0; i < numberOfMessages; i++){
            bus.post(new Object()).asynchronously();
        }

        pause(10000);

        for(SynchronizedMessageHandlerAsyn handler : handlers){
            assertEquals(incrementsPerHandler * numberOfMessages, handler.Counter);
        }

    }

    public static class SynchronizedMessageHandlerSync{

        private int Counter = 0;

        @Handler
        @Synchronized
        public void handleMessage(Object o){
           for(int i = 0; i < incrementsPerHandler; i++){
               Counter++;
           }
        }

    }

    public static class SynchronizedMessageHandlerAsyn{

        private int Counter = 0;

        @Handler(delivery = Invoke.Asynchronously)
        @Synchronized
        public void handleMessage(Object o){
            for(int i = 0; i < incrementsPerHandler; i++){
                Counter++;
            }
        }

    }
}
