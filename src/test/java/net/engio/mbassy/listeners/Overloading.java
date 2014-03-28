package net.engio.mbassy.listeners;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;
import net.engio.mbassy.messages.AbstractMessage;

/**
 * Some handlers and message types to test correct functioning of overloaded
 * message handlers
 *
 */
public class Overloading {

    public static class TestMessageA extends AbstractMessage {}

    public static class TestMessageB extends AbstractMessage {}

    public static class ListenerSub extends ListenerBase {

        @Handler
        public void handleEvent(TestMessageB event) {
            event.handled(this.getClass());
        }

    }

    @Listener(references = References.Strong)
    public static class ListenerBase {


        /**
         * (!) If this method is removed, NO event handler will be called.
         */
        @Handler
        public void handleEventWithNonOverloadedMethodName(TestMessageA event) {
            event.handled(this.getClass());
        }

        @Handler
        public void handleEvent(TestMessageA event) {
            event.handled(this.getClass());
        }

    }

}
