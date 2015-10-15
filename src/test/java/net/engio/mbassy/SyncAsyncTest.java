package net.engio.mbassy;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;
import net.engio.mbassy.common.*;
import net.engio.mbassy.listeners.*;
import net.engio.mbassy.messages.MessageTypes;
import net.engio.mbassy.messages.MultipartMessage;
import net.engio.mbassy.messages.StandardMessage;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test synchronous and asynchronous dispatch in single and multi-threaded scenario.
 *
 * @author bennidi
 *         Date: 2/8/12
 */
public class SyncAsyncTest extends MessageBusTest {



    @Test
    public void testSyncPublicationSyncHandlers() throws Exception {

        ListenerFactory listeners = new ListenerFactory()
                .create(InstancesPerListener, Listeners.synchronous())
                .create(InstancesPerListener, Listeners.noHandlers());
        final MBassador bus = createBus(SyncAsync(), listeners);


        Runnable publishAndCheck = new Runnable() {
            @Override
            public void run() {
                StandardMessage standardMessage = new StandardMessage();
                MultipartMessage multipartMessage = new MultipartMessage();

                bus.post(standardMessage).now();
                bus.post(multipartMessage).now();
                bus.post(MessageTypes.Simple).now();

                assertEquals(InstancesPerListener, standardMessage.getTimesHandled(IMessageListener.DefaultListener.class));
                assertEquals(InstancesPerListener, multipartMessage.getTimesHandled(IMessageListener.DefaultListener.class));
            }
        };

        // test single-threaded
        ConcurrentExecutor.runConcurrent(publishAndCheck, 1);

        // test multi-threaded
        MessageTypes.resetAll();
        ConcurrentExecutor.runConcurrent(publishAndCheck, ConcurrentUnits);
        assertEquals(InstancesPerListener * ConcurrentUnits, MessageTypes.Simple.getTimesHandled(IMessageListener.DefaultListener.class));
        assertEquals(InstancesPerListener * ConcurrentUnits, MessageTypes.Simple.getTimesHandled(MessagesTypeListener.DefaultListener.class));

        bus.shutdown();
        pause(200);
    }


    @Test
    public void testSyncPublicationAsyncHandlers() throws Exception {
        ListenerFactory listeners = new ListenerFactory()
                .create(InstancesPerListener, Listeners.asynchronous())
                .create(InstancesPerListener, Listeners.noHandlers());
        final MBassador bus = createBus(SyncAsync(), listeners);

        final MessageManager messageManager = new MessageManager();
        Runnable publishAndCheck = new Runnable() {
            @Override
            public void run() {

                StandardMessage standardMessage = messageManager.createAndTrack(
                        StandardMessage.class,
                        InstancesPerListener,
                        Listeners.join(Listeners.asynchronous(),Listeners.handlesStandardMessage()));
                MultipartMessage multipartMessage = messageManager.create(
                        MultipartMessage.class,
                        InstancesPerListener,
                        IMessageListener.AsyncListener.class, IMultipartMessageListener.AsyncListener.class);

                bus.post(standardMessage).now();
                bus.post(multipartMessage).now();
                bus.post(MessageTypes.Simple).now();

            }
        };

        ConcurrentExecutor.runConcurrent(publishAndCheck, 1);
        messageManager.waitForMessages(waitForMessageTimeout);

        MessageTypes.resetAll();
        messageManager.register(MessageTypes.Simple, InstancesPerListener * ConcurrentUnits, IMessageListener.AsyncListener.class, MessagesTypeListener.AsyncListener.class);
        ConcurrentExecutor.runConcurrent(publishAndCheck, ConcurrentUnits);
        messageManager.waitForMessages(waitForMessageTimeout);

        bus.shutdown();
        pause(200);
    }

    @Test
    public void testAsynchronousMessagePublication() throws Exception {

        ListenerFactory listeners = new ListenerFactory()
                .create(InstancesPerListener, Listeners.asynchronous())
                .create(InstancesPerListener, Listeners.noHandlers());
        final MBassador bus = createBus(SyncAsync(), listeners);


        final MessageManager messageManager = new MessageManager();

        Runnable publishAndCheck = new Runnable() {
            @Override
            public void run() {
                StandardMessage standardMessage = messageManager.create(StandardMessage.class, InstancesPerListener, IMessageListener.AsyncListener.class);
                MultipartMessage multipartMessage = messageManager.create(MultipartMessage.class, InstancesPerListener, IMessageListener.AsyncListener.class);

                bus.post(standardMessage).asynchronously(1, TimeUnit.MILLISECONDS);
                bus.post(multipartMessage).asynchronously();
                bus.post(MessageTypes.Simple).asynchronously();

            }
        };

        ConcurrentExecutor.runConcurrent(publishAndCheck, 1);
        messageManager.waitForMessages(waitForMessageTimeout);

        MessageTypes.resetAll();
        ConcurrentExecutor.runConcurrent(publishAndCheck, ConcurrentUnits);
        messageManager.waitForMessages(waitForMessageTimeout);

        bus.shutdown();
        pause(200);

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

        //DS: Exception counter added via config
        IBusConfiguration config = SyncAsync(false)
            .addPublicationErrorHandler(ExceptionCounter);

        final MBassador bus = new MBassador(config);
        ListenerFactory listeners = new ListenerFactory()
                .create(InstancesPerListener, ExceptionThrowingListener.class);
        ConcurrentExecutor.runConcurrent(TestUtil.subscriber(bus, listeners), ConcurrentUnits);

        Runnable asynchronousPublication = new Runnable() {
            @Override
            public void run() {
                bus.post(new Object()).asynchronously();
            }
        };

        // single threaded
        ConcurrentExecutor.runConcurrent(asynchronousPublication, 1);
        pause(processingTimeInMS);
        assertEquals(InstancesPerListener, exceptionCount.get());


        // multi threaded
        exceptionCount.set(0);
        ConcurrentExecutor.runConcurrent(asynchronousPublication, ConcurrentUnits);
        pause(processingTimeInMS);
        assertEquals(InstancesPerListener * ConcurrentUnits, exceptionCount.get());

        bus.shutdown();
        pause(200);

    }




}
