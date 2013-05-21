package net.engio.mbassy;

import net.engio.mbassy.bus.BusConfiguration;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.common.DeadMessage;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.messages.ITestMessage;
import net.engio.mbassy.messages.TestMessage3;

import org.junit.Assert;
import org.junit.Test;

/**
 * A test to make sure that Interface subscriptions are working correctly
 * 
 * @author durron597
 */
public class InterfaceTest {
	@Test
	public void testMBassador() {
		MBassador<ITestMessage> bus = new MBassador<ITestMessage>(BusConfiguration.Default());
		bus.subscribe(this);
		TestMessage3 myFoo = new TestMessage3();
		bus.publish(myFoo);
	}

	@Handler
	public void handleFoo(ITestMessage f) {
		Assert.assertTrue(f instanceof TestMessage3);
	}
	
	@Handler
	public void handleDead(DeadMessage d) {
		Assert.fail("This class should handle this message appropriately!");
	}
}
