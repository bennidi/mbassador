package net.engio.mbassy;

import net.engio.mbassy.common.ConcurrentExecutor;
import net.engio.mbassy.common.UnitTest;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.MetadataReader;
import net.engio.mbassy.messages.ITestMessage;
import net.engio.mbassy.messages.TestMessage;
import net.engio.mbassy.messages.TestMessage3;
import net.engio.mbassy.subscription.Subscription;
import net.engio.mbassy.subscription.SubscriptionFactory;
import net.engio.mbassy.subscription.SubscriptionManager;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Todo: Add javadoc
 *
 * @author bennidi
 *         Date: 5/12/13
 */
public class SubscriptionManagerTest extends UnitTest{

    @Test
    public void testSimpleSynchronousHandler(){
        final SubscriptionManager subMan = new SubscriptionManager(new MetadataReader(), new SubscriptionFactory());
        final Set listeners = Collections.synchronizedSet(new HashSet());
        final int concurrentUnits = 5;
        final int numberOfLoops = 100;
        final int numberOfListeners =  numberOfLoops * concurrentUnits * 2;

        ConcurrentExecutor.runConcurrent(new Runnable() {
            @Override
            public void run() {
                for(int i = 0 ; i < numberOfLoops ; i++){
                    SimpleSynchronousMessageHandler
                            listener1 = new SimpleSynchronousMessageHandler();
                    SimpleSynchronousMessageHandler2 listener2 = new SimpleSynchronousMessageHandler2();
                    subMan.subscribe(listener1);
                    subMan.subscribe(listener2);
                    listeners.add(listener1);
                    listeners.add(listener2);
                }

            }
        }, concurrentUnits);


        Collection<Subscription> subscriptions = subMan.getSubscriptionsByMessageType(TestMessage.class);
        assertEquals(2, subscriptions.size());

        for(Subscription sub : subscriptions){
            assertEquals(numberOfListeners / 2, sub.size());
            for(Object listener : listeners){
                if(sub.isFromListener(listener))assertTrue(sub.contains(listener));
            }
        }

        subscriptions = subMan.getSubscriptionsByMessageType(ITestMessage.class);
        assertEquals(2 , subscriptions.size());
        for(Subscription sub : subscriptions){
            assertEquals(numberOfListeners / 2, sub.size());
            for(Object listener : listeners){
                if(sub.isFromListener(listener))assertTrue(sub.contains(listener));
            }
        }

        subscriptions = subMan.getSubscriptionsByMessageType(TestMessage3.class);
        assertEquals(4 , subscriptions.size());
        for(Subscription sub : subscriptions){
            assertEquals(numberOfListeners / 2, sub.size());
            for(Object listener : listeners){
                if(sub.isFromListener(listener))assertTrue(sub.contains(listener));
            }
        }
    }





    static class SimpleSynchronousMessageHandler{

        @Handler
        public void handle(TestMessage message) {
        }

        @Handler
        public void handle(ITestMessage message) {
        }

        @Handler
        public void handle(TestMessage3 message) {
        }
    }


    static class SimpleSynchronousMessageHandler2{

        @Handler
        public void handle(TestMessage message) {
        }

        @Handler
        public void handle(ITestMessage message) {
        }

        @Handler
        public void handle(TestMessage3 message) {
        }
    }
}
