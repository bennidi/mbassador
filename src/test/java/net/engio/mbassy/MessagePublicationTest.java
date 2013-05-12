package net.engio.mbassy;

import net.engio.mbassy.bus.BusConfiguration;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.common.ConcurrentExecutor;
import net.engio.mbassy.common.MessageBusTest;
import net.engio.mbassy.common.TestUtil;
import net.engio.mbassy.messages.SubTestMessage;
import net.engio.mbassy.messages.TestMessage;
import net.engio.mbassy.messages.TestMessage2;
import net.engio.mbassy.listeners.*;
import org.junit.Test;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Test synchronous and asynchronous dispatch in single and multi-threaded scenario.
 *
 * @author bennidi
 *         Date: 2/8/12
 */
public class MessagePublicationTest extends MessageBusTest {

    // this value probably needs to be adjusted depending on the performance of the underlying plattform
    // otherwise the tests will fail since asynchronous processing might not have finished when
    // evaluation is run
    private int processingTimeInMS = 4000;


    @Test
    public void testAsynchronousMessagePublication() throws Exception {

        MBassador bus = getBus(new BusConfiguration());
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

        TestMessage message = new TestMessage();
        TestMessage subMessage = new SubTestMessage();
        TestMessage2 message2 = new TestMessage2();

        bus.publishAsync(message);
        bus.publishAsync(subMessage);
        bus.publishAsync(message2);

        pause(processingTimeInMS);

        assertEquals(50000, message.counter.get());
        assertEquals(80000, subMessage.counter.get());
        assertEquals(20000, message2.counter.get());

    }

    @Test
    public void testSynchronousMessagePublication() throws Exception {

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

        TestMessage message = new TestMessage();
        TestMessage subMessage = new SubTestMessage();

        bus.publish(message);
        bus.publish(subMessage);

        pause(processingTimeInMS);

        assertEquals(30000, message.counter.get());
        assertEquals(70000, subMessage.counter.get());

    }

    @Test
    public void testStrongListenerSubscription() throws Exception {

        MBassador bus = getBus(new BusConfiguration());


        for(int i = 0; i< 10000; i++){
            bus.subscribe(new EventingTestBean2());
        }

        runGC();

        TestMessage message = new TestMessage();
        TestMessage subMessage = new SubTestMessage();

        bus.publish(message);
        bus.publish(subMessage);

        pause(processingTimeInMS);

        assertEquals(10000, message.counter.get());
        assertEquals(20000, subMessage.counter.get());

    }

    @Test
    public void testConcurrentMixedMessagePublication() throws Exception {
        final CopyOnWriteArrayList<TestMessage> testMessages = new CopyOnWriteArrayList<TestMessage>();
        final CopyOnWriteArrayList<SubTestMessage> subtestMessages = new CopyOnWriteArrayList<SubTestMessage>();
        final int eventLoopsPerTHread = 100;


        final MBassador bus = getBus(new BusConfiguration());
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
                    TestMessage message = new TestMessage();
                    SubTestMessage subMessage = new SubTestMessage();
                    testMessages.add(message);
                    subtestMessages.add(subMessage);

                    bus.publishAsync(message);
                    bus.publish(subMessage);
                }
            }
        }, 10);

        pause(processingTimeInMS);

        for (TestMessage message : testMessages) {
            assertEquals(30000, message.counter.get());
        }

        for (SubTestMessage message : subtestMessages) {
            assertEquals(70000, message.counter.get());
        }

    }


}
