package org.mbassy;

import org.junit.Test;
import org.mbassy.listener.Enveloped;
import org.mbassy.listener.Listener;
import org.mbassy.listener.MessageListenerMetadata;
import org.mbassy.listener.MetadataReader;
import org.mbassy.subscription.MessageEnvelope;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mbassy.listener.MessageListenerMetadata.ForMessage;

/**
 * Todo: Add javadoc
 *
 * @author bennidi
 *         Date: 12/16/12
 */
public class MetadataReaderTest extends UnitTest {

    private MetadataReader reader = new MetadataReader();

    @Test
    public void testListenerWithoutInheritance() {
        MessageListenerMetadata<EventListener1> listener = reader.getMessageListener(EventListener1.class);
        ListenerValidator validator = new ListenerValidator()
                .expectHandlers(2, String.class)
                .expectHandlers(2, Object.class)
                .expectHandlers(1, BufferedReader.class);
        validator.check(listener);
    }


    @Test
    public void testListenerWithInheritance() {
        MessageListenerMetadata<EventListener2> listener = reader.getMessageListener(EventListener2.class);
        ListenerValidator validator = new ListenerValidator()
                .expectHandlers(2, String.class)
                .expectHandlers(2, Object.class)
                .expectHandlers(1, BufferedReader.class);
        validator.check(listener);
    }

    @Test
    public void testListenerWithInheritanceOverriding() {
        MessageListenerMetadata<EventListener3> listener = reader.getMessageListener(EventListener3.class);

        ListenerValidator validator = new ListenerValidator()
                .expectHandlers(0, String.class)
                .expectHandlers(2, Object.class)
                .expectHandlers(0, BufferedReader.class);
        validator.check(listener);
    }

    @Test
    public void testEnveloped() {
        MessageListenerMetadata<EnvelopedListener> listener = reader.getMessageListener(EnvelopedListener.class);
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
        MessageListenerMetadata<EnvelopedListenerSubclass> listener = reader.getMessageListener(EnvelopedListenerSubclass.class);
        ListenerValidator validator = new ListenerValidator()
                .expectHandlers(1, String.class)
                .expectHandlers(2, Integer.class)
                .expectHandlers(1, Long.class)
                .expectHandlers(0, Double.class)
                .expectHandlers(0, Number.class);
        validator.check(listener);
    }


    private class ListenerValidator {

        private Map<Class<?>, Integer> handlers = new HashMap<Class<?>, Integer>();

        public ListenerValidator expectHandlers(Integer count, Class<?> messageType){
            handlers.put(messageType, count);
            return this;
        }

        public void check(MessageListenerMetadata listener){
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
    public class EventListener1 {

        @Listener(rejectSubtypes = true)
        public void handleObject(Object o) {

        }

        @Listener
        public void handleAny(Object o) {

        }


        @Listener
        public void handleString(String s) {

        }

    }

    // the same handlers as its super class
    public class EventListener2 extends EventListener1 {}

    public class EventListener3 extends EventListener2 {

        // narrow the handler
        @Listener(rejectSubtypes = true)
        public void handleAny(Object o) {

        }

        // remove this handler
        public void handleString(String s) {

        }

    }

    public class EnvelopedListener{


        @Listener(rejectSubtypes = true)
        @Enveloped(messages = {String.class, Integer.class, Long.class})
        public void handleEnveloped(MessageEnvelope o) {

        }

        @Listener
        @Enveloped(messages = {Number.class})
        public void handleEnveloped2(MessageEnvelope o) {

        }

    }

    public class EnvelopedListenerSubclass extends EnvelopedListener{

        // narrow to integer
        @Listener
        @Enveloped(messages = {Integer.class})
        public void handleEnveloped2(MessageEnvelope o) {

        }

    }

}
