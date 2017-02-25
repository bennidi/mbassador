package net.engio.mbassy;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.SyncMessageBus;
import net.engio.mbassy.bus.common.GenericMessagePublicationSupport;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;
import net.engio.mbassy.common.ConcurrentExecutor;
import net.engio.mbassy.common.ListenerFactory;
import net.engio.mbassy.common.MessageBusTest;
import net.engio.mbassy.common.TestUtil;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listeners.CustomInvocationListener;
import net.engio.mbassy.listeners.ExceptionThrowingListener;
import net.engio.mbassy.listeners.IMessageListener;
import net.engio.mbassy.listeners.MessagesTypeListener;
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


    protected abstract GenericMessagePublicationSupport getSyncMessageBus(boolean failOnException, IPublicationErrorHandler errorHandler);

    protected abstract GenericMessagePublicationSupport getSyncMessageBus(boolean failOnException);

    @Test
    public void testSynchronousMessagePublication() throws Exception {

        final GenericMessagePublicationSupport bus = getSyncMessageBus(true);
        ListenerFactory listeners = new ListenerFactory()
                .create(InstancesPerListener, IMessageListener.DefaultListener.class)
                .create(InstancesPerListener, IMessageListener.DisabledListener.class)
                .create(InstancesPerListener, MessagesTypeListener.DefaultListener.class)
                .create(InstancesPerListener, MessagesTypeListener.DisabledListener.class)
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
        assertEquals(InstancesPerListener * ConcurrentUnits, MessageTypes.Simple.getTimesHandled(MessagesTypeListener.DefaultListener.class));
        assertEquals(InstancesPerListener * ConcurrentUnits, MessageTypes.Multipart.getTimesHandled(MessagesTypeListener.DefaultListener.class));
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

        //DS: modified to pass ExceptionCounter via the configuration object
        final GenericMessagePublicationSupport bus = getSyncMessageBus(false,ExceptionCounter);
        ListenerFactory listeners = new ListenerFactory()
                .create(InstancesPerListener, ExceptionThrowingListener.class);

        ConcurrentExecutor.runConcurrent(TestUtil.subscriber(bus, listeners), ConcurrentUnits);

        Runnable publish = new Runnable() {
            @Override
            public void run() {
                bus.post(new Object()).now();
            }
        };

        // single threaded
        ConcurrentExecutor.runConcurrent(publish, 1);
        assertEquals(InstancesPerListener, exceptionCount.get());
        exceptionCount.set(0); // reset for next test

        // multi threaded
        ConcurrentExecutor.runConcurrent(publish, ConcurrentUnits);
        assertEquals(InstancesPerListener * ConcurrentUnits, exceptionCount.get());
    }


    @Test
    public void testCustomHandlerInvocation(){
        final GenericMessagePublicationSupport bus = getSyncMessageBus(true);
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

    @Test
    public void testHandlerPriorities(){
        final GenericMessagePublicationSupport bus = getSyncMessageBus(true);
        ListenerFactory listeners = new ListenerFactory()
                .create(InstancesPerListener, PrioritizedListener.class)
                .create(InstancesPerListener, Object.class);

        ConcurrentExecutor.runConcurrent(TestUtil.subscriber(bus, listeners), ConcurrentUnits);

        Runnable publishAndCheck = new Runnable() {
            @Override
            public void run() {
                bus.post(new IncrementingMessage()).now();
            }
        };

        // single threaded
        ConcurrentExecutor.runConcurrent(publishAndCheck, 1);

        // multi threaded
        ConcurrentExecutor.runConcurrent(publishAndCheck, ConcurrentUnits);

    }


    public static class MBassadorTest extends SyncBusTest {

        //DS: added errorHandler parameter to allow adding handler from caller
        @Override
        protected GenericMessagePublicationSupport getSyncMessageBus(boolean failOnException, IPublicationErrorHandler errorHandler) {
            IBusConfiguration asyncFIFOConfig = new BusConfiguration().addPublicationErrorHandler(new AssertionErrorHandler(failOnException));
            asyncFIFOConfig.addFeature(Feature.SyncPubSub.Default());
            asyncFIFOConfig.addFeature(Feature.AsynchronousHandlerInvocation.Default(1, 1));
            asyncFIFOConfig.addFeature(Feature.AsynchronousMessageDispatch.Default().setNumberOfMessageDispatchers(1));
            if (errorHandler != null) {
                asyncFIFOConfig.addPublicationErrorHandler(errorHandler);
            }
            return new MBassador(asyncFIFOConfig);

        }


        @Override
        protected GenericMessagePublicationSupport getSyncMessageBus(boolean failOnException) {
            return getSyncMessageBus(failOnException, null);
        }

    }

    public static class SyncMessageBusTest extends SyncBusTest {


        @Override
        protected GenericMessagePublicationSupport getSyncMessageBus(boolean failOnException, IPublicationErrorHandler errorHandler) {
            IBusConfiguration syncPubSubCfg = new BusConfiguration().addPublicationErrorHandler(new AssertionErrorHandler(failOnException));
            syncPubSubCfg.addFeature(Feature.SyncPubSub.Default());
            if (errorHandler != null) {
                syncPubSubCfg.addPublicationErrorHandler(errorHandler);
            }
            return new SyncMessageBus(syncPubSubCfg);
        }

        @Override
        protected GenericMessagePublicationSupport getSyncMessageBus(boolean failOnException) {
            return getSyncMessageBus(failOnException, null);
        }
    }





    static class IncrementingMessage{

        private int count = 1;

        public void markHandled(int newVal){
            // only transitions by the next handler are allowed
            if(count == newVal || count + 1 == newVal) count = newVal;
            else throw new RuntimeException("Message was handled out of order");
        }
    }


    public static class PrioritizedListener{

        @Handler(priority = Integer.MIN_VALUE)
        public void handle1(IncrementingMessage message) {
            message.markHandled(4);
        }

        @Handler(priority = -2)
        public void handle2(IncrementingMessage message) {
            message.markHandled(3);
        }

        @Handler
        public void handle3(IncrementingMessage message) {
            message.markHandled(2);
        }

        @Handler(priority = Integer.MAX_VALUE)
        public void handle4(IncrementingMessage message) {
            message.markHandled(1);
        }


    }



}
