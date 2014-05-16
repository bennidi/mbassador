package net.engio.mbassy;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.common.MessageBusTest;
import net.engio.mbassy.listener.Handler;

import org.junit.Test;

/*****************************************************************************
 * Some unit tests for the "condition" filter.
 ****************************************************************************/

public class ConditionTest extends MessageBusTest {

	public static class TestEvent {

		public Object result;
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
		
	}

	public static class ConditionalMessageListener {

		@Handler(condition = "msg.type == \"TEST\"")
		public void handleTypeMessage(TestEvent message) {
			message.result = "handleTypeMessage";
		}

		@Handler(condition = "msg.size > 4")
		public void handleSizeMessage(TestEvent message) {
			message.result = "handleSizeMessage";
		}
		
		@Handler(condition = "msg.size > 2 && msg.size < 4")
		public void handleCombinedEL(TestEvent message) {
			message.result = "handleCombinedEL";
		}
		
		@Handler(condition = "msg.getType().equals(\"XYZ\") && msg.getSize() == 1")
		public void handleMethodAccessEL(TestEvent message) {
			message.result = "handleMethodAccessEL";
		}

		
	}

	/*************************************************************************
	 * @throws Exception
	 ************************************************************************/
	@Test
	public void testSimpleStringCondition() throws Exception {
		MBassador bus = getBus(BusConfiguration.Default());
		bus.subscribe(new ConditionalMessageListener());

		TestEvent message = new TestEvent("TEST", 0);
		bus.publish(message);

		assertEquals("handleTypeMessage", message.result);
	}
	
	/*************************************************************************
	 * @throws Exception
	 ************************************************************************/
	@Test
	public void testSimpleNumberCondition() throws Exception {
		MBassador bus = getBus(BusConfiguration.Default());
		bus.subscribe(new ConditionalMessageListener());

		TestEvent message = new TestEvent("", 5);
		bus.publish(message);

		assertEquals("handleSizeMessage", message.result);
	}
	
	/*************************************************************************
	 * @throws Exception
	 ************************************************************************/
	@Test
	public void testHandleCombinedEL() throws Exception {
		MBassador bus = getBus(BusConfiguration.Default());
		bus.subscribe(new ConditionalMessageListener());

		TestEvent message = new TestEvent("", 3);
		bus.publish(message);

		assertEquals("handleCombinedEL", message.result);
	}
	
	/*************************************************************************
	 * @throws Exception
	 ************************************************************************/
	@Test
	public void testNotMatchingAnyCondition() throws Exception {
		MBassador bus = getBus(BusConfiguration.Default());
		bus.subscribe(new ConditionalMessageListener());

		TestEvent message = new TestEvent("", 0);
		bus.publish(message);

		assertTrue(message.result == null);
	}
	
	/*************************************************************************
	 * @throws Exception
	 ************************************************************************/
	@Test
	public void testHandleMethodAccessEL() throws Exception {
		MBassador bus = getBus(BusConfiguration.Default());
		bus.subscribe(new ConditionalMessageListener());

		TestEvent message = new TestEvent("XYZ", 1);
		bus.publish(message);

		assertEquals("handleMethodAccessEL", message.result);
	}

}
