package net.engio.mbassy;

import static net.engio.mbassy.listener.MessageListener.ForMessage;

import java.io.BufferedReader;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.engio.mbassy.common.AssertSupport;
import net.engio.mbassy.common.IPredicate;
import net.engio.mbassy.listener.Enveloped;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.MessageHandler;
import net.engio.mbassy.listener.MessageListener;
import net.engio.mbassy.listener.MetadataReader;
import net.engio.mbassy.subscription.MessageEnvelope;

import org.junit.Test;

/**
 *
 * @author bennidi
 *         Date: 12/16/12
 */
public class MetadataReaderTest extends AssertSupport {

    private MetadataReader reader = new MetadataReader();

    @Test
    public void testListenerWithoutInheritance() {
        MessageListener<MessageListener1> listener = this.reader.getMessageListener(MessageListener1.class);
        ListenerValidator validator = new ListenerValidator()
                .expectHandlers(2, String.class)
                .expectHandlers(2, Object.class)
                .expectHandlers(1, BufferedReader.class);
        validator.check(listener);
    }

    /*
    public void testInterfaced() {
        MessageListener listener = reader.getMessageListener(InterfacedListener.class);
        ListenerValidator validator = new ListenerValidator()
                .expectHandlers(1, Object.class);
        validator.check(listener);
    }  WIP */


    @Test
    public void testListenerWithInheritance() {
        MessageListener<MessageListener2> listener = this.reader.getMessageListener(MessageListener2.class);
        ListenerValidator validator = new ListenerValidator()
                .expectHandlers(2, String.class)
                .expectHandlers(2, Object.class)
                .expectHandlers(1, BufferedReader.class);
        validator.check(listener);
    }

    @Test
    public void testListenerWithInheritanceOverriding() {
        MessageListener<MessageListener3> listener = this.reader.getMessageListener(MessageListener3.class);

        ListenerValidator validator = new ListenerValidator()
                .expectHandlers(0, String.class)
                .expectHandlers(2, Object.class)
                .expectHandlers(0, BufferedReader.class);
        validator.check(listener);
    }

    @Test
    public void testEnveloped() {
        MessageListener<EnvelopedListener> listener = this.reader.getMessageListener(EnvelopedListener.class);
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
        MessageListener<EnvelopedListenerSubclass> listener = this.reader.getMessageListener(EnvelopedListenerSubclass.class);
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
            this.handlers.put(messageType, count);
            return this;
        }

        public void check(MessageListener listener){
            for(Map.Entry<Class<?>, Integer> expectedHandler: this.handlers.entrySet()){
                Class<?> key = expectedHandler.getKey();

                if(expectedHandler.getValue() > 0){
                    assertTrue(!getHandlers(listener, ForMessage(key)).isEmpty());
                }
                else{
                    assertFalse(!getHandlers(listener, ForMessage(key)).isEmpty());
                }
                assertEquals(expectedHandler.getValue(), getHandlers(listener, ForMessage(key)).size());
            }
        }

    }

    public List<MessageHandler> getHandlers(MessageListener<MessageHandler> listener, IPredicate<MessageHandler> filter) {
        List<MessageHandler> matching = new LinkedList<MessageHandler>();
        for (MessageHandler handler : listener.getHandlers()) {
            if (filter.apply(handler)) {
                matching.add(handler);
            }
        }
        return matching;
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
        @Override
        public void handleString(String s) {

        }

    }

    public class MessageListener3 extends MessageListener2 {

        // narrow the handler
        @Override
        @Handler(rejectSubtypes = true)
        public void handleAny(Object o) {

        }

        @Override
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
        @Override
        @Handler
        @Enveloped(messages = Integer.class)
        public void handleEnveloped2(MessageEnvelope o) {

        }

    }

    public static interface ListenerInterface{

        @Handler
        @Enveloped(messages = Object.class)
        void handle(MessageEnvelope envelope);
    }

    public class InterfacedListener implements  ListenerInterface{

        @Override
        public void handle(MessageEnvelope envelope) {
            //
        }
    }

}
