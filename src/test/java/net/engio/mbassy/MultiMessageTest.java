/*
 * Copyright 2015 dorkbox, llc
 */
package net.engio.mbassy;

import java.util.concurrent.atomic.AtomicInteger;

import net.engio.mbassy.annotations.Handler;
import net.engio.mbassy.common.MessageBusTest;

import org.junit.Test;

/**
 * @author dorkbox, llc
 *         Date: 2/2/15
 */
public class MultiMessageTest extends MessageBusTest {

    private static AtomicInteger count = new AtomicInteger(0);

    @Test
    public void testMultiMessageSending(){
        IMessageBus bus = new MBassador();

        Listener listener = new Listener();
        bus.subscribe(listener);

        bus.publish("s");
        bus.publish("s", "s");
        bus.publish("s", "s", "s");
        bus.publish("s", "s", "s", "s");
        bus.publish(1, 2, "s");
        bus.publish(1, 2, 3, 4, 5, 6);

        assertEquals(count.get(), 5);
    }

    public static class Listener {
        @Handler
        @SuppressWarnings("unused")
        public void handleSync(String o1) {
            count.getAndIncrement();
        }

        @Handler
        @SuppressWarnings("unused")
        public void handleSync(String o1, String o2) {
            count.getAndIncrement();
        }

        @Handler
        @SuppressWarnings("unused")
        public void handleSync(String o1, String o2, String o3) {
            count.getAndIncrement();
        }

        @Handler
        @SuppressWarnings("unused")
        public void handleSync(Integer o1, Integer o2, String o3) {
            count.getAndIncrement();
        }

        @Handler(vararg = true)
        @SuppressWarnings("unused")
        public void handleSync(String... o) {
            count.getAndIncrement();
        }

        @Handler
        @SuppressWarnings("unused")
        public void handleSync(Integer... o) {
            count.getAndIncrement();
        }
    }
}
