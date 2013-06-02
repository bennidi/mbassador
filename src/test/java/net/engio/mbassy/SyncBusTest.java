package net.engio.mbassy;

import net.engio.mbassy.bus.*;
import net.engio.mbassy.common.ConcurrentExecutor;
import net.engio.mbassy.common.ListenerFactory;
import net.engio.mbassy.common.MessageBusTest;
import net.engio.mbassy.common.TestUtil;
import net.engio.mbassy.listeners.*;
import net.engio.mbassy.messages.MessageTypes;
import net.engio.mbassy.messages.MultipartMessage;
import net.engio.mbassy.messages.StandardMessage;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test synchronous and asynchronous dispatch in single and multi-threaded scenario.
 *
 * @author bennidi
 *         Date: 2/8/12
 */
public abstract class SyncBusTest extends MessageBusTest {


    protected abstract ISyncMessageBus getSyncMessageBus();

    @Test
    public void testSynchronousMessagePublication() throws Exception {

        final ISyncMessageBus bus = getSyncMessageBus();
        ListenerFactory listeners = new ListenerFactory()
                .create(InstancesPerListener, IMessageListener.DefaultListener.class)
                .create(InstancesPerListener, IMessageListener.DisabledListener.class)
                .create(InstancesPerListener, MessagesListener.DefaultListener.class)
                .create(InstancesPerListener, MessagesListener.DisabledListener.class)
                .create(InstancesPerListener, Object.class);


        ConcurrentExecutor.runConcurrent(TestUtil.subscriber(bus, listeners), ConcurrentUnits);

        Runnable publishAndCheck = new Runnable() {
            @Override
            public void run() {
                StandardMessage standardMessage = new StandardMessage();
                MultipartMessage multipartMessage = new MultipartMessage();

                bus.post(standardMessage).now();
                bus.post(multipartMessage).now();
                bus.post(MessageTypes.Simple).now();
                bus.post(MessageTypes.Multipart).now();

                assertEquals(InstancesPerListener, standardMessage.getTimesHandled(IMessageListener.DefaultListener.class));
                assertEquals(InstancesPerListener, multipartMessage.getTimesHandled(IMessageListener.DefaultListener.class));
            }
        };

        // single threaded
        ConcurrentExecutor.runConcurrent(publishAndCheck, 1);

        // multi threaded
        MessageTypes.resetAll();
        ConcurrentExecutor.runConcurrent(publishAndCheck, ConcurrentUnits);
        assertEquals(InstancesPerListener * ConcurrentUnits, MessageTypes.Simple.getTimesHandled(IMessageListener.DefaultListener.class));
        assertEquals(InstancesPerListener * ConcurrentUnits, MessageTypes.Multipart.getTimesHandled(IMessageListener.DefaultListener.class));
        assertEquals(InstancesPerListener * ConcurrentUnits, MessageTypes.Simple.getTimesHandled(MessagesListener.DefaultListener.class));
        assertEquals(InstancesPerListener * ConcurrentUnits, MessageTypes.Multipart.getTimesHandled(MessagesListener.DefaultListener.class));
    }


    @Test
    public void testExceptionInHandlerInvocation(){
        final AtomicInteger exceptionCount = new AtomicInteger(0);
        IPublicationErrorHandler ExceptionCounter = new IPublicationErrorHandler() {
            @Override
            public void handleError(PublicationError error) {
                exceptionCount.incrementAndGet();
            }
        };

        final ISyncMessageBus bus = getSyncMessageBus();
        bus.addErrorHandler(ExceptionCounter);
        ListenerFactory listeners = new ListenerFactory()
                .create(InstancesPerListener, ExceptionThrowingListener.class);

        ConcurrentExecutor.runConcurrent(TestUtil.subscriber(bus, listeners), ConcurrentUnits);

        Runnable publish = new Runnable() {
            @Override
            public void run() {
                bus.post(new StandardMessage()).now();
            }
        };

        // single threaded
        ConcurrentExecutor.runConcurrent(publish, 1);

        exceptionCount.set(0);

        // multi threaded
        ConcurrentExecutor.runConcurrent(publish, ConcurrentUnits);
        assertEquals(InstancesPerListener * ConcurrentUnits, exceptionCount.get());
    }

    @Test
    public void testCustomHandlerInvocation(){
        final ISyncMessageBus bus = getSyncMessageBus();
        ListenerFactory listeners = new ListenerFactory()
                .create(InstancesPerListener, CustomInvocationListener.class)
                .create(InstancesPerListener, Object.class);


        ConcurrentExecutor.runConcurrent(TestUtil.subscriber(bus, listeners), ConcurrentUnits);

        Runnable publishAndCheck = new Runnable() {
            @Override
            public void run() {
                StandardMessage standardMessage = new StandardMessage();
                MultipartMessage multipartMessage = new MultipartMessage();

                bus.post(standardMessage).now();
                bus.post(multipartMessage).now();
                bus.post(MessageTypes.Simple).now();

                assertEquals(InstancesPerListener * 2, standardMessage.getTimesHandled(CustomInvocationListener.class));
                assertEquals(0, multipartMessage.getTimesHandled(CustomInvocationListener.class));
                assertEquals(0, MessageTypes.Simple.getTimesHandled(CustomInvocationListener.class));
            }
        };

        // single threaded
        ConcurrentExecutor.runConcurrent(publishAndCheck, 1);

        // multi threaded
        ConcurrentExecutor.runConcurrent(publishAndCheck, ConcurrentUnits);

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
