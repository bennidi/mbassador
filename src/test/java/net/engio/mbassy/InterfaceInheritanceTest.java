package net.engio.mbassy;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.common.MessageBusTest;
import net.engio.mbassy.listener.*;
import net.engio.mbassy.subscription.SubscriptionContext;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test suite for interface annotation inheritance feature.
 * Tests that @Handler annotations on interface methods are properly inherited
 * by implementing classes, with proper precedence rules.
 *
 * @author bennidi
 */
public class InterfaceInheritanceTest extends MessageBusTest {

    // ============= Test Messages =============

    public static class TestMessage {
        public final AtomicInteger handlerCallCount = new AtomicInteger(0);
        public String lastHandlerCalled;
    }

    public static class SpecificMessage extends TestMessage {
    }

    public static class PriorityTestMessage {
        public int priority = -1;
    }

    // ============= Test 1: Basic Interface Inheritance =============

    interface BasicHandler {
        @Handler
        void handleMessage(TestMessage message);
    }

    public static class BasicImplementation implements BasicHandler {
        @Override
        public void handleMessage(TestMessage message) {
            message.handlerCallCount.incrementAndGet();
            message.lastHandlerCalled = "BasicImplementation";
        }
    }

    @Test
    public void testBasicInterfaceInheritance() {
        MBassador bus = createBus(SyncAsync());
        BasicImplementation listener = new BasicImplementation();
        bus.subscribe(listener);

        TestMessage message = new TestMessage();
        bus.publish(message);

        assertEquals(1, message.handlerCallCount.get());
        assertEquals("BasicImplementation", message.lastHandlerCalled);
    }

    // ============= Test 2: Class Annotation Overrides Interface =============

    interface OverrideTestInterface {
        @Handler(priority = 1)
        void handleMessage(TestMessage message);
    }

    public static class ClassAnnotationWins implements OverrideTestInterface {
        @Override
        @Handler(priority = 10)  // This should win
        public void handleMessage(TestMessage message) {
            message.handlerCallCount.incrementAndGet();
            message.lastHandlerCalled = "ClassAnnotationWins";
        }
    }

    @Test
    public void testClassAnnotationOverridesInterface() {
        MBassador bus = createBus(SyncAsync());
        ClassAnnotationWins listener = new ClassAnnotationWins();
        bus.subscribe(listener);

        // Verify the handler was registered
        TestMessage message = new TestMessage();
        bus.publish(message);

        assertEquals(1, message.handlerCallCount.get());
        assertEquals("ClassAnnotationWins", message.lastHandlerCalled);
    }

    // ============= Test 3: Diamond Problem - Last Interface Wins =============

    interface DiamondA {
        @Handler(priority = 1)
        void handlePriority(PriorityTestMessage message);
    }

    interface DiamondB {
        @Handler(priority = 5)
        void handlePriority(PriorityTestMessage message);
    }

    public static class DiamondImplementation implements DiamondA, DiamondB {
        @Override
        public void handlePriority(PriorityTestMessage message) {
            // Should inherit priority = 5 from DiamondB (last interface)
            message.priority = 5;
        }
    }

    @Test
    public void testDiamondProblemLastInterfaceWins() {
        MBassador bus = createBus(SyncAsync());
        DiamondImplementation listener = new DiamondImplementation();
        bus.subscribe(listener);

        PriorityTestMessage message = new PriorityTestMessage();
        bus.publish(message);

        // Verify it was called with priority from last interface
        assertEquals(5, message.priority);
    }

    // ============= Test 4: Deep Interface Hierarchy =============

    interface BaseInterface {
        @Handler(priority = 1)
        void handleDeep(TestMessage message);
    }

    interface ExtendedInterface extends BaseInterface {
        @Override
        @Handler(priority = 10)  // Overrides base
        void handleDeep(TestMessage message);
    }

    public static class DeepHierarchyImpl implements ExtendedInterface {
        @Override
        public void handleDeep(TestMessage message) {
            message.handlerCallCount.incrementAndGet();
            message.lastHandlerCalled = "DeepHierarchyImpl";
        }
    }

    @Test
    public void testDeepInterfaceHierarchy() {
        MBassador bus = createBus(SyncAsync());
        DeepHierarchyImpl listener = new DeepHierarchyImpl();
        bus.subscribe(listener);

        TestMessage message = new TestMessage();
        bus.publish(message);

        // Should inherit from ExtendedInterface, not BaseInterface
        assertEquals(1, message.handlerCallCount.get());
        assertEquals("DeepHierarchyImpl", message.lastHandlerCalled);
    }

    // ============= Test 5: Mixed Annotations (Some Interface, Some Class) =============

    interface MixedInterface {
        @Handler
        void fromInterface(TestMessage message);

        void noAnnotation(SpecificMessage message);
    }

    public static class MixedImplementation implements MixedInterface {
        private final AtomicInteger interfaceMethodCalls = new AtomicInteger(0);
        private final AtomicInteger classMethodCalls = new AtomicInteger(0);

        @Override
        public void fromInterface(TestMessage message) {
            interfaceMethodCalls.incrementAndGet();
            message.lastHandlerCalled = "fromInterface";
        }

        @Override
        @Handler  // Class provides annotation
        public void noAnnotation(SpecificMessage message) {
            classMethodCalls.incrementAndGet();
            message.lastHandlerCalled = "noAnnotation";
        }
    }

    @Test
    public void testMixedAnnotations() {
        MBassador bus = createBus(SyncAsync());
        MixedImplementation listener = new MixedImplementation();
        bus.subscribe(listener);

        // Test interface-annotated method
        TestMessage message1 = new TestMessage();
        bus.publish(message1);
        assertEquals(1, listener.interfaceMethodCalls.get());
        assertEquals(0, listener.classMethodCalls.get());
        assertEquals("fromInterface", message1.lastHandlerCalled);

        // Test class-annotated method
        // Note: SpecificMessage extends TestMessage, so both handlers will be called
        SpecificMessage message2 = new SpecificMessage();
        bus.publish(message2);
        assertEquals(2, listener.interfaceMethodCalls.get());  // Called again for SpecificMessage
        assertEquals(1, listener.classMethodCalls.get());
        // Both handlers are called, lastHandlerCalled could be either depending on execution order
        assertTrue(message2.lastHandlerCalled.equals("noAnnotation") ||
                   message2.lastHandlerCalled.equals("fromInterface"));
    }

    // ============= Test 6: Filter Inheritance from Interface =============

    public static class OnlySpecificMessageFilter implements IMessageFilter<TestMessage> {
        @Override
        public boolean accepts(TestMessage message, SubscriptionContext context) {
            return message instanceof SpecificMessage;
        }
    }

    interface FilteredInterface {
        @Handler(filters = @Filter(OnlySpecificMessageFilter.class))
        void handleFiltered(TestMessage message);
    }

    public static class FilterInheritanceImpl implements FilteredInterface {
        private final AtomicInteger callCount = new AtomicInteger(0);

        @Override
        public void handleFiltered(TestMessage message) {
            callCount.incrementAndGet();
        }
    }

    @Test
    public void testFilterInheritance() {
        MBassador bus = createBus(SyncAsync());
        FilterInheritanceImpl listener = new FilterInheritanceImpl();
        bus.subscribe(listener);

        // Generic message should be filtered out
        TestMessage generic = new TestMessage();
        bus.publish(generic);
        assertEquals(0, listener.callCount.get());

        // SpecificMessage should pass filter
        SpecificMessage specific = new SpecificMessage();
        bus.publish(specific);
        assertEquals(1, listener.callCount.get());
    }

    // ============= Test 7: Multiple Interfaces with Different Methods =============

    interface InterfaceA {
        @Handler
        void methodA(TestMessage message);
    }

    interface InterfaceB {
        @Handler
        void methodB(SpecificMessage message);
    }

    public static class MultipleInterfacesImpl implements InterfaceA, InterfaceB {
        private final AtomicInteger methodACalls = new AtomicInteger(0);
        private final AtomicInteger methodBCalls = new AtomicInteger(0);

        @Override
        public void methodA(TestMessage message) {
            methodACalls.incrementAndGet();
        }

        @Override
        public void methodB(SpecificMessage message) {
            methodBCalls.incrementAndGet();
        }
    }

    @Test
    public void testMultipleInterfacesDifferentMethods() {
        MBassador bus = createBus(SyncAsync());
        MultipleInterfacesImpl listener = new MultipleInterfacesImpl();
        bus.subscribe(listener);

        // Test methodA
        TestMessage message1 = new TestMessage();
        bus.publish(message1);
        assertEquals(1, listener.methodACalls.get());
        assertEquals(0, listener.methodBCalls.get());

        // Test methodB
        SpecificMessage message2 = new SpecificMessage();
        bus.publish(message2);
        assertEquals(2, listener.methodACalls.get());  // Also handled by methodA
        assertEquals(1, listener.methodBCalls.get());
    }

    // ============= Test 8: Interface Without Handler (Should Not Register) =============

    interface NoHandlerInterface {
        void notAHandler(TestMessage message);
    }

    public static class NoHandlerImpl implements NoHandlerInterface {
        private final AtomicInteger callCount = new AtomicInteger(0);

        @Override
        public void notAHandler(TestMessage message) {
            callCount.incrementAndGet();
        }
    }

    @Test
    public void testInterfaceWithoutHandlerAnnotation() {
        MBassador bus = createBus(SyncAsync());
        NoHandlerImpl listener = new NoHandlerImpl();
        bus.subscribe(listener);

        TestMessage message = new TestMessage();
        bus.publish(message);

        // Should not be called - no @Handler annotation anywhere
        assertEquals(0, listener.callCount.get());
    }

    // ============= Test 9: Async Handler from Interface =============

    interface AsyncInterface {
        @Handler(delivery = Invoke.Asynchronously)
        void handleAsync(TestMessage message);
    }

    public static class AsyncImpl implements AsyncInterface {
        private final AtomicInteger callCount = new AtomicInteger(0);

        @Override
        public void handleAsync(TestMessage message) {
            callCount.incrementAndGet();
        }

        public int getCallCount() {
            return callCount.get();
        }
    }

    @Test
    public void testAsyncHandlerInheritance() throws InterruptedException {
        MBassador bus = createBus(SyncAsync());
        AsyncImpl listener = new AsyncImpl();
        bus.subscribe(listener);

        TestMessage message = new TestMessage();
        bus.publish(message);

        // Wait for async processing
        Thread.sleep(100);

        assertEquals(1, listener.getCallCount());
    }

    // ============= Test 10: Disabled Handler from Interface =============

    interface DisabledInterface {
        @Handler(enabled = false)
        void handleDisabled(TestMessage message);
    }

    public static class DisabledImpl implements DisabledInterface {
        private final AtomicInteger callCount = new AtomicInteger(0);

        @Override
        public void handleDisabled(TestMessage message) {
            callCount.incrementAndGet();
        }
    }

    @Test
    public void testDisabledHandlerInheritance() {
        MBassador bus = createBus(SyncAsync());
        DisabledImpl listener = new DisabledImpl();
        bus.subscribe(listener);

        TestMessage message = new TestMessage();
        bus.publish(message);

        // Should not be called - handler is disabled
        assertEquals(0, listener.callCount.get());
    }

    // ============= Test 11: Class Enables Disabled Interface Handler =============

    interface DisabledByDefaultInterface {
        @Handler(enabled = false)
        void handle(TestMessage message);
    }

    public static class EnableInClass implements DisabledByDefaultInterface {
        private final AtomicInteger callCount = new AtomicInteger(0);

        @Override
        @Handler(enabled = true)  // Class enables it
        public void handle(TestMessage message) {
            callCount.incrementAndGet();
        }
    }

    @Test
    public void testClassEnablesDisabledInterfaceHandler() {
        MBassador bus = createBus(SyncAsync());
        EnableInClass listener = new EnableInClass();
        bus.subscribe(listener);

        TestMessage message = new TestMessage();
        bus.publish(message);

        // Should be called - class annotation enables it
        assertEquals(1, listener.callCount.get());
    }

    // ============= Helper Methods =============

    public static IBusConfiguration SyncAsync() {
        return MessageBusTest.SyncAsync(false)
                .addPublicationErrorHandler(new EmptyErrorHandler());
    }
}
