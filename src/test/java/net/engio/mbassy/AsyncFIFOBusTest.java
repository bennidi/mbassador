package net.engio.mbassy;

import java.util.LinkedList;
import java.util.List;

import net.engio.mbassy._misc.BusFactory;
import net.engio.mbassy.annotations.Handler;
import net.engio.mbassy.bus.common.IMessageBus;
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
        IMessageBus fifoBUs = BusFactory.AsynchronousSequentialFIFO();

        List<SyncListener> listeners = new LinkedList<SyncListener>();
        for(int i = 0; i < 1000 ; i++){
            SyncListener listener = new SyncListener();
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
            fifoBUs.publishAsync(message);
        }

        while(fifoBUs.hasPendingMessages()) {
            pause(1000);
        }

        for(SyncListener listener : listeners){
            assertEquals(messages.length, listener.receivedSync.size());
            for(int i=0; i < messages.length; i++){
                assertEquals(messages[i], listener.receivedSync.get(i));
            }
        }

    }

    // NOTE: Can fail due to timing issues.
    @Test
    public void testSingleThreadedSyncAsyncFIFO(){
        // create a fifo bus with 1000 concurrently subscribed listeners
        IMessageBus fifoBUs = BusFactory.AsynchronousSequentialFIFO();

        List<SyncAsyncListener> listeners = new LinkedList<SyncAsyncListener>();
        for(int i = 0; i < 1000 ; i++){
            SyncAsyncListener listener = new SyncAsyncListener();
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
            fifoBUs.publishAsync(message);
        }

        while(fifoBUs.hasPendingMessages()) {
            pause(2000);
        }

        for(SyncAsyncListener listener : listeners){
            assertEquals(messages.length, listener.receivedSync.size());
            for(int i=0; i < messages.length; i++){
                assertEquals(messages[i], listener.receivedSync.get(i));
            }
        }

    }

    public static class SyncListener {

        private List<Integer> receivedSync = new LinkedList<Integer>();

        @Handler
        public void handleSync(Integer message){
            this.receivedSync.add(message);
        }

    }

    public static class SyncAsyncListener {

        private List<Integer> receivedSync = new LinkedList<Integer>();

        @Handler
        public void handleSync(Integer message){
            this.receivedSync.add(message);
        }
    }

}
