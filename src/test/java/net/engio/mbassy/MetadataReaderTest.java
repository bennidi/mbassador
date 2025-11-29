package net.engio.mbassy;

import net.engio.mbassy.common.AssertSupport;
import net.engio.mbassy.listener.MessageListener;
import net.engio.mbassy.listener.MessageHandler;
import net.engio.mbassy.listeners.SimpleHandler;
import org.junit.Test;
import net.engio.mbassy.listener.Enveloped;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.MetadataReader;
import net.engio.mbassy.subscription.MessageEnvelope;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.engio.mbassy.listener.MessageListener.ForMessage;

/**
 *
 * @author bennidi
 *         Date: 12/16/12
 */
public class MetadataReaderTest extends AssertSupport {

    private MetadataReader reader = new MetadataReader();

    @Test
    public void testListenerWithoutInheritance() {
        MessageListener<MessageListener1> listener = reader.getMessageListener(MessageListener1.class);
        ListenerValidator validator = new ListenerValidator()
                .expectHandlers(2, String.class)
                .expectHandlers(2, Object.class)
                .expectHandlers(1, BufferedReader.class);
        validator.check(listener);
    }

    @Test
    public void testInterfaced() {
        MessageListener listener = reader.getMessageListener(InterfacedListener.class);
        ListenerValidator validator = new ListenerValidator()
                .expectHandlers(1, String.class);
        validator.check(listener);
    }

    @Test
    public void testInterfacedEnveloped() {
        MessageListener listener = reader.getMessageListener(EnvelopedInterfacedListener.class);
        ListenerValidator validator = new ListenerValidator()
                .expectHandlers(1, Integer.class);
        validator.check(listener);
    }


    @Test
    public void testListenerWithInheritance() {
        MessageListener<MessageListener2> listener = reader.getMessageListener(MessageListener2.class);
        ListenerValidator validator = new ListenerValidator()
                .expectHandlers(2, String.class)
                .expectHandlers(2, Object.class)
                .expectHandlers(1, BufferedReader.class);
        validator.check(listener);
    }

    @Test
    public void testListenerWithInheritanceOverriding() {
        MessageListener<MessageListener3> listener = reader.getMessageListener(MessageListener3.class);

        ListenerValidator validator = new ListenerValidator()
                .expectHandlers(0, String.class)
                .expectHandlers(2, Object.class)
                .expectHandlers(0, BufferedReader.class);
        validator.check(listener);
    }

    @Test
    public void testEnveloped() {
        MessageListener<EnvelopedListener> listener = reader.getMessageListener(EnvelopedListener.class);
        ListenerValidator validator = new ListenerValidator()
                .expectHandlers(1, String.class)
                .expectHandlers(2, Integer.class)
                .expectHandlers(2, Long.class)
                .expectHandlers(1, Double.class)
                .expectHandlers(1, Number.class)
                .expectHandlers(0, List.class);
        validator.check(listener);
    }

    @Test
    public void testEnvelopedSubclass() {
        MessageListener<EnvelopedListenerSubclass> listener = reader.getMessageListener(EnvelopedListenerSubclass.class);
        ListenerValidator validator = new ListenerValidator()
                .expectHandlers(1, String.class)
                .expectHandlers(2, Integer.class)
                .expectHandlers(1, Long.class)
                .expectHandlers(0, Double.class)
                .expectHandlers(0, Number.class);
        validator.check(listener);
    }

    @Test
    public void testAnonymousListener() {
       SimpleHandler anonymousSimpleHandler = new SimpleHandler() {
           @Override
           @Handler
           public void onMessage(Object msg) {
               // nop
           }
       };
        MessageListener<EnvelopedListenerSubclass> listener = reader.getMessageListener(anonymousSimpleHandler.getClass());
        ListenerValidator validator = new ListenerValidator()
                .expectHandlers(1, Object.class);
        validator.check(listener);
    }

    // ============= Interface Annotation Inheritance Tests =============

    @Test
    public void testDiamondProblemAB() {
        // When implementing DiamondA, DiamondB - creates handlers from both interfaces
        MessageListener listener = reader.getMessageListener(DiamondImplementationAB.class);
        ListenerValidator validator = new ListenerValidator()
                .expectHandlers(2, String.class);  // One from each interface
        validator.check(listener);

        // Both handlers should exist with their respective priorities
        List<MessageHandler> handlers = listener.getHandlers(ForMessage(String.class));
        assertEquals(2, handlers.size());
        // Verify we have both priorities
        boolean hasPriority1 = handlers.stream().anyMatch(h -> h.getPriority() == 1);
        boolean hasPriority5 = handlers.stream().anyMatch(h -> h.getPriority() == 5);
        assertTrue(hasPriority1);
        assertTrue(hasPriority5);
    }

    @Test
    public void testDiamondProblemBA() {
        // When implementing DiamondB, DiamondA - creates handlers from both interfaces
        MessageListener listener = reader.getMessageListener(DiamondImplementationBA.class);
        ListenerValidator validator = new ListenerValidator()
                .expectHandlers(2, String.class);  // One from each interface
        validator.check(listener);

        // Both handlers should exist with their respective priorities
        List<MessageHandler> handlers = listener.getHandlers(ForMessage(String.class));
        assertEquals(2, handlers.size());
        // Verify we have both priorities (order doesn't affect which handlers are created)
        boolean hasPriority1 = handlers.stream().anyMatch(h -> h.getPriority() == 1);
        boolean hasPriority5 = handlers.stream().anyMatch(h -> h.getPriority() == 5);
        assertTrue(hasPriority1);
        assertTrue(hasPriority5);
    }

    @Test
    public void testClassAnnotationOverridesInterface() {
        // Class annotation should take precedence over interface annotation
        MessageListener listener = reader.getMessageListener(ClassOverridesInterface.class);
        ListenerValidator validator = new ListenerValidator()
                .expectHandlers(1, String.class);
        validator.check(listener);

        // Verify priority from class (10) is used, not interface (1)
        List<MessageHandler> handlers = listener.getHandlers(ForMessage(String.class));
        assertEquals(10, handlers.get(0).getPriority());
    }

    @Test
    public void testDeepInterfaceHierarchy() {
        // Extended interface should override base interface
        MessageListener listener = reader.getMessageListener(DeepHierarchyImpl.class);
        ListenerValidator validator = new ListenerValidator()
                .expectHandlers(1, String.class);
        validator.check(listener);

        // Verify priority from ExtendedInterface (10), not BaseInterface (1)
        List<MessageHandler> handlers = listener.getHandlers(ForMessage(String.class));
        assertEquals(10, handlers.get(0).getPriority());
    }

    @Test
    public void testMultipleInterfacesDifferentMethods() {
        // Should have handlers from both interfaces
        MessageListener listener = reader.getMessageListener(MultiInterfaceImpl.class);
        ListenerValidator validator = new ListenerValidator()
                .expectHandlers(1, String.class)
                .expectHandlers(1, Integer.class);
        validator.check(listener);
    }

    @Test
    public void testDisabledHandlerFromInterface() {
        // Disabled handler in interface should not be registered
        MessageListener listener = reader.getMessageListener(DisabledInterfaceImpl.class);
        ListenerValidator validator = new ListenerValidator()
                .expectHandlers(0, String.class);
        validator.check(listener);
    }

    @Test
    public void testClassEnablesDisabledInterfaceHandler() {
        // Class can re-enable a disabled interface handler
        MessageListener listener = reader.getMessageListener(EnablesDisabledHandler.class);
        ListenerValidator validator = new ListenerValidator()
                .expectHandlers(1, String.class);
        validator.check(listener);
    }

    @Test
    public void testRejectSubtypesFromInterface() {
        // rejectSubtypes should be inherited from interface
        MessageListener listener = reader.getMessageListener(RejectSubtypesImpl.class);
        ListenerValidator validator = new ListenerValidator()
                .expectHandlers(1, Number.class)
                .expectHandlers(0, Integer.class)  // Should be rejected
                .expectHandlers(0, Double.class);   // Should be rejected
        validator.check(listener);

        // Verify rejectSubtypes is true (acceptsSubtypes is false)
        List<MessageHandler> handlers = listener.getHandlers(ForMessage(Number.class));
        assertFalse(handlers.get(0).acceptsSubtypes());
    }

    @Test
    public void testClassOverridesRejectSubtypes() {
        // Class can override rejectSubtypes from interface
        MessageListener listener = reader.getMessageListener(AcceptsSubtypesImpl.class);
        ListenerValidator validator = new ListenerValidator()
                .expectHandlers(1, Number.class)
                .expectHandlers(1, Integer.class)   // Should be accepted
                .expectHandlers(1, Double.class);    // Should be accepted
        validator.check(listener);

        // Verify rejectSubtypes is false (acceptsSubtypes is true)
        List<MessageHandler> handlers = listener.getHandlers(ForMessage(Number.class));
        assertTrue(handlers.get(0).acceptsSubtypes());
    }

    @Test
    public void testEnvelopedFromInterface() {
        // Enveloped annotation should be inherited from interface
        MessageListener listener = reader.getMessageListener(EnvelopedInterfacedListener.class);
        ListenerValidator validator = new ListenerValidator()
                .expectHandlers(1, Integer.class)
                .expectHandlers(0, String.class);  // Not in enveloped messages
        validator.check(listener);
    }

    @Test
    public void testMixedInterfaceAndClassHandlers() {
        // Should have handlers from both interface and class-defined methods
        MessageListener listener = reader.getMessageListener(MixedHandlersImpl.class);
        ListenerValidator validator = new ListenerValidator()
                .expectHandlers(1, String.class)   // From interface
                .expectHandlers(1, Integer.class);  // From class
        validator.check(listener);
    }

    @Test
    public void testNoHandlerInterfaceMethod() {
        // Interface method without @Handler should not create handler
        MessageListener listener = reader.getMessageListener(NoHandlerInterfaceImpl.class);
        ListenerValidator validator = new ListenerValidator()
                .expectHandlers(0, String.class);
        validator.check(listener);
    }

    @Test
    public void testTripleDiamondABC() {
        // With three interfaces - creates handlers from all three
        MessageListener listener = reader.getMessageListener(TripleDiamondABC.class);
        ListenerValidator validator = new ListenerValidator()
                .expectHandlers(3, String.class);  // One from each interface
        validator.check(listener);

        // All three handlers should exist with their respective priorities
        List<MessageHandler> handlers = listener.getHandlers(ForMessage(String.class));
        assertEquals(3, handlers.size());
        // Verify we have all three priorities
        boolean hasPriority1 = handlers.stream().anyMatch(h -> h.getPriority() == 1);
        boolean hasPriority2 = handlers.stream().anyMatch(h -> h.getPriority() == 2);
        boolean hasPriority3 = handlers.stream().anyMatch(h -> h.getPriority() == 3);
        assertTrue(hasPriority1);
        assertTrue(hasPriority2);
        assertTrue(hasPriority3);
    }

    @Test
    public void testTripleDiamondCBA() {
        // With three interfaces in reverse order - creates handlers from all three
        MessageListener listener = reader.getMessageListener(TripleDiamondCBA.class);
        ListenerValidator validator = new ListenerValidator()
                .expectHandlers(3, String.class);  // One from each interface
        validator.check(listener);

        // All three handlers should exist (order doesn't matter)
        List<MessageHandler> handlers = listener.getHandlers(ForMessage(String.class));
        assertEquals(3, handlers.size());
        // Verify we have all three priorities
        boolean hasPriority1 = handlers.stream().anyMatch(h -> h.getPriority() == 1);
        boolean hasPriority2 = handlers.stream().anyMatch(h -> h.getPriority() == 2);
        boolean hasPriority3 = handlers.stream().anyMatch(h -> h.getPriority() == 3);
        assertTrue(hasPriority1);
        assertTrue(hasPriority2);
        assertTrue(hasPriority3);
    }


    // Define and assert expectations on handlers in a listener
    private class ListenerValidator {

        private Map<Class<?>, Integer> handlers = new HashMap<Class<?>, Integer>();

        public ListenerValidator expectHandlers(Integer count, Class<?> messageType){
            handlers.put(messageType, count);
            return this;
        }

        public void check(MessageListener listener){
            for(Map.Entry<Class<?>, Integer> expectedHandler: handlers.entrySet()){
                if(expectedHandler.getValue() > 0){
                    assertTrue(listener.handles(expectedHandler.getKey()));
                }
                else{
                    assertFalse(listener.handles(expectedHandler.getKey()));
                }
                assertEquals(expectedHandler.getValue(), listener.getHandlers(ForMessage(expectedHandler.getKey())).size());
            }
        }

    }


    // a simple event listener
    public class MessageListener1 {

        @Handler(rejectSubtypes = true)
        public void handleObject(Object o) {

        }

        @Handler
        public void handleAny(Object o) {

        }


        @Handler
        public void handleString(String s) {

        }

    }

    // the same handlers as its super class
    public class MessageListener2 extends MessageListener1 {

        // redefine handler implementation (not configuration)
        public void handleString(String s) {

        }

    }

    public class MessageListener3 extends MessageListener2 {

        // narrow the handler
        @Handler(rejectSubtypes = true)
        public void handleAny(Object o) {

        }

        @Handler(enabled = false)
        public void handleString(String s) {

        }

    }

    public class EnvelopedListener{


        @Handler(rejectSubtypes = true)
        @Enveloped(messages = {String.class, Integer.class, Long.class})
        public void handleEnveloped(MessageEnvelope o) {

        }

        @Handler
        @Enveloped(messages = Number.class)
        public void handleEnveloped2(MessageEnvelope o) {

        }

    }

    public class EnvelopedListenerSubclass extends EnvelopedListener{

        // narrow to integer
        @Handler
        @Enveloped(messages = Integer.class)
        public void handleEnveloped2(MessageEnvelope o) {

        }

    }

    public interface EnvelopedListenerInterface {

        @Handler
        @Enveloped(messages = Integer.class)
        void handle(MessageEnvelope envelope);
    }

    public class EnvelopedInterfacedListener implements EnvelopedListenerInterface {

        @Override
        public void handle(MessageEnvelope envelope) {

        }
    }

    public interface ListenerInterface {

        @Handler
        void handle(String str);
    }

    public class InterfacedListener implements ListenerInterface {

        @Override
        public void handle(String str) {

        }
    }

    // ============= Diamond Problem Test Classes =============

    public interface DiamondA {
        @Handler(priority = 1)
        void handleDiamond(String message);
    }

    public interface DiamondB {
        @Handler(priority = 5)
        void handleDiamond(String message);
    }

    public class DiamondImplementationAB implements DiamondA, DiamondB {
        @Override
        public void handleDiamond(String message) {
        }
    }

    public class DiamondImplementationBA implements DiamondB, DiamondA {
        @Override
        public void handleDiamond(String message) {
        }
    }

    // ============= Class Override Test Classes =============

    public interface OverrideInterface {
        @Handler(priority = 1)
        void handleOverride(String message);
    }

    public class ClassOverridesInterface implements OverrideInterface {
        @Override
        @Handler(priority = 10)
        public void handleOverride(String message) {
        }
    }

    // ============= Deep Hierarchy Test Classes =============

    public interface BaseInterface {
        @Handler(priority = 1)
        void handleDeep(String message);
    }

    public interface ExtendedInterface extends BaseInterface {
        @Override
        @Handler(priority = 10)
        void handleDeep(String message);
    }

    public class DeepHierarchyImpl implements ExtendedInterface {
        @Override
        public void handleDeep(String message) {
        }
    }

    // ============= Multiple Interfaces Test Classes =============

    public interface InterfaceA {
        @Handler
        void handleA(String message);
    }

    public interface InterfaceB {
        @Handler
        void handleB(Integer message);
    }

    public class MultiInterfaceImpl implements InterfaceA, InterfaceB {
        @Override
        public void handleA(String message) {
        }

        @Override
        public void handleB(Integer message) {
        }
    }

    // ============= Disabled Handler Test Classes =============

    public interface DisabledInterface {
        @Handler(enabled = false)
        void handleDisabled(String message);
    }

    public class DisabledInterfaceImpl implements DisabledInterface {
        @Override
        public void handleDisabled(String message) {
        }
    }

    public class EnablesDisabledHandler implements DisabledInterface {
        @Override
        @Handler(enabled = true)
        public void handleDisabled(String message) {
        }
    }

    // ============= RejectSubtypes Test Classes =============

    public interface RejectSubtypesInterface {
        @Handler(rejectSubtypes = true)
        void handleNumber(Number message);
    }

    public class RejectSubtypesImpl implements RejectSubtypesInterface {
        @Override
        public void handleNumber(Number message) {
        }
    }

    public class AcceptsSubtypesImpl implements RejectSubtypesInterface {
        @Override
        @Handler(rejectSubtypes = false)
        public void handleNumber(Number message) {
        }
    }

    // ============= Mixed Handlers Test Classes =============

    public interface MixedInterface {
        @Handler
        void fromInterface(String message);
    }

    public class MixedHandlersImpl implements MixedInterface {
        @Override
        public void fromInterface(String message) {
        }

        @Handler
        public void fromClass(Integer message) {
        }
    }

    // ============= No Handler Interface Test Classes =============

    public interface NoHandlerInterface {
        void notAHandler(String message);
    }

    public class NoHandlerInterfaceImpl implements NoHandlerInterface {
        @Override
        public void notAHandler(String message) {
        }
    }

    // ============= Triple Diamond Test Classes =============

    public interface TripleA {
        @Handler(priority = 1)
        void handleTriple(String message);
    }

    public interface TripleB {
        @Handler(priority = 2)
        void handleTriple(String message);
    }

    public interface TripleC {
        @Handler(priority = 3)
        void handleTriple(String message);
    }

    public class TripleDiamondABC implements TripleA, TripleB, TripleC {
        @Override
        public void handleTriple(String message) {
        }
    }

    public class TripleDiamondCBA implements TripleC, TripleB, TripleA {
        @Override
        public void handleTriple(String message) {
        }
    }

}
