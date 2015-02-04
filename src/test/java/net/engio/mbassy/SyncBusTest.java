package net.engio.mbassy;

import java.util.concurrent.atomic.AtomicInteger;

import net.engio.mbassy._misc.BusFactory;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.common.IMessageBus;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;
import net.engio.mbassy.common.ConcurrentExecutor;
import net.engio.mbassy.common.ListenerFactory;
import net.engio.mbassy.common.MessageBusTest;
import net.engio.mbassy.common.TestUtil;
import net.engio.mbassy.listeners.ExceptionThrowingListener;
import net.engio.mbassy.listeners.IMessageListener;
import net.engio.mbassy.listeners.MessagesListener;
import net.engio.mbassy.messages.MessageTypes;
import net.engio.mbassy.messages.MultipartMessage;
import net.engio.mbassy.messages.StandardMessage;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test synchronous and asynchronous dispatch in single and multi-threaded scenario.
 *
 * @author bennidi
 *         Date: 2/8/12
 */
public abstract class SyncBusTest extends MessageBusTest {


    protected abstract IMessageBus getSyncMessageBus();

    @Test
    public void testSynchronousMessagePublication() throws Exception {

        final IMessageBus bus = getSyncMessageBus();
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

                bus.publish(standardMessage);
                bus.publish(multipartMessage);
                bus.publish(MessageTypes.Simple);
                bus.publish(MessageTypes.Multipart);

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

        final IMessageBus bus = getSyncMessageBus();
        bus.addErrorHandler(ExceptionCounter);
        ListenerFactory listeners = new ListenerFactory()
                .create(InstancesPerListener, ExceptionThrowingListener.class);

        ConcurrentExecutor.runConcurrent(TestUtil.subscriber(bus, listeners), ConcurrentUnits);

        Runnable publish = new Runnable() {
            @Override
            public void run() {
                bus.publish(new StandardMessage());
            }
        };

        // single threaded
        ConcurrentExecutor.runConcurrent(publish, 1);

        exceptionCount.set(0);

        // multi threaded
        ConcurrentExecutor.runConcurrent(publish, ConcurrentUnits);
        assertEquals(InstancesPerListener * ConcurrentUnits, exceptionCount.get());
    }


    public static class MBassadorTest extends SyncBusTest {


        @Override
        protected IMessageBus getSyncMessageBus() {
            return new MBassador();
        }

    }

    public static class SyncMessageBusTest extends SyncBusTest {


        @Override
        protected IMessageBus getSyncMessageBus() {
            return BusFactory.SynchronousOnly();
        }
    }



    static class IncrementingMessage{

        private int count = 1;

        public void markHandled(int newVal){
            // only transitions by the next handler are allowed
            if(this.count == newVal || this.count + 1 == newVal) {
                this.count = newVal;
            } else {
                Assert.fail("Message was handled out of order");
            }
        }
    }

}
