package net.engio.mbassy;

import net.engio.mbassy.bus.IMessagePublication;
import net.engio.mbassy.bus.common.IMessageBus;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.common.MessageBusTest;
import net.engio.mbassy.listener.Handler;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Very simple test to verify dispatch to correct message handler
 *
 * @author bennidi
 *         Date: 1/17/13
 */
public class MethodDispatchTest extends MessageBusTest {

    private boolean listener1Called = false;
    private boolean listener2Called = false;

    // a simple event listener
    public class EventListener1 {

        @Handler
        public void handleString(String s) {
            listener1Called = true;
        }

    }

    // the same handlers as its super class
    public class EventListener2 extends EventListener1 {

        // redefine handler implementation (not configuration)
        public void handleString(String s) {
            listener2Called = true;
        }

    }

    @Test
    public void testDispatchWithSequentialSubscription() {
        IMessageBus bus = createBus(SyncAsync());
        EventListener2 listener2 = new EventListener2();
        bus.subscribe(listener2);
        bus.post("only one listener registered").now();
        assertTrue(listener2Called);
        assertFalse(listener1Called);

        EventListener1 listener1 = new EventListener1();
        bus.subscribe(listener1);
        bus.post("second listener registered and called").now();
        assertTrue(listener1Called);
    }

    @Test
    public void testAsyncDispatchAfterExceptionInErrorHandler() throws InterruptedException {
        IMessageBus bus = createBus(SyncAsync(false /*configures an error handler that throws exception*/).addFeature(Feature.AsynchronousMessageDispatch.Default().setNumberOfMessageDispatchers(1)));
        final AtomicInteger msgHandlerCounter = new AtomicInteger(0);
        bus.subscribe(new Object() {
            @Handler
            public void handleAndThrowException(String s) throws Exception {
                msgHandlerCounter.incrementAndGet();
                throw new Exception("error in msg handler on call no. " + msgHandlerCounter.get());
            }
        });
        IMessagePublication first = bus.post("first event - event handler will raise exception followed by another exception in the error handler").asynchronously();
        IMessagePublication second = bus.post("second event - expecting that msg dispatcher will still dispatch this after encountering exception in error handler").asynchronously();
        pause(200);
        assertEquals("msg handler is called also on the 2nd event after an exception in error handler following 1st event error", 2, msgHandlerCounter.get());
        assertFalse("no more messages left to process", bus.hasPendingMessages());
        assertTrue(first.hasError());
        assertTrue(first.isFinished());
        assertNotNull(first.getError().toString());
        assertEquals(first.getError().getPublishedMessage(),"first event - event handler will raise exception followed by another exception in the error handler" );
        assertTrue(second.hasError());
        assertTrue(second.isFinished());
        assertNotNull(second.getError().toString());
    }
}
