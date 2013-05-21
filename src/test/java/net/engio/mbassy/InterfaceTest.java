package net.engio.mbassy;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import net.engio.mbassy.bus.BusConfiguration;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.common.DeadMessage;
import net.engio.mbassy.common.ReflectionUtils;
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
	private int itestCount = 0;
	private int cloneCount = 0;
	
	@Test
	public void testInterfaces() {
		Class[] testCases = { TestMessage3.class, CloneMessage.class };
		for (Class testClazz : testCases) {
			Collection<Class> classes = ReflectionUtils.getSuperclasses(testClazz);
			Set<Class> noDups = new HashSet<Class>();
			
			for(Class clazz : classes) {
				if (noDups.contains(clazz)) Assert.fail("ReflectionUtils.getSuperclasses should not have duplicates: " + testClazz.getName());
				noDups.add(clazz);
			}
		}
		
		MBassador<ITestMessage> bus = new MBassador<ITestMessage>(BusConfiguration.Default());
		MBassador<Cloneable> cloneBus = new MBassador<Cloneable>(BusConfiguration.Default());
		bus.subscribe(this);
		cloneBus.subscribe(this);
		
		TestMessage3 test = new TestMessage3();
		CloneMessage clone = new CloneMessage();
		
		bus.publish(test);
		bus.publish(clone);
		cloneBus.publish(clone);
		
		Assert.assertEquals(3, itestCount);
		Assert.assertEquals(2, cloneCount);
	}

	@Handler
	public void handleITest(ITestMessage f) {
		itestCount++;
		Assert.assertTrue(f instanceof TestMessage3);
	}
	
	@Handler
	public void handleClone(Cloneable f) {
		cloneCount++;
		Assert.assertTrue(f instanceof TestMessage3);
		Assert.assertTrue(f instanceof ITestMessage);
	}
	
	@Handler
	public void handleDead(DeadMessage d) {
		Assert.fail("This class should handle this message appropriately!");
	}
	
	public static class CloneMessage extends TestMessage3 implements Cloneable, ITestMessage { }
}
