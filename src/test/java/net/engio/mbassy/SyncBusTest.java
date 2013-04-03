package net.engio.mbassy;

import net.engio.mbassy.bus.*;
import net.engio.mbassy.common.MessageBusTest;
import net.engio.mbassy.common.TestUtil;
import net.engio.mbassy.dispatch.HandlerInvocation;
import net.engio.mbassy.events.SubTestMessage;
import net.engio.mbassy.events.TestMessage;
import net.engio.mbassy.listener.*;
import net.engio.mbassy.listeners.*;
import net.engio.mbassy.subscription.SubscriptionContext;
import org.junit.Test;

import java.util.List;

/**
 * Test synchronous and asynchronous dispatch in single and multi-threaded scenario.
 *
 * @author bennidi
 *         Date: 2/8/12
 */
public abstract class SyncBusTest extends MessageBusTest {

    // this value probably needs to be adjusted depending on the performance of the underlying plattform
    // otherwise the tests will fail since asynchronous processing might not have finished when
    // evaluation is run
    private int processingTimeInMS = 4000;


    @Test
    public void testSynchronousMessagePublication() throws Exception {

        ISyncMessageBus bus = getSyncMessageBus();
        ListenerFactory listenerFactory = new ListenerFactory()
                .create(10000, MessageListener1.class)
                .create(10000, MessageListener2.class)
                .create(10000, MessageListener3.class)
                .create(10000, Object.class)
                .create(10000, NonListeningBean.class);

        List<Object> listeners = listenerFactory.build();

        // this will subscribe the listeners concurrently to the bus
        TestUtil.setup(bus, listeners, 10);

        TestMessage message = new TestMessage();
        TestMessage subMessage = new SubTestMessage();

        bus.post(message).now();
        bus.post(subMessage).now();

        pause(processingTimeInMS);

        assertEquals(30000, message.counter.get());
        assertEquals(70000, subMessage.counter.get());

    }

    @Test
    public void testStrongListenerSubscription() throws Exception {

        ISyncMessageBus bus = getSyncMessageBus();


        for(int i = 0; i< 10000; i++){
            bus.subscribe(new MessageListener2());
        }

        runGC();

        TestMessage message = new TestMessage();
        TestMessage subMessage = new SubTestMessage();

        bus.post(message).now();
        bus.post(subMessage).now();

        pause(processingTimeInMS);

        assertEquals(10000, message.counter.get());
        assertEquals(20000, subMessage.counter.get());

    }

    protected abstract ISyncMessageBus getSyncMessageBus();


    public static class MessageListener1 {

        // every event of type TestEvent or any subtype will be delivered
        // to this listener
        @Handler
        public void handleTestEvent(TestMessage message) {
            message.counter.incrementAndGet();
        }

        // this handler will be invoked asynchronously
        @Handler(priority = 0, invocation = HandleSubTestEventInvocation.class)
        public void handleSubTestEvent(SubTestMessage message) {
            message.counter.incrementAndGet();
        }

        // this handler will receive events of type SubTestEvent
        // or any subtabe and that passes the given filter
        @Handler(
                priority = 10,
                delivery = Invoke.Synchronously,
                filters = {@Filter(Filters.RejectAll.class), @Filter(Filters.AllowAll.class)})
        public void handleFiltered(SubTestMessage message) {
            message.counter.incrementAndGet();
        }


    }

    public static class HandleSubTestEventInvocation extends HandlerInvocation<MessageListener1, SubTestMessage> {

        public HandleSubTestEventInvocation(SubscriptionContext context) {
            super(context);
        }

        @Override
        public void invoke(MessageListener1 listener, SubTestMessage message) {
            listener.handleSubTestEvent(message);
        }
    }

    @Listener(references = References.Strong)
    public static class MessageListener2 extends net.engio.mbassy.listeners.EventingTestBean {

        // redefine the configuration for this handler
        @Handler(delivery = Invoke.Synchronously)
        public void handleSubTestEvent(SubTestMessage message) {
            super.handleSubTestEvent(message);
        }

    }

    @Listener(references = References.Strong)
    public static class MessageListener3 extends net.engio.mbassy.listeners.EventingTestBean2 {


        // this handler will be invoked asynchronously
        @Handler(priority = 0, delivery = Invoke.Synchronously)
        public void handleSubTestEventAgain(SubTestMessage message) {
            message.counter.incrementAndGet();
        }

    }


    public static class MBassadorTest extends SyncBusTest {


        @Override
        protected ISyncMessageBus getSyncMessageBus() {
            return new MBassador(BusConfiguration.Default());
        }

    }

    public static class SyncMessageBusTest extends SyncBusTest {


        @Override
        protected ISyncMessageBus getSyncMessageBus() {
            return new SyncMessageBus(new SyncBusConfiguration());
        }
    }

}
