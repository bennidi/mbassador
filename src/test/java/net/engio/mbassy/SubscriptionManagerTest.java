package net.engio.mbassy;

import net.engio.mbassy.common.UnitTest;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.MetadataReader;
import net.engio.mbassy.messages.ITestMessage;
import net.engio.mbassy.messages.TestMessage;
import net.engio.mbassy.subscription.Subscription;
import net.engio.mbassy.subscription.SubscriptionFactory;
import net.engio.mbassy.subscription.SubscriptionManager;
import org.junit.Test;

import java.util.Collection;

/**
 * Todo: Add javadoc
 *
 * @author bennidi
 *         Date: 5/12/13
 */
public class SubscriptionManagerTest extends UnitTest{

    @Test
    public void testSimpleSynchronousHandler(){
        SubscriptionManager subMan = new SubscriptionManager(new MetadataReader(), new SubscriptionFactory());
        SimpleSynchronousMessageHandler
                listener1 = new SimpleSynchronousMessageHandler(),
                listener2 = new SimpleSynchronousMessageHandler();
        subMan.subscribe(listener1);
        subMan.subscribe(listener2);

        Collection<Subscription> subscriptions = subMan.getSubscriptionsByMessageType(TestMessage.class);
        assertEquals(1, subscriptions.size());
        for(Subscription sub : subscriptions){
            assertEquals(2, sub.size());
            assertTrue(sub.contains(listener1));
            assertTrue(sub.contains(listener2));
        }

        subscriptions = subMan.getSubscriptionsByMessageType(ITestMessage.class);
        assertEquals(1, subscriptions.size());
        for(Subscription sub : subscriptions){
            assertEquals(2, sub.size());
            assertTrue(sub.contains(listener1));
            assertTrue(sub.contains(listener2));
        }
    }


    static class SimpleSynchronousMessageHandler{

        @Handler
        public void handle(TestMessage message) {
        }

        @Handler
        public void handle(ITestMessage message) {
        }
    }
}
