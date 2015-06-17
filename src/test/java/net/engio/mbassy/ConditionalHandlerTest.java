package net.engio.mbassy;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.common.MessageBusTest;
import net.engio.mbassy.listener.Enveloped;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;
import net.engio.mbassy.subscription.MessageEnvelope;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

/*****************************************************************************
 * Some unit tests for the "condition" filter.
 ****************************************************************************/

public class ConditionalHandlerTest extends MessageBusTest {


	@Test
	public void testSimpleStringCondition(){
		MBassador bus = createBus(SyncAsync());
		bus.subscribe(new ConditionalMessageListener());

		TestEvent message = new TestEvent("TEST", 0);
		bus.publish(message);

		assertTrue(message.wasHandledBy("handleTypeMessage", "handleEnvelopedMessage"));
        assertFalse(message.wasHandledBy("handleInvalidEL"));
	}

	@Test
	public void testSimpleNumberCondition(){
		MBassador bus =  createBus(SyncAsync());
		bus.subscribe(new ConditionalMessageListener());

		TestEvent message = new TestEvent("", 5);
		bus.publish(message);

		assertTrue(message.wasHandledBy("handleSizeMessage"));
        assertFalse(message.wasHandledBy("handleInvalidEL"));
	}

	@Test
	public void testHandleCombinedEL(){
		MBassador bus = createBus(SyncAsync());
		bus.subscribe(new ConditionalMessageListener());

		TestEvent message = new TestEvent("", 3);
		bus.publish(message);

        assertTrue(message.wasHandledBy("handleCombinedEL"));
        assertFalse(message.wasHandledBy("handleInvalidEL"));
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
	public void testHandleMethodAccessEL(){
		MBassador bus = createBus(SyncAsync());
		bus.subscribe(new ConditionalMessageListener());

		TestEvent message = new TestEvent("XYZ", 1);
		bus.publish(message);

        assertTrue(message.wasHandledBy("handleMethodAccessEL"));
        assertFalse(message.wasHandledBy("handleInvalidEL"));

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

    @Listener(references = References.Strong)
    public static class ConditionalMessageListener {

        @Handler(condition = "msg.type == 'TEST'")
        public void handleTypeMessage(TestEvent message) {
            message.handledBy("handleTypeMessage");
        }

        @Handler(condition = "msg.size > 4")
        public void handleSizeMessage(TestEvent message) {
            message.handledBy("handleSizeMessage");
        }

        @Handler(condition = "msg.foo > 4")
        public void handleInvalidEL(TestEvent message) {
            message.handledBy("handleInvalidEL");
        }

        @Handler(condition = "msg.size > 2 && msg.size < 4")
        public void handleCombinedEL(TestEvent message) {
            message.handledBy( "handleCombinedEL");
        }

        @Handler(condition = "msg.getType().equals('XYZ') && msg.getSize() == 1")
        public void handleMethodAccessEL(TestEvent message) {
            message.handledBy("handleMethodAccessEL");
        }

        @Handler(condition = "msg.type == 'TEST'")
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
