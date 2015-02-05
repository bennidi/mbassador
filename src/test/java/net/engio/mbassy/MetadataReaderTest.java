package net.engio.mbassy;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import net.engio.mbassy.annotations.Handler;
import net.engio.mbassy.common.AssertSupport;
import net.engio.mbassy.listener.MessageHandler;
import net.engio.mbassy.listener.MessageListener;
import net.engio.mbassy.listener.MetadataReader;

import org.junit.Test;

/**
 *
 * @author bennidi
 *         Date: 12/16/12
 */
public class MetadataReaderTest extends AssertSupport {

    private MetadataReader reader = new MetadataReader();

//    @Test
//    public void testListenerWithoutInheritance() {
//        MessageListener listener = this.reader.getMessageListener(MessageListener1.class);
//        ListenerValidator validator = new ListenerValidator()
//                .expectHandlers(2, String.class)
//                .expectHandlers(2, Object.class)
//                .expectHandlers(1, BufferedReader.class);
//        validator.check(listener);
//    }
//
//    /*
//    public void testInterfaced() {
//        MessageListener listener = reader.getMessageListener(InterfacedListener.class);
//        ListenerValidator validator = new ListenerValidator()
//                .expectHandlers(1, Object.class);
//        validator.check(listener);
//    }  WIP */
//
//
//    @Test
//    public void testListenerWithInheritance() {
//        MessageListener listener = this.reader.getMessageListener(MessageListener2.class);
//        ListenerValidator validator = new ListenerValidator()
//                .expectHandlers(2, String.class)
//                .expectHandlers(2, Object.class)
//                .expectHandlers(1, BufferedReader.class);
//        validator.check(listener);
//    }
//
//    @Test
//    public void testListenerWithInheritanceOverriding() {
//        MessageListener listener = this.reader.getMessageListener(MessageListener3.class);
//
//        ListenerValidator validator = new ListenerValidator()
//                .expectHandlers(0, String.class)
//                .expectHandlers(2, Object.class)
//                .expectHandlers(0, BufferedReader.class);
//        validator.check(listener);
//    }

    public static class NClasses {
        final Class<?>[] messageTypes;

        public NClasses(Class<?> nClass) {
            this.messageTypes = new Class<?>[] {nClass};
        }

        public NClasses(Class<?>... messageTypes) {
            this.messageTypes = messageTypes;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result + Arrays.hashCode(this.messageTypes);
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            NClasses other = (NClasses) obj;
            if (!Arrays.equals(this.messageTypes, other.messageTypes)) {
                return false;
            }
            return true;
        }
    }

    private class ListenerValidator {
        private Map<NClasses, Integer> handlers = new HashMap<NClasses, Integer>();

        public ListenerValidator expectHandlers(Integer count, Class<?> requiredMessageType) {
            NClasses nClasses = new NClasses(requiredMessageType);

            this.handlers.put(nClasses, count);
            return this;
        }

        public ListenerValidator expectHandlers(Integer count, Class<?>... messageTypes) {
            NClasses nClasses = new NClasses(messageTypes);

            this.handlers.put(nClasses, count);
            return this;
        }

        public void check(MessageListener listener){
            for (Map.Entry<NClasses, Integer> expectedHandler: this.handlers.entrySet()) {
                NClasses key = expectedHandler.getKey();
                List<MessageHandler> handlers2 = getHandlers(listener, key.messageTypes);

                if (expectedHandler.getValue() > 0){
                    assertTrue(!handlers2.isEmpty());
                }
                else{
                    assertFalse(!handlers2.isEmpty());
                }
                assertEquals(expectedHandler.getValue(), handlers2.size());
            }
        }

        // for testing
        public List<MessageHandler> getHandlers(MessageListener listener, Class<?>... messageTypes) {
            List<MessageHandler> matching = new LinkedList<MessageHandler>();
            for (MessageHandler handler : listener.getHandlers()) {
                if (handler.handlesMessage(messageTypes)) {
                    matching.add(handler);
                }
            }
            return matching;
        }
    }


    // a simple event listener
    @SuppressWarnings("unused")
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




    @Test
    public void testMultipleSignatureListenerWithoutInheritance() {
        MessageListener listener = this.reader.getMessageListener(MultiMessageListener1.class);
        ListenerValidator validator = new ListenerValidator()
                .expectHandlers(7, String.class)
                .expectHandlers(9, String.class, String.class)
                .expectHandlers(9, String.class, String.class, String.class)
                .expectHandlers(3, String.class, String[].class)
                .expectHandlers(1, String.class, String[].class, String[].class)
                .expectHandlers(6, String[].class)
                .expectHandlers(3, String[].class, String[].class)
                .expectHandlers(2, Object.class)
                .expectHandlers(2, String.class, Object.class)
                .expectHandlers(2, String.class, Object[].class)
                ;
        validator.check(listener);
    }

    @SuppressWarnings("unused")
    public class MultiMessageListener1 {

        @Handler public void handleString1(String s) {}
        @Handler public void handleString2(String s, String s1) {}
        @Handler public void handleString3(String s, String s1, String s2) {}

        @Handler public void handleStringN(String... s1) {}
        @Handler public void handleStringArray(String[] s1) {}

        @Handler public void handleStringN(Object... s1) {}
        @Handler public void handleStringArray(Object[] s1) {}

        @Handler public void handleString1plusN(String s, String... s1) {}
        @Handler public void handleString1plusN(String s, Object... s1) {}

        @Handler public void handleString2plusN(String s, String s1, String... s2) {}
        @Handler public void handleString2plusN(String s, Object s1, String... s2) {}

        @Handler public void handleStringXplus1(String[] s, String s1) {}

        @Handler public void handleStringXplusN(String[] s, String... s1) {}
        @Handler public void handleStringXplusN(String[] s, Object... s1) {}

        @Handler public void handleStringXplus1plusN(String[] s, String s1, String... s2) {}
        @Handler public void handleStringXplus1plusN(String[] s, String s1, Object... o) {}

        @Handler public void handleStringXplus1plusN(String[] s, Object o, Object... o1) {}

    }
}
