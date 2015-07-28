package net.engio.mbassy;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.publication.SyncAsyncPostCommand;
import net.engio.mbassy.common.ConcurrentExecutor;
import net.engio.mbassy.common.ListenerFactory;
import net.engio.mbassy.common.MessageBusTest;
import net.engio.mbassy.listeners.IMessageListener;
import net.engio.mbassy.listeners.Listeners;
import net.engio.mbassy.listeners.MessagesTypeListener;
import net.engio.mbassy.messages.MessageTypes;
import net.engio.mbassy.messages.MultipartMessage;
import net.engio.mbassy.messages.StandardMessage;

import org.junit.After;
import org.junit.Test;

public class PubSubPauseSupportTest extends MessageBusTest {

    private MBassador bus;

    @After
    public void tearDown ()
    {
	if (bus != null) bus.shutdown();
        bus.shutdown();
        pause(200);
    }

    @Test
    public void testSyncWithSyncHandlersUnpausedPublication ()
    {
        final ListenerFactory listeners = new ListenerFactory()
                .create(InstancesPerListener, Listeners.synchronous())
                .create(InstancesPerListener, Listeners.noHandlers());
        bus = createBus (SyncAsync (), listeners);

        final Runnable publishAndCheck = new Runnable() {
            @Override
            public void run() {
                final StandardMessage standardMessage = new StandardMessage();
                final MultipartMessage multipartMessage = new MultipartMessage();

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
    }

    @Test
    public void testSyncWithSyncHandlersPauseAndResumePublication ()
    {
	final ListenerFactory listeners = new ListenerFactory()
		.create(InstancesPerListener, Listeners.synchronous())
		.create(InstancesPerListener, Listeners.noHandlers());
	bus = createBus (SyncAsync (), listeners);

	final Runnable publishAndCheck = new Runnable() {
	    @Override
	    public void run() {
		final StandardMessage standardMessage = new StandardMessage();
		final MultipartMessage multipartMessage = new MultipartMessage();

		final SyncAsyncPostCommand stdmsg = bus.post(standardMessage);
		final SyncAsyncPostCommand mpmsg = bus.post(multipartMessage);
		final SyncAsyncPostCommand smsg = bus.post(MessageTypes.Simple);

		bus.pause();

		stdmsg.now();
		mpmsg.now();
		smsg.now();

		assertEquals(0, standardMessage.getTimesHandled(IMessageListener.DefaultListener.class));
		assertEquals(0, multipartMessage.getTimesHandled(IMessageListener.DefaultListener.class));

		bus.resume();

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
    }
}
