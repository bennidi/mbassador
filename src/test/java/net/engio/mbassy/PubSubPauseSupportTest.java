package net.engio.mbassy;

import java.util.concurrent.atomic.AtomicInteger;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.common.PubSubPauseSupport.FlushMode;
import net.engio.mbassy.bus.publication.SyncAsyncPostCommand;
import net.engio.mbassy.common.ConcurrentExecutor;
import net.engio.mbassy.common.ListenerFactory;
import net.engio.mbassy.common.MessageBusTest;
import net.engio.mbassy.common.MessageManager;
import net.engio.mbassy.listeners.IMessageListener;
import net.engio.mbassy.listeners.IMultipartMessageListener;
import net.engio.mbassy.listeners.Listeners;
import net.engio.mbassy.listeners.MessagesTypeListener;
import net.engio.mbassy.messages.MessageTypes;
import net.engio.mbassy.messages.MultipartMessage;
import net.engio.mbassy.messages.StandardMessage;

import org.junit.After;
import org.junit.Test;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Brian Groenke [groenke.5@osu.edu]
 */
public class PubSubPauseSupportTest extends MessageBusTest {

    private static final Logger log = LoggerFactory.getLogger(PubSubPauseSupportTest.class);

    private MBassador bus;

    @After
    public void tearDown() {
        if (bus != null) bus.shutdown();
        bus.shutdown();
        pause(200);
    }

    @Test
    public void testUnpausedPublicationWithSyncHandlers() {
        final ListenerFactory listeners = new ListenerFactory().create(InstancesPerListener, Listeners.synchronous())
                                                               .create(InstancesPerListener, Listeners.noHandlers());
        bus = createBus(SyncAsync(), listeners);

        final Runnable publishAndCheck = new Runnable() {

            @Override
            public void run() {
                final StandardMessage standardMessage = new StandardMessage();
                final MultipartMessage multipartMessage = new MultipartMessage();

                bus.post(standardMessage).now();
                bus.post(multipartMessage).now();
                bus.post(MessageTypes.Simple).now();

                assertEquals(InstancesPerListener,
                             standardMessage.getTimesHandled(IMessageListener.DefaultListener.class));
                assertEquals(InstancesPerListener,
                             multipartMessage.getTimesHandled(IMessageListener.DefaultListener.class));
            }
        };

        // test single-threaded
        ConcurrentExecutor.runConcurrent(publishAndCheck, 1);

        // test multi-threaded
        MessageTypes.resetAll();
        ConcurrentExecutor.runConcurrent(publishAndCheck, ConcurrentUnits);
        assertEquals(InstancesPerListener * ConcurrentUnits,
                     MessageTypes.Simple.getTimesHandled(IMessageListener.DefaultListener.class));
        assertEquals(InstancesPerListener * ConcurrentUnits,
                     MessageTypes.Simple.getTimesHandled(MessagesTypeListener.DefaultListener.class));
    }

    @Test
    public void testPauseAtomicResumePublicationWithSyncHandlers() {
        final ListenerFactory listeners = new ListenerFactory().create(InstancesPerListener, Listeners.synchronous())
                                                               .create(InstancesPerListener, Listeners.noHandlers());
        bus = createBus(SyncAsync(), listeners);

        final Runnable publishAndCheck = new Runnable() {

            @Override
            public void run() {
                final StandardMessage standardMessage = new StandardMessage();
                final MultipartMessage multipartMessage = new MultipartMessage();

                final SyncAsyncPostCommand stdmsg = bus.post(standardMessage);
                final SyncAsyncPostCommand mpmsg = bus.post(multipartMessage);
                final SyncAsyncPostCommand smsg = bus.post(MessageTypes.Simple);

                synchronized (bus) {
                    bus.pause();

                    stdmsg.now();
                    mpmsg.now();
                    smsg.now();

                    assertEquals(0, standardMessage.getTimesHandled(IMessageListener.DefaultListener.class));
                    assertEquals(0, multipartMessage.getTimesHandled(IMessageListener.DefaultListener.class));

                    assertTrue(bus.resume()); // synchronous/atomic resume
                }

                assertEquals(InstancesPerListener,
                             standardMessage.getTimesHandled(IMessageListener.DefaultListener.class));
                assertEquals(InstancesPerListener,
                             multipartMessage.getTimesHandled(IMessageListener.DefaultListener.class));
            }
        };

        // test single-threaded
        ConcurrentExecutor.runConcurrent(publishAndCheck, 1);

        // test multi-threaded
        MessageTypes.resetAll();
        ConcurrentExecutor.runConcurrent(publishAndCheck, ConcurrentUnits);
        assertEquals(InstancesPerListener * ConcurrentUnits,
                     MessageTypes.Simple.getTimesHandled(IMessageListener.DefaultListener.class));
        assertEquals(InstancesPerListener * ConcurrentUnits,
                     MessageTypes.Simple.getTimesHandled(MessagesTypeListener.DefaultListener.class));
    }

    @Test
    public void testPauseNonatomicResumePublicationWithSyncHandlers() {
        final ListenerFactory listeners = new ListenerFactory().create(InstancesPerListener, Listeners.synchronous())
                                                               .create(InstancesPerListener, Listeners.noHandlers());
        bus = createBus(SyncAsync(), listeners);

        final Runnable publishAndCheck = new Runnable() {

            @Override
            public void run() {
                final StandardMessage standardMessage = new StandardMessage();
                final MultipartMessage multipartMessage = new MultipartMessage();

                final SyncAsyncPostCommand stdmsg = bus.post(standardMessage);
                final SyncAsyncPostCommand mpmsg = bus.post(multipartMessage);
                final SyncAsyncPostCommand smsg = bus.post(MessageTypes.Simple);

                final int numLeftInQueue;
                synchronized (bus) {
                    bus.pause();

                    stdmsg.now();
                    mpmsg.now();
                    smsg.now();

                    assertEquals(0, standardMessage.getTimesHandled(IMessageListener.DefaultListener.class));
                    assertEquals(0, multipartMessage.getTimesHandled(IMessageListener.DefaultListener.class));

                    bus.resume(FlushMode.NONATOMIC);
                    numLeftInQueue = bus.countInQueue();
                }

                // With nonatomic resume, some messages may or may not still be in the queue; so we need to check
                // that at least as many messages were published as there are handlers minus the outstanding queue size.
                final int handleCountLowerBound = Math.max(InstancesPerListener - numLeftInQueue, 0);
                assertTrue(standardMessage.getTimesHandled(IMessageListener.DefaultListener.class) >= handleCountLowerBound);
                assertTrue(multipartMessage.getTimesHandled(IMessageListener.DefaultListener.class) >= handleCountLowerBound);
            }
        };

        // test single-threaded
        ConcurrentExecutor.runConcurrent(publishAndCheck, 1);

        // test multi-threaded
        MessageTypes.resetAll();
        ConcurrentExecutor.runConcurrent(publishAndCheck, ConcurrentUnits);
        assertEquals(InstancesPerListener * ConcurrentUnits,
                     MessageTypes.Simple.getTimesHandled(IMessageListener.DefaultListener.class));
        assertEquals(InstancesPerListener * ConcurrentUnits,
                     MessageTypes.Simple.getTimesHandled(MessagesTypeListener.DefaultListener.class));
    }

    @Test
    public void testPauseAtomicResumePublicationWithAsyncHandlers() {
        final ListenerFactory listeners = new ListenerFactory().create(InstancesPerListener, Listeners.asynchronous())
                                                               .create(InstancesPerListener, Listeners.noHandlers());
        bus = createBus(SyncAsync(), listeners);

        final MessageManager messageManager = new MessageManager();
        final AtomicInteger expectedCount = new AtomicInteger(0);
        final Runnable publish = new Runnable() {
            @Override
            public void run() {

                final StandardMessage standardMessage = messageManager.createAndTrack(
                        StandardMessage.class,
                        expectedCount.get(),
                        Listeners.join(Listeners.asynchronous(),Listeners.handlesStandardMessage()));
                final MultipartMessage multipartMessage = messageManager.create(
                        MultipartMessage.class,
                        expectedCount.get(),
                        IMessageListener.AsyncListener.class, IMultipartMessageListener.AsyncListener.class);

                bus.post(standardMessage).now();
                bus.post(multipartMessage).now();
                bus.post(MessageTypes.Simple).now();
            }
        };

        bus.pause();
        ConcurrentExecutor.runConcurrent(publish, 1);
        messageManager.waitForMessages(waitForMessageTimeout);
        expectedCount.set(InstancesPerListener);
        ConcurrentExecutor.runConcurrent(publish, 1);
        bus.resume();
        messageManager.waitForMessages(waitForMessageTimeout);

        MessageTypes.resetAll();
        messageManager.register(MessageTypes.Simple, 0, IMessageListener.AsyncListener.class, MessagesTypeListener.AsyncListener.class);
        expectedCount.set(0);
        bus.pause();
        ConcurrentExecutor.runConcurrent(publish, ConcurrentUnits);
        messageManager.waitForMessages(waitForMessageTimeout);

        messageManager.register(MessageTypes.Simple, InstancesPerListener * ConcurrentUnits, IMessageListener.AsyncListener.class, MessagesTypeListener.AsyncListener.class);
        expectedCount.set(InstancesPerListener);
        bus.resume();
        messageManager.waitForMessages(waitForMessageTimeout);
    }
}
