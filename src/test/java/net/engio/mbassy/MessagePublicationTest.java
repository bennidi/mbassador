package net.engio.mbassy;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import org.junit.Test;
import net.engio.mbassy.common.ConcurrentExecutor;
import net.engio.mbassy.common.TestUtil;
import net.engio.mbassy.common.UnitTest;
import net.engio.mbassy.events.SubTestEvent;
import net.engio.mbassy.events.TestEvent;
import net.engio.mbassy.events.TestEvent2;
import net.engio.mbassy.listeners.EventingTestBean;
import net.engio.mbassy.listeners.EventingTestBean2;
import net.engio.mbassy.listeners.EventingTestBean3;
import net.engio.mbassy.listeners.ListenerFactory;
import net.engio.mbassy.listeners.MultiEventHandler;
import net.engio.mbassy.listeners.NonListeningBean;

/**
 * Test synchronous and asynchronous dispatch in single and multi-threaded scenario.
 *
 * @author bennidi
 *         Date: 2/8/12
 */
public class MessagePublicationTest extends UnitTest {

    // this value probably needs to be adjusted depending on the performance of the underlying plattform
    // otherwise the tests will fail since asynchronous processing might not have finished when
    // evaluation is run
    private int processingTimeInMS = 4000;


    @Test
    public void testAsynchronousMessagePublication() throws Exception {

        MBassador bus = new MBassador(new BusConfiguration());
        ListenerFactory listenerFactory = new ListenerFactory()
                .create(10000, EventingTestBean.class)
                .create(10000, EventingTestBean2.class)
                .create(10000, EventingTestBean3.class)
                .create(10000, Object.class)
                .create(10000, NonListeningBean.class)
                .create(10000, MultiEventHandler.class);

        List<Object> listeners = listenerFactory.build();

        // this will subscribe the listeners concurrently to the bus
        TestUtil.setup(bus, listeners, 10);

        TestEvent event = new TestEvent();
        TestEvent subEvent = new SubTestEvent();
        TestEvent2 event2 = new TestEvent2();

        bus.publishAsync(event);
        bus.publishAsync(subEvent);
        bus.publishAsync(event2);

        pause(processingTimeInMS);

        assertEquals(50000, event.counter.get());
        assertEquals(80000, subEvent.counter.get());
        assertEquals(20000, event2.counter.get());

    }

    @Test
    public void testSynchronousMessagePublication() throws Exception {

        MBassador bus = new MBassador(new BusConfiguration());
        ListenerFactory listenerFactory = new ListenerFactory()
                .create(10000, EventingTestBean.class)
                .create(10000, EventingTestBean2.class)
                .create(10000, EventingTestBean3.class)
                .create(10000, Object.class)
                .create(10000, NonListeningBean.class);

        List<Object> listeners = listenerFactory.build();

        // this will subscribe the listeners concurrently to the bus
        TestUtil.setup(bus, listeners, 10);

        TestEvent event = new TestEvent();
        TestEvent subEvent = new SubTestEvent();

        bus.publish(event);
        bus.publish(subEvent);

        pause(processingTimeInMS);

        assertEquals(30000, event.counter.get());
        assertEquals(70000, subEvent.counter.get());

    }

    @Test
    public void testConcurrentMixedMessagePublication() throws Exception {
        final CopyOnWriteArrayList<TestEvent> testEvents = new CopyOnWriteArrayList<TestEvent>();
        final CopyOnWriteArrayList<SubTestEvent> subtestEvents = new CopyOnWriteArrayList<SubTestEvent>();
        final int eventLoopsPerTHread = 100;


        final MBassador bus = new MBassador(new BusConfiguration());
        ListenerFactory listenerFactory = new ListenerFactory()
                .create(10000, EventingTestBean.class)
                .create(10000, EventingTestBean2.class)
                .create(10000, EventingTestBean3.class)
                .create(10000, Object.class)
                .create(10000, NonListeningBean.class);

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

        pause(processingTimeInMS);

        for (TestEvent event : testEvents) {
            assertEquals(30000, event.counter.get());
        }

        for (SubTestEvent event : subtestEvents) {
            assertEquals(70000, event.counter.get());
        }

    }


}
