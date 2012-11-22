package org.mbassy;

import org.junit.Test;
import org.mbassy.events.SubTestEvent;
import org.mbassy.events.TestEvent;
import org.mbassy.listeners.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Test synchronous and asynchronous dispatch in single and multi-threaded scenario.
 *
 * @author bennidi
 *         Date: 2/8/12
 */
public class MBassadorTest extends UnitTest {


    @Test
    public void testSubscribeSimple() throws InterruptedException {

        MBassador bus = new MBassador();
        int listenerCount = 1000;

        for (int i = 1; i <= listenerCount; i++) {
            EventingTestBean listener = new EventingTestBean();
            NonListeningBean nonListener = new NonListeningBean();
            bus.subscribe(listener);
            bus.subscribe(nonListener);
            assertTrue(bus.unsubscribe(listener));
            assertFalse(bus.unsubscribe(nonListener));
            assertFalse(bus.unsubscribe(new EventingTestBean()));

        }
    }

    @Test
    public void testSubscribeConcurrent() throws Exception {

        MBassador bus = new MBassador();
        ListenerFactory listenerFactory = new ListenerFactory()
                .create(100, EventingTestBean.class)
                .create(100, EventingTestBean2.class)
                .create(100, EventingTestBean3.class)
                .create(100, Object.class)
                .create(100, NonListeningBean.class);

        List<Object> listeners = listenerFactory.build();
        TestUtil.setup(bus, listeners, 10);

        TestEvent event = new TestEvent();
        SubTestEvent subEvent = new SubTestEvent();

        bus.publish(event);
        bus.publish(subEvent);

        pause(4000);

        assertEquals(300, event.counter.get());
        assertEquals(700, subEvent.counter.get());

    }


    @Test
    public void testAsynchronous() throws InterruptedException {

        MBassador bus = new MBassador();
        int listenerCount = 1000;
        List<EventingTestBean> persistentReferences = new ArrayList();

        for (int i = 1; i <= listenerCount; i++) {
            EventingTestBean bean = new EventingTestBean();
            persistentReferences.add(bean);
            bus.subscribe(bean);
        }

        TestEvent event = new TestEvent();
        TestEvent subEvent = new SubTestEvent();

        bus.publishAsync(event);
        bus.publishAsync(subEvent);

        pause(2000);

        assertTrue(event.counter.get() == 1000);
        assertTrue(subEvent.counter.get() == 1000 * 2);

    }

    @Test
    public void testSynchronous() throws InterruptedException {

        MBassador bus = new MBassador();
        int listenerCount = 10;
        List<EventingTestBean> persistentReferences = new ArrayList();
        for (int i = 1; i <= listenerCount; i++) {


            EventingTestBean bean = new EventingTestBean();
            persistentReferences.add(bean);
            bus.subscribe(bean);

            TestEvent event = new TestEvent();
            TestEvent subEvent = new SubTestEvent();

            bus.publish(event);
            bus.publish(subEvent);

            assertEquals(i, event.counter.get());

            pause(50);

            assertEquals(i * 2, subEvent.counter.get());

        }

    }

    @Test
    public void testConcurrentPublication() {
        final MBassador bus = new MBassador();
        final int listenerCount = 100;
        final int concurrency = 20;
        final CopyOnWriteArrayList<TestEvent> testEvents = new CopyOnWriteArrayList<TestEvent>();
        final CopyOnWriteArrayList<SubTestEvent> subtestEvents = new CopyOnWriteArrayList<SubTestEvent>();
        final CopyOnWriteArrayList<EventingTestBean> persistentReferences = new CopyOnWriteArrayList<EventingTestBean>();

        ConcurrentExecutor.runConcurrent(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < listenerCount; i++) {
                    EventingTestBean bean = new EventingTestBean();
                    persistentReferences.add(bean);
                    bus.subscribe(bean);
                }
            }
        }, concurrency);

        ConcurrentExecutor.runConcurrent(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < listenerCount; i++) {
                    TestEvent event = new TestEvent();
                    SubTestEvent subEvent = new SubTestEvent();
                    testEvents.add(event);
                    subtestEvents.add(subEvent);

                    bus.publishAsync(event);
                    bus.publish(subEvent);
                }
            }
        }, concurrency);

        pause(3000);

        for (TestEvent event : testEvents) {
            assertEquals(listenerCount * concurrency, event.counter.get());
        }

        for (SubTestEvent event : subtestEvents) {
            assertEquals(listenerCount * concurrency * 2, event.counter.get());
        }

    }


}
