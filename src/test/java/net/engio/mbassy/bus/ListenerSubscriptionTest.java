package net.engio.mbassy.bus;

import net.engio.mbassy.common.MessageBusTest;
import net.engio.mbassy.common.TestUtil;
import net.engio.mbassy.events.SubTestMessage;
import net.engio.mbassy.events.TestMessage;
import net.engio.mbassy.listeners.EventingTestBean;
import net.engio.mbassy.listeners.EventingTestBean2;
import net.engio.mbassy.listeners.EventingTestBean3;
import net.engio.mbassy.listeners.ListenerFactory;
import net.engio.mbassy.listeners.NonListeningBean;
import net.engio.mbassy.subscription.Subscription;
import org.junit.Test;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Testing different scenarios of subscribing objects (listeners and non-listeners) to the message bus.
 *
 * @author bennidi
 *         Date: 1/9/13
 */
public class ListenerSubscriptionTest extends MessageBusTest{


    // this is a single threaded test for subscribing and unsubscribing of a single listener
    @Test
    public void testSubscribeSimple() throws InterruptedException {
        MBassador bus = getBus(new BusConfiguration());
        List<Object> listeners = new LinkedList<Object>();
        int listenerCount = 200000;

        // subscribe a number of listeners to the bus
        for (int i = 1; i <= listenerCount; i++) {
            EventingTestBean listener = new EventingTestBean();
            NonListeningBean nonListener = new NonListeningBean();
            listeners.add(listener);

            bus.subscribe(listener);
            bus.subscribe(nonListener);

            assertFalse(bus.unsubscribe(nonListener)); // these are not expected to be subscribed listeners
            assertFalse(bus.unsubscribe(new EventingTestBean()));

        }

        // check the generated subscriptions for existence of all previously subscribed valid listeners
        Collection<Subscription> testEventsubscriptions = bus.getSubscriptionsByMessageType(TestMessage.class);
        assertEquals(1, testEventsubscriptions.size());
        assertEquals(listenerCount, getNumberOfSubscribedListeners(testEventsubscriptions));

        Collection<Subscription> subTestEventsubscriptions = bus.getSubscriptionsByMessageType(SubTestMessage.class);
        assertEquals(3, subTestEventsubscriptions.size());
        assertEquals(3 * listenerCount, getNumberOfSubscribedListeners(subTestEventsubscriptions));

        // unsubscribe the listeners
        for(Object listener : listeners){
            assertTrue(bus.unsubscribe(listener)); // this listener is expected to exist
        }

        // no listener should be left
        testEventsubscriptions = bus.getSubscriptionsByMessageType(TestMessage.class);
        assertEquals(1, testEventsubscriptions.size());
        assertEquals(0, getNumberOfSubscribedListeners(testEventsubscriptions));

        subTestEventsubscriptions = bus.getSubscriptionsByMessageType(SubTestMessage.class);
        assertEquals(3, subTestEventsubscriptions.size());
        assertEquals(0, getNumberOfSubscribedListeners(subTestEventsubscriptions));

    }

    private int getNumberOfSubscribedListeners(Collection<Subscription> subscriptions) {
        int listeners = 0;
        for (Subscription sub : subscriptions) {
            listeners += sub.size();
        }
        return listeners;
    }

    @Test
    public void testConcurrentSubscription() throws Exception {

        MBassador bus = getBus(new BusConfiguration());
        ListenerFactory listenerFactory = new ListenerFactory()
                .create(10000, EventingTestBean.class)
                .create(10000, EventingTestBean2.class)
                .create(10000, EventingTestBean3.class)
                .create(10000, Object.class)
                .create(10000, NonListeningBean.class);

        List<Object> listeners = listenerFactory.build();

        // this will subscribe the listeners concurrently to the bus
        TestUtil.setup(bus, listeners, 10);

        // check the generated subscriptions for existence of all previously subscribed valid listeners
        Collection<Subscription> testEventsubscriptions = bus.getSubscriptionsByMessageType(TestMessage.class);
        assertEquals(3, testEventsubscriptions.size());
        assertEquals(30000, getNumberOfSubscribedListeners(testEventsubscriptions));

        Collection<Subscription> subTestEventsubscriptions = bus.getSubscriptionsByMessageType(SubTestMessage.class);
        assertEquals(10, subTestEventsubscriptions.size());
        assertEquals(100000, getNumberOfSubscribedListeners(subTestEventsubscriptions));

    }
}
