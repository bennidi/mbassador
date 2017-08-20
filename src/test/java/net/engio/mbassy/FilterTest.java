package net.engio.mbassy;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.common.DeadMessage;
import net.engio.mbassy.bus.common.FilteredMessage;
import net.engio.mbassy.common.ListenerFactory;
import net.engio.mbassy.common.MessageBusTest;
import net.engio.mbassy.common.TestUtil;
import net.engio.mbassy.listener.*;
import net.engio.mbassy.messages.SubTestMessage;
import net.engio.mbassy.messages.TestMessage;
import net.engio.mbassy.subscription.SubscriptionContext;
import org.junit.Test;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Testing of filter functionality
 *
 * @author bennidi
 *         Date: 11/26/12
 */
public class FilterTest extends MessageBusTest {

    private static final AtomicInteger FilteredEventCounter = new AtomicInteger(0);
    private static final AtomicInteger DeadEventCounter = new AtomicInteger(0);

    @Test
    public void testSubclassFilter() throws Exception {
        FilteredEventCounter.set(0);
        DeadEventCounter.set(0);

        MBassador bus = createBus(SyncAsync());
        ListenerFactory listenerFactory = new ListenerFactory()
                .create(100, FilteredMessageListener.class);

        List<Object> listeners = listenerFactory.getAll();

        // this will subscribe the listeners concurrently to the bus
        TestUtil.setup(bus, listeners, 10);

        TestMessage message = new TestMessage();
        TestMessage subTestMessage = new SubTestMessage();

        bus.post(message).now();
        bus.post(subTestMessage).now();

        assertEquals(100, message.counter.get());
        assertEquals(0, subTestMessage.counter.get());
        assertEquals(100, FilteredEventCounter.get());
    }

    @Test
    public void testFilteredFilteredEvent() throws Exception {
        FilteredEventCounter.set(0);
        DeadEventCounter.set(0);

        MBassador bus = createBus(SyncAsync());
        ListenerFactory listenerFactory = new ListenerFactory()
                .create(100, FilteredMessageListener.class);

        List<Object> listeners = listenerFactory.getAll();

        // this will subscribe the listeners concurrently to the bus
        TestUtil.setup(bus, listeners, 10);

        bus.post(new Object()).now();
        bus.post(new SubTestMessage()).now();

        assertEquals(100, FilteredEventCounter.get()); // the SubTestMessage should have been republished as a filtered event
        assertEquals(100, DeadEventCounter.get()); // Object.class was filtered and the fil
    }

    public static class FilteredMessageListener{

        // NOTE: Use rejectSubtypes property of @Handler to achieve the same functionality but with better performance
        // and more concise syntax
        @Handler(filters = {@Filter(Filters.RejectSubtypes.class)})
        public void handleTestMessage(TestMessage message){
            message.counter.incrementAndGet();
        }

        // FilteredEvents that contain messages of class Object will be filtered (again) and should cause a DeadEvent to be thrown
        @Handler
        @RejectFiltered
        public void handleFilteredEvent(FilteredMessage filtered){
            FilteredEventCounter.incrementAndGet();
        }

        // will cause republication of a FilteredEvent
        @Handler
        @RejectAll
        public void handleNone(Object any){
            FilteredEventCounter.incrementAndGet();
        }

        // will cause republication of a FilteredEvent
        @Handler
        public void handleDead(DeadMessage dead){
            DeadEventCounter.incrementAndGet();
        }
    }

    @Test
    public void testSubtypesOnly(){
        MBassador bus = createBus(SyncAsync());
        ListenerFactory listeners = new ListenerFactory()
                .create(100, TestMessageHandler.class);

        // this will subscribe the listeners concurrently to the bus
        TestUtil.setup(bus, listeners, 10);

        TestMessage supertype = new TestMessage();
        TestMessage subtype = new SubTestMessage();

        bus.publish(supertype);
        bus.publish(subtype);

        assertEquals(100, subtype.counter.get());
        assertEquals(0, supertype.counter.get());

    }

    public static class TestMessageHandler{

        @Handler(filters = @Filter(Filters.SubtypesOnly.class))
        public void handle(TestMessage message){
            message.counter.incrementAndGet();
        }

    }

    public static class RejectFilteredObjectsFilter implements IMessageFilter{

        @Override
        public boolean accepts(Object message, SubscriptionContext context) {
            if(message.getClass().equals(FilteredMessage.class) && ((FilteredMessage)message).getMessage().getClass().equals(Object.class)){
                return false;
            }
            return true;
        }
    }

    public static final class RejectAllFilter implements IMessageFilter {

        @Override
        public boolean accepts(Object event,  SubscriptionContext context) {
            return false;
        }
    }

    @Filter(RejectFilteredObjectsFilter.class)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RejectFiltered {

    }

    @IncludeFilters({@Filter(RejectAllFilter.class)})
    @Retention(RetentionPolicy.RUNTIME)
    public @interface RejectAll {

    }

}
