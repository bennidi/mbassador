package net.engio.mbassy;

import junit.framework.Assert;
import net.engio.mbassy.bus.IMessagePublication;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.common.IMessageBus;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.common.MessageBusTest;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Invoke;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author bennidi
 *         Date: 3/30/14
 */
public class AsyncFIFOBusTest extends MessageBusTest {

    @Test
    public void testSingleThreadedSyncFIFO(){
        BusConfiguration asyncFIFOConfig = new BusConfiguration();
        asyncFIFOConfig.addFeature(Feature.SyncPubSub.Default());
        asyncFIFOConfig.addFeature(Feature.AsynchronousHandlerInvocation.Default(1, 1));
        asyncFIFOConfig.addFeature(Feature.AsynchronousMessageDispatch.Default().setNumberOfMessageDispatchers(1));
        IMessageBus fifoBUs = new MBassador(asyncFIFOConfig);

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
        for(Integer message : messages)
            fifoBUs.post(message).asynchronously();

        while(fifoBUs.hasPendingMessages())
            pause(1000);

        for(SyncListener listener : listeners){
            assertEquals(messages.length, listener.receivedSync.size());
            for(int i=0; i < messages.length; i++){
                assertEquals(messages[i], listener.receivedSync.get(i));
            }
        }

    }

    @Test
    public void testSingleThreadedSyncAsyncFIFO(){
        BusConfiguration asyncFIFOConfig = new BusConfiguration();
        asyncFIFOConfig.addFeature(Feature.SyncPubSub.Default());
        asyncFIFOConfig.addFeature(Feature.AsynchronousHandlerInvocation.Default(1, 1));
        asyncFIFOConfig.addFeature(Feature.AsynchronousMessageDispatch.Default().setNumberOfMessageDispatchers(1));
        IMessageBus fifoBUs = new MBassador(asyncFIFOConfig);

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
        IMessagePublication publication = null;
        // publish in ascending order
        for(Integer message : messages)
            publication = fifoBUs.post(message).asynchronously();

        while(fifoBUs.hasPendingMessages() && ! publication.isFinished())
            pause(200);

        // Check the handlers processing status
        // Define timeframe in which processing should be finished
        // If not then an error is assumed
        long timeElapsed = 0;
        long timeOut = 30000; // 30 seconds
        long begin =  System.currentTimeMillis();
        for(SyncAsyncListener listener : listeners){
            boolean successful = true;
            successful &= messages.length == listener.receivedSync.size();
            successful &=  listener.receivedSync.size() ==listener.receivedAsync.size();
            for(int i=0; i < listener.receivedAsync.size(); i++){
                successful &= messages[i] == listener.receivedSync.get(i);
                // sync and async in same order
                successful &= listener.receivedSync.get(i) == listener.receivedAsync.get(i);
            }
            if(successful)
                break;
            timeElapsed = System.currentTimeMillis() - begin;
        }
        if(timeElapsed >= timeOut) Assert.fail("Processing of handlers unfinished after timeout");

    }

    public static class SyncListener {

        private List<Integer> receivedSync = new LinkedList<Integer>();

        @Handler
        public void handleSync(Integer message){
            receivedSync.add(message);
        }

    }

    public static class SyncAsyncListener {

        private List<Integer> receivedSync = new LinkedList<Integer>();
        private List<Integer> receivedAsync = new LinkedList<Integer>();

        @Handler
        public void handleSync(Integer message){
            receivedSync.add(message);
        }

        @Handler(delivery = Invoke.Asynchronously)
        public void handleASync(Integer message){
            receivedAsync.add(message);
        }

    }

}
