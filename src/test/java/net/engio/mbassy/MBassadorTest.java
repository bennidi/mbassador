package net.engio.mbassy;

import net.engio.mbassy.bus.BusConfiguration;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.common.ConcurrentExecutor;
import net.engio.mbassy.common.MessageBusTest;
import net.engio.mbassy.common.TestUtil;
import net.engio.mbassy.listeners.ExceptionThrowingListener;
import net.engio.mbassy.listeners.IMessageListener;
import net.engio.mbassy.common.ListenerFactory;
import net.engio.mbassy.listeners.MessagesListener;
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
public class MBassadorTest extends MessageBusTest {


    @Test
    public void testSynchronousMessagePublication() throws Exception {

        final MBassador bus = getBus(new BusConfiguration());
        ListenerFactory listeners = new ListenerFactory()
                .create(InstancesPerListener, IMessageListener.DefaultListener.class)
                .create(InstancesPerListener, IMessageListener.AsyncListener.class)
                .create(InstancesPerListener, IMessageListener.DisabledListener.class)
                .create(InstancesPerListener, MessagesListener.DefaultListener.class)
                .create(InstancesPerListener, MessagesListener.AsyncListener.class)
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

                pause(processingTimeInMS);

                assertEquals(InstancesPerListener, standardMessage.getTimesHandled(IMessageListener.DefaultListener.class));
                assertEquals(InstancesPerListener, standardMessage.getTimesHandled(IMessageListener.AsyncListener.class));

                assertEquals(InstancesPerListener, multipartMessage.getTimesHandled(IMessageListener.DefaultListener.class));
                assertEquals(InstancesPerListener, multipartMessage.getTimesHandled(IMessageListener.AsyncListener.class));


            }
        };

        ConcurrentExecutor.runConcurrent(publishAndCheck, 1);

        MessageTypes.resetAll();
        ConcurrentExecutor.runConcurrent(publishAndCheck, ConcurrentUnits);
        assertEquals(InstancesPerListener * ConcurrentUnits, MessageTypes.Simple.getTimesHandled(IMessageListener.DefaultListener.class));
        assertEquals(InstancesPerListener * ConcurrentUnits, MessageTypes.Simple.getTimesHandled(IMessageListener.AsyncListener.class));
        assertEquals(InstancesPerListener * ConcurrentUnits, MessageTypes.Simple.getTimesHandled(MessagesListener.DefaultListener.class));
        assertEquals(InstancesPerListener * ConcurrentUnits, MessageTypes.Simple.getTimesHandled(MessagesListener.AsyncListener.class));
    }

    @Test
    public void testAsynchronousMessagePublication() throws Exception {

        final MBassador bus = getBus(new BusConfiguration());
        ListenerFactory listeners = new ListenerFactory()
                .create(InstancesPerListener, IMessageListener.DefaultListener.class)
                .create(InstancesPerListener, IMessageListener.AsyncListener.class)
                .create(InstancesPerListener, IMessageListener.DisabledListener.class)
                .create(InstancesPerListener, MessagesListener.DefaultListener.class)
                .create(InstancesPerListener, MessagesListener.AsyncListener.class)
                .create(InstancesPerListener, MessagesListener.DisabledListener.class)
                .create(InstancesPerListener, Object.class);


        ConcurrentExecutor.runConcurrent(TestUtil.subscriber(bus, listeners), ConcurrentUnits);

        Runnable publishAndCheck = new Runnable() {
            @Override
            public void run() {
                StandardMessage standardMessage = new StandardMessage();
                MultipartMessage multipartMessage = new MultipartMessage();

                bus.post(standardMessage).asynchronously();
                bus.post(multipartMessage).asynchronously();
                bus.post(MessageTypes.Simple).asynchronously();

                pause(processingTimeInMS);

                assertEquals(InstancesPerListener, standardMessage.getTimesHandled(IMessageListener.DefaultListener.class));
                assertEquals(InstancesPerListener, standardMessage.getTimesHandled(IMessageListener.AsyncListener.class));

                assertEquals(InstancesPerListener, multipartMessage.getTimesHandled(IMessageListener.DefaultListener.class));
                assertEquals(InstancesPerListener, multipartMessage.getTimesHandled(IMessageListener.AsyncListener.class));


            }
        };

        ConcurrentExecutor.runConcurrent(publishAndCheck, 1);

        MessageTypes.resetAll();
        ConcurrentExecutor.runConcurrent(publishAndCheck, ConcurrentUnits);
        assertEquals(InstancesPerListener * ConcurrentUnits, MessageTypes.Simple.getTimesHandled(IMessageListener.DefaultListener.class));
        assertEquals(InstancesPerListener * ConcurrentUnits, MessageTypes.Simple.getTimesHandled(IMessageListener.AsyncListener.class));
        assertEquals(InstancesPerListener * ConcurrentUnits, MessageTypes.Simple.getTimesHandled(MessagesListener.DefaultListener.class));
        assertEquals(InstancesPerListener * ConcurrentUnits, MessageTypes.Simple.getTimesHandled(MessagesListener.AsyncListener.class));
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

        final MBassador bus = new MBassador(new BusConfiguration());
        bus.addErrorHandler(ExceptionCounter);
        ListenerFactory listeners = new ListenerFactory()
                .create(InstancesPerListener, ExceptionThrowingListener.class);
        ConcurrentExecutor.runConcurrent(TestUtil.subscriber(bus, listeners), ConcurrentUnits);

        Runnable publishAndCheck = new Runnable() {
            @Override
            public void run() {
                bus.post(new StandardMessage()).asynchronously();

            }
        };

        // single threaded
        ConcurrentExecutor.runConcurrent(publishAndCheck, 1);
        pause(processingTimeInMS);
        assertEquals(InstancesPerListener, exceptionCount.get());


        // multi threaded
        exceptionCount.set(0);
        ConcurrentExecutor.runConcurrent(publishAndCheck, ConcurrentUnits);
        pause(processingTimeInMS);
        assertEquals(InstancesPerListener * ConcurrentUnits, exceptionCount.get());

    }




}
