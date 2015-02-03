package net.engio.mbassy;

import java.util.LinkedList;
import java.util.List;

import net.engio.mbassy.bus.BusFactory;
import net.engio.mbassy.bus.common.IMessageBus;
import net.engio.mbassy.common.MessageBusTest;
import net.engio.mbassy.listener.Handler;

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
            fifoBUs.post(message).asynchronously();
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
            fifoBUs.post(message).asynchronously();
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

    /*
    @Test
    public void testMultiThreadedSyncFIFO(){
        // create a fifo bus with 1000 concurrently subscribed listeners
        final IMessageBus fifoBUs = BusFactory.AsynchronousSequentialFIFO();

        List<SyncListener> listeners = new LinkedList<SyncListener>();
        for(int i = 0; i < 1000 ; i++){
            SyncListener listener = new SyncListener();
            listeners.add(listener);
            fifoBUs.subscribe(listener);
        }

        // prepare set of messages in increasing order
        final int[] messages = new int[10000];
        for(int i = 0; i < messages.length ; i++){
            messages[i] = i;
        }
        final AtomicInteger messageIndex = new AtomicInteger(0);
        // publish in ascending order
        ConcurrentExecutor.runConcurrent(new Runnable() {
            @Override
            public void run() {
                int idx;
                while((idx = messageIndex.getAndIncrement()) < messages.length){
                    fifoBUs.post(messages[idx]).asynchronously();
                }
            }
        }, 5);

        while(fifoBUs.hasPendingMessages())
            pause(1000);

        for(SyncListener listener : listeners){
            assertEquals(messages.length, listener.receivedSync.size());
            for(int i=0; i < messages.length; i++){
                assertEquals(messages[i], listener.receivedSync.get(i));
            }
        }

    }  */



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
