package net.engio.mbassy;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.common.MessageBusTest;
import net.engio.mbassy.listener.*;
import net.engio.mbassy.subscription.SubscriptionContext;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Integration test suite for interface annotation inheritance feature.
 * Tests handler execution with multiple subscribed listeners and verifies
 * execution order based on priority settings.
 *
 * @author bennidi
 */
public class InterfaceInheritanceTest extends MessageBusTest {

    // ============= Test Messages =============

    public static class TestMessage {
        public final List<String> executionOrder = new ArrayList<>();
    }

    public static class PriorityTestMessage {
        public final List<String> executionOrder = new ArrayList<>();
    }

    // ============= Test 1: Priority Execution Order with Multiple Listeners =============

    interface HighPriorityHandler {
        @Handler(priority = 10)
        void handle(PriorityTestMessage message);
    }

    interface MediumPriorityHandler {
        @Handler(priority = 5)
        void handle(PriorityTestMessage message);
    }

    interface LowPriorityHandler {
        @Handler(priority = 1)
        void handle(PriorityTestMessage message);
    }

    public static class HighPriorityListener implements HighPriorityHandler {
        @Override
        public void handle(PriorityTestMessage message) {
            message.executionOrder.add("High");
        }
    }

    public static class MediumPriorityListener implements MediumPriorityHandler {
        @Override
        public void handle(PriorityTestMessage message) {
            message.executionOrder.add("Medium");
        }
    }

    public static class LowPriorityListener implements LowPriorityHandler {
        @Override
        public void handle(PriorityTestMessage message) {
            message.executionOrder.add("Low");
        }
    }

    @Test
    public void testPriorityBasedExecutionOrder() {
        MBassador bus = createBus(SyncAsync());

        // Subscribe in random order
        bus.subscribe(new LowPriorityListener());
        bus.subscribe(new HighPriorityListener());
        bus.subscribe(new MediumPriorityListener());

        PriorityTestMessage message = new PriorityTestMessage();
        bus.publish(message);

        // Verify execution order is based on priority (high to low)
        assertEquals(3, message.executionOrder.size());
        assertEquals("High", message.executionOrder.get(0));
        assertEquals("Medium", message.executionOrder.get(1));
        assertEquals("Low", message.executionOrder.get(2));
    }

    // ============= Test 2: Mixed Class and Interface Priorities =============

    interface InterfacePriority5 {
        @Handler(priority = 5)
        void handle(PriorityTestMessage message);
    }

    public static class OverridesToPriority8 implements InterfacePriority5 {
        @Override
        @Handler(priority = 8)  // Class overrides interface priority
        public void handle(PriorityTestMessage message) {
            message.executionOrder.add("Override8");
        }
    }

    public static class KeepsPriority5 implements InterfacePriority5 {
        @Override
        public void handle(PriorityTestMessage message) {
            message.executionOrder.add("Keep5");
        }
    }

    @Test
    public void testMixedInterfaceAndClassPriorities() {
        MBassador bus = createBus(SyncAsync());

        bus.subscribe(new HighPriorityListener());  // Priority 10
        bus.subscribe(new OverridesToPriority8());  // Priority 8 (overridden)
        bus.subscribe(new KeepsPriority5());        // Priority 5 (from interface)
        bus.subscribe(new LowPriorityListener());   // Priority 1

        PriorityTestMessage message = new PriorityTestMessage();
        bus.publish(message);

        // Verify execution order: 10 -> 8 -> 5 (x2 from diamond) -> 5 -> 1
        assertEquals("High", message.executionOrder.get(0));
        assertEquals("Override8", message.executionOrder.get(1));
        assertTrue(message.executionOrder.contains("Keep5"));
        assertEquals("Low", message.executionOrder.get(message.executionOrder.size() - 1));
    }

    // ============= Test 3: Multiple Handlers with Diamond Interfaces =============

    interface DiamondInterfaceA {
        @Handler(priority = 10)
        void handleDiamond(TestMessage message);
    }

    interface DiamondInterfaceB {
        @Handler(priority = 5)
        void handleDiamond(TestMessage message);
    }

    public static class DiamondListener implements DiamondInterfaceA, DiamondInterfaceB {
        @Override
        public void handleDiamond(TestMessage message) {
            message.executionOrder.add("Diamond");
        }
    }

    @Test
    public void testDiamondInterfaceExecutionOrder() {
        MBassador bus = createBus(SyncAsync());

        bus.subscribe(new DiamondListener());

        TestMessage message = new TestMessage();
        bus.publish(message);

        // Should be called twice - once for each interface
        assertEquals(2, message.executionOrder.size());
        assertEquals("Diamond", message.executionOrder.get(0));  // Priority 10
        assertEquals("Diamond", message.executionOrder.get(1));  // Priority 5
    }

    // ============= Test 4: Complex Multi-Listener Scenario =============

    interface EarlyHandler {
        @Handler(priority = 100)
        void handleEarly(TestMessage message);
    }

    interface LateHandler {
        @Handler(priority = 1)
        void handleLate(TestMessage message);
    }

    public static class EarlyListenerA implements EarlyHandler {
        @Override
        public void handleEarly(TestMessage message) {
            message.executionOrder.add("EarlyA");
        }
    }

    public static class EarlyListenerB implements EarlyHandler {
        @Override
        public void handleEarly(TestMessage message) {
            message.executionOrder.add("EarlyB");
        }
    }

    public static class LateListenerA implements LateHandler {
        @Override
        public void handleLate(TestMessage message) {
            message.executionOrder.add("LateA");
        }
    }

    public static class LateListenerB implements LateHandler {
        @Override
        public void handleLate(TestMessage message) {
            message.executionOrder.add("LateB");
        }
    }

    @Test
    public void testMultipleListenersSamePriority() {
        MBassador bus = createBus(SyncAsync());

        // Subscribe listeners with same priorities
        bus.subscribe(new LateListenerA());
        bus.subscribe(new EarlyListenerA());
        bus.subscribe(new LateListenerB());
        bus.subscribe(new EarlyListenerB());

        TestMessage message = new TestMessage();
        bus.publish(message);

        // Verify execution order
        assertEquals(4, message.executionOrder.size());

        // Early handlers (priority 100) should execute first
        assertTrue(message.executionOrder.get(0).startsWith("Early"));
        assertTrue(message.executionOrder.get(1).startsWith("Early"));

        // Late handlers (priority 1) should execute last
        assertTrue(message.executionOrder.get(2).startsWith("Late"));
        assertTrue(message.executionOrder.get(3).startsWith("Late"));
    }

    // ============= Helper Methods =============

    public static IBusConfiguration SyncAsync() {
        return MessageBusTest.SyncAsync(false)
                .addPublicationErrorHandler(new EmptyErrorHandler());
    }
}
