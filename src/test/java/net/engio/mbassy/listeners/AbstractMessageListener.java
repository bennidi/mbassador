package net.engio.mbassy.listeners;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Invoke;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;
import net.engio.mbassy.messages.AbstractMessage;

/**
 *
 * @author bennidi
 *         Date: 5/24/13
 */
@Listener(references = References.Weak)
public class AbstractMessageListener {

    private static abstract class BaseListener {

        @Handler
        public void handle(AbstractMessage message){
            message.handled(this.getClass());
        }

    }

    public static class DefaultListener extends BaseListener {

        public void handle(AbstractMessage message){
            super.handle(message);
        }
    }

    public static class NoSubtypesListener extends BaseListener {

        @Handler(rejectSubtypes = true, priority = 4)
        public void handle(AbstractMessage message){
            super.handle(message);
        }
    }


    public static class AsyncListener extends BaseListener {

        @Handler(delivery = Invoke.Asynchronously, priority = Integer.MAX_VALUE)
        public void handle(AbstractMessage message){
            super.handle(message);
        }

    }

    public static class DisabledListener extends BaseListener {

        @Handler(enabled = false)
        public void handle(AbstractMessage message){
            super.handle(message);
        }

    }


}
