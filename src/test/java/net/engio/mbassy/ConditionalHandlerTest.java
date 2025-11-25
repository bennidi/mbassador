package net.engio.mbassy;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.common.MessageBusTest;
import net.engio.mbassy.listener.*;
import net.engio.mbassy.subscription.MessageEnvelope;
import net.engio.mbassy.subscription.SubscriptionContext;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/*****************************************************************************
 * Unit tests for the lambda-based filter functionality (replacing EL conditions).
 ****************************************************************************/

public class ConditionalHandlerTest extends MessageBusTest {


	@Test
	public void testSimpleStringCondition(){
		MBassador bus = createBus(SyncAsync());
		bus.subscribe(new ConditionalMessageListener());

		TestEvent message = new TestEvent("TEST", 0);
		bus.publish(message);

		assertTrue(message.wasHandledBy("handleTypeMessage", "handleEnvelopedMessage"));
        assertFalse(message.wasHandledBy("handleInvalidProperty"));
	}

	@Test
	public void testSimpleNumberCondition(){
		MBassador bus =  createBus(SyncAsync());
		bus.subscribe(new ConditionalMessageListener());

		TestEvent message = new TestEvent("", 5);
		bus.publish(message);

		assertTrue(message.wasHandledBy("handleSizeMessage"));
        assertFalse(message.wasHandledBy("handleInvalidProperty"));
	}

	@Test
	public void testHandleCombinedFilter(){
		MBassador bus = createBus(SyncAsync());
		bus.subscribe(new ConditionalMessageListener());

		TestEvent message = new TestEvent("", 3);
		bus.publish(message);

        assertTrue(message.wasHandledBy("handleCombinedFilter"));
        assertFalse(message.wasHandledBy("handleInvalidProperty"));
	}

	@Test
	public void testNotMatchingAnyCondition(){
		MBassador bus = createBus(SyncAsync());
		bus.subscribe(new ConditionalMessageListener());

		TestEvent message = new TestEvent("", 0);
		bus.publish(message);

		assertTrue(message.handledBy.isEmpty());
	}

	@Test
	public void testHandleMethodAccessFilter(){
		MBassador bus = createBus(SyncAsync());
		bus.subscribe(new ConditionalMessageListener());

		TestEvent message = new TestEvent("XYZ", 1);
		bus.publish(message);

        assertTrue(message.wasHandledBy("handleMethodAccessFilter"));
        assertFalse(message.wasHandledBy("handleInvalidProperty"));

    }

    public static class TestEvent {

        private Set<String> handledBy = new HashSet<String>();
        private String type;
        private int size;

        public TestEvent(String type, int size) {
            super();
            this.type = type;
            this.size = size;
        }

        public String getType() {
            return type;
        }

        public int getSize() {
            return size;
        }

        public boolean wasHandledBy(String ...handlers){
            for(String handler : handlers){
                if (!handledBy.contains(handler)) return false;
            }
            return true;
        }

        public void handledBy(String handler){
            handledBy.add(handler);
        }

    }

    // Filter implementations using lambda-style classes

    public static class TestTypeFilter implements IMessageFilter<TestEvent> {
        @Override
        public boolean accepts(TestEvent message, SubscriptionContext context) {
            return "TEST".equals(message.getType());
        }
    }

    public static class SizeGreaterThan4Filter implements IMessageFilter<TestEvent> {
        @Override
        public boolean accepts(TestEvent message, SubscriptionContext context) {
            return message.getSize() > 4;
        }
    }

    public static class InvalidPropertyFilter implements IMessageFilter<TestEvent> {
        @Override
        public boolean accepts(TestEvent message, SubscriptionContext context) {
            // This simulates accessing an invalid property - always returns false
            // In the old EL version this would be "msg.foo > 4" where foo doesn't exist
            return false;
        }
    }

    public static class SizeBetween2And4Filter implements IMessageFilter<TestEvent> {
        @Override
        public boolean accepts(TestEvent message, SubscriptionContext context) {
            return message.getSize() > 2 && message.getSize() < 4;
        }
    }

    public static class XYZTypeAndSize1Filter implements IMessageFilter<TestEvent> {
        @Override
        public boolean accepts(TestEvent message, SubscriptionContext context) {
            return "XYZ".equals(message.getType()) && message.getSize() == 1;
        }
    }

    @Listener(references = References.Strong)
    public static class ConditionalMessageListener {

        @Handler(filters = @Filter(TestTypeFilter.class))
        public void handleTypeMessage(TestEvent message) {
            message.handledBy("handleTypeMessage");
        }

        @Handler(filters = @Filter(SizeGreaterThan4Filter.class))
        public void handleSizeMessage(TestEvent message) {
            message.handledBy("handleSizeMessage");
        }

        @Handler(filters = @Filter(InvalidPropertyFilter.class))
        public void handleInvalidProperty(TestEvent message) {
            message.handledBy("handleInvalidProperty");
        }

        @Handler(filters = @Filter(SizeBetween2And4Filter.class))
        public void handleCombinedFilter(TestEvent message) {
            message.handledBy("handleCombinedFilter");
        }

        @Handler(filters = @Filter(XYZTypeAndSize1Filter.class))
        public void handleMethodAccessFilter(TestEvent message) {
            message.handledBy("handleMethodAccessFilter");
        }

        @Handler(filters = @Filter(TestTypeFilter.class))
        @Enveloped(messages = {TestEvent.class, Object.class})
        public void handleEnvelopedMessage(MessageEnvelope envelope) {
            envelope.<TestEvent>getMessage().handledBy("handleEnvelopedMessage");
        }

    }

    public static IBusConfiguration SyncAsync() {
        return MessageBusTest.SyncAsync(false)
                .addPublicationErrorHandler(new EmptyErrorHandler());
    }



}
