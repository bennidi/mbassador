package net.engio.mbassy.listeners;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.messages.MessageTypes;

/**
 *
 * @author bennidi
 *         Date: 5/24/13
 */
public class MessagesListener {

    private static abstract class BaseListener {

        @Handler
        public void handle(MessageTypes message){
            message.handled(this.getClass());
        }

    }

    public static class DefaultListener extends BaseListener {

        @Override
        public void handle(MessageTypes message){
            super.handle(message);
        }
    }

    public static class NoSubtypesListener extends BaseListener {

        @Override
        @Handler(rejectSubtypes = true)
        public void handle(MessageTypes message){
            super.handle(message);
        }
    }


    public static class DisabledListener extends BaseListener {

        @Override
        @Handler(enabled = false)
        public void handle(MessageTypes message){
            super.handle(message);
        }

    }


}
