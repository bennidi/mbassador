package net.engio.mbassy.listeners;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Invoke;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;
import net.engio.mbassy.messages.MessageTypes;

/**
 *
 * @author bennidi
 *         Date: 5/24/13
 */
public class MessagesTypeListener {

    @Listener(references = References.Weak)
    private static abstract class BaseListener {

        @Handler
        public void handle(MessageTypes message){
            message.handled(this.getClass());
        }

    }

    public static class DefaultListener extends BaseListener {

        public void handle(MessageTypes message){
            super.handle(message);
        }
    }

    public static class NoSubtypesListener extends BaseListener {

        @Handler(rejectSubtypes = true)
        public void handle(MessageTypes message){
            super.handle(message);
        }
    }


    public static class AsyncListener extends BaseListener {

        @Handler(delivery = Invoke.ASYNCHRONOUSLY)
        public void handle(MessageTypes message){
            super.handle(message);
        }

    }

    public static class DisabledListener extends BaseListener {

        @Handler(enabled = false)
        public void handle(MessageTypes message){
            super.handle(message);
        }

    }


}
