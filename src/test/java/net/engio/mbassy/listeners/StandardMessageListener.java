package net.engio.mbassy.listeners;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Invoke;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;
import net.engio.mbassy.messages.StandardMessage;

/**
 *
 * @author bennidi
 *         Date: 5/24/13
 */
public class StandardMessageListener {

    @Listener(references = References.Weak)
    private static abstract class BaseListener {

        @Handler(priority = 3)
        public void handle(StandardMessage message){
            message.handled(this.getClass());
        }

    }

    public static class DefaultListener extends BaseListener {

        public void handle(StandardMessage message){
            super.handle(message);
        }
    }

    public static class NoSubtypesListener extends BaseListener {

        @Handler(rejectSubtypes = true, priority = 4)
        public void handle(StandardMessage message){
            super.handle(message);
        }
    }


    public static class AsyncListener extends BaseListener {

        @Handler(delivery = Invoke.Asynchronously, priority = -10)
        public void handle(StandardMessage message){
            super.handle(message);
        }

    }

    public static class DisabledListener extends BaseListener {

        @Handler(enabled = false)
        public void handle(StandardMessage message){
            super.handle(message);
        }

    }


}
