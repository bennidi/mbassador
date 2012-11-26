package org.mbassy;

import org.junit.Test;
import org.mbassy.events.SubTestEvent;
import org.mbassy.events.TestEvent;
import org.mbassy.listeners.*;
import org.mbassy.subscription.Subscription;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Test synchronous and asynchronous dispatch in single and multi-threaded scenario.
 *
 * @author bennidi
 *         Date: 2/8/12
 */
public class MBassadorTest extends UnitTest {

    // this is a single threaded test for subscribing and unsubscribing of a single listener
    @Test
    public void testSubscribeSimple() throws InterruptedException {
        MBassador bus = new MBassador();
        List<Object> listeners = new LinkedList<Object>();
        int listenerCount = 1000;

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
        Collection<Subscription> testEventsubscriptions = bus.getSubscriptionsByMessageType(TestEvent.class);
        assertEquals(1, testEventsubscriptions.size());
        assertEquals(listenerCount, getNumberOfSubscribedListeners(testEventsubscriptions));

        Collection<Subscription> subTestEventsubscriptions = bus.getSubscriptionsByMessageType(SubTestEvent.class);
        assertEquals(3, subTestEventsubscriptions.size());
        assertEquals(3 * listenerCount, getNumberOfSubscribedListeners(subTestEventsubscriptions));

        // unsubscribe the listeners
        for(Object listener : listeners){
            assertTrue(bus.unsubscribe(listener)); // this listener is expected to exist
        }

        // no listener should be left
        testEventsubscriptions = bus.getSubscriptionsByMessageType(TestEvent.class);
        assertEquals(1, testEventsubscriptions.size());
        assertEquals(0, getNumberOfSubscribedListeners(testEventsubscriptions));

        subTestEventsubscriptions = bus.getSubscriptionsByMessageType(SubTestEvent.class);
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

        MBassador bus = new MBassador();
        ListenerFactory listenerFactory = new ListenerFactory()
                .create(100, EventingTestBean.class)
                .create(100, EventingTestBean2.class)
                .create(100, EventingTestBean3.class)
                .create(100, Object.class)
                .create(100, NonListeningBean.class);

        List<Object> listeners = listenerFactory.build();

        // this will subscribe the listeners concurrently to the bus
        TestUtil.setup(bus, listeners, 10);

        // check the generated subscriptions for existence of all previously subscribed valid listeners
        Collection<Subscription> testEventsubscriptions = bus.getSubscriptionsByMessageType(TestEvent.class);
        assertEquals(3, testEventsubscriptions.size());
        assertEquals(300, getNumberOfSubscribedListeners(testEventsubscriptions));

        Collection<Subscription> subTestEventsubscriptions = bus.getSubscriptionsByMessageType(SubTestEvent.class);
        assertEquals(10, subTestEventsubscriptions.size());
        assertEquals(1000, getNumberOfSubscribedListeners(subTestEventsubscriptions));

    }


    @Test
    public void testAsynchronousMessagePublication() throws Exception {

        MBassador bus = new MBassador();
        ListenerFactory listenerFactory = new ListenerFactory()
                .create(100, EventingTestBean.class)
                .create(100, EventingTestBean2.class)
                .create(100, EventingTestBean3.class)
                .create(100, Object.class)
                .create(100, NonListeningBean.class);

        List<Object> listeners = listenerFactory.build();

        // this will subscribe the listeners concurrently to the bus
        TestUtil.setup(bus, listeners, 10);

        TestEvent event = new TestEvent();
        TestEvent subEvent = new SubTestEvent();

        bus.publishAsync(event);
        bus.publishAsync(subEvent);

        pause(2000);

        assertEquals(300, event.counter.get());
        assertEquals(700, subEvent.counter.get());

    }

    @Test
    public void testSynchronousMessagePublication() throws Exception {

        MBassador bus = new MBassador();
        ListenerFactory listenerFactory = new ListenerFactory()
                .create(100, EventingTestBean.class)
                .create(100, EventingTestBean2.class)
                .create(100, EventingTestBean3.class)
                .create(100, Object.class)
                .create(100, NonListeningBean.class);

        List<Object> listeners = listenerFactory.build();

        // this will subscribe the listeners concurrently to the bus
        TestUtil.setup(bus, listeners, 10);

        TestEvent event = new TestEvent();
        TestEvent subEvent = new SubTestEvent();

        bus.publish(event);
        bus.publish(subEvent);

        pause(2000);

        assertEquals(300, event.counter.get());
        assertEquals(700, subEvent.counter.get());

    }

    @Test
    public void testConcurrentMixedMessagePublication() throws Exception {
        final CopyOnWriteArrayList<TestEvent> testEvents = new CopyOnWriteArrayList<TestEvent>();
        final CopyOnWriteArrayList<SubTestEvent> subtestEvents = new CopyOnWriteArrayList<SubTestEvent>();
        final int eventLoopsPerTHread = 100;


        final MBassador bus = new MBassador();
        ListenerFactory listenerFactory = new ListenerFactory()
                .create(100, EventingTestBean.class)
                .create(100, EventingTestBean2.class)
                .create(100, EventingTestBean3.class)
                .create(100, Object.class)
                .create(100, NonListeningBean.class);

        List<Object> listeners = listenerFactory.build();

        // this will subscribe the listeners concurrently to the bus
        TestUtil.setup(bus, listeners, 10);

        ConcurrentExecutor.runConcurrent(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < eventLoopsPerTHread; i++) {
                    TestEvent event = new TestEvent();
                    SubTestEvent subEvent = new SubTestEvent();
                    testEvents.add(event);
                    subtestEvents.add(subEvent);

                    bus.publishAsync(event);
                    bus.publish(subEvent);
                }
            }
        }, 10);

        pause(3000);

        for (TestEvent event : testEvents) {
            assertEquals(300, event.counter.get());
        }

        for (SubTestEvent event : subtestEvents) {
            assertEquals(700, event.counter.get());
        }

    }


}
