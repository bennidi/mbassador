package net.engio.mbassy;

import java.util.LinkedList;
import java.util.List;

import net.engio.mbassy.annotations.Handler;
import net.engio.mbassy.common.MessageBusTest;

import org.junit.Test;

/**
 *
 * @author bennidi
 *         Date: 3/30/14
 */
public class AsyncFIFOBusTest extends MessageBusTest {

    @Test
    public void testSingleThreadedSyncFIFO(){
        // create a fifo bus with 1000 concurrently subscribed listeners
        IMessageBus fifoBUs = new MBassador().start();

        List<Listener> listeners = new LinkedList<Listener>();
        for(int i = 0; i < 1000 ; i++){
            Listener listener = new Listener();
            listeners.add(listener);
            fifoBUs.subscribe(listener);
        }

        // prepare set of messages in increasing order
        int[] messages = new int[1000];
        for(int i = 0; i < messages.length ; i++){
             messages[i] = i;
        }
        // publish in ascending order
        for(Integer message : messages) {
            fifoBUs.publish(message);
        }

        while(fifoBUs.hasPendingMessages()) {
            pause(1000);
        }

        for(Listener listener : listeners){
            assertEquals(messages.length, listener.receivedSync.size());
            for(int i=0; i < messages.length; i++){
                assertEquals(messages[i], listener.receivedSync.get(i));
            }
        }

    }

    @Test
    public void testSingleThreadedSyncAsyncFIFO(){
        // create a fifo bus with 1000 concurrently subscribed listeners
        IMessageBus fifoBUs = new MBassador(1).start();

        List<Listener> listeners = new LinkedList<Listener>();
        for(int i = 0; i < 1000 ; i++){
            Listener listener = new Listener();
            listeners.add(listener);
            fifoBUs.subscribe(listener);
        }

        // prepare set of messages in increasing order
        int[] messages = new int[1000];
        for(int i = 0; i < messages.length ; i++){
            messages[i] = i;
        }
        // publish in ascending order
        for (Integer message : messages) {
            fifoBUs.publishAsync(message);
        }

        while (fifoBUs.hasPendingMessages()) {
            pause(2000);
        }

        for(Listener listener : listeners) {
            List<Integer> receivedSync = listener.receivedSync;

            synchronized (receivedSync) {
                assertEquals(messages.length, receivedSync.size());

                for(int i=0; i < messages.length; i++){
                    assertEquals(messages[i], receivedSync.get(i));
                }
            }
        }

    }

    public static class Listener {

        private List<Integer> receivedSync = new LinkedList<Integer>();

        @Handler
        public void handleSync(Integer message){
            synchronized (this.receivedSync) {
                this.receivedSync.add(message);
            }
        }

    }
}
