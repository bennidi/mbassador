package net.engio.mbassy.listeners;

import net.engio.mbassy.annotations.Handler;
import net.engio.mbassy.messages.IMultipartMessage;

/**
 *
 * @author bennidi
 *         Date: 5/24/13
 */
public class IMultipartMessageListener {

    private static abstract class BaseListener {

        @Handler
        public void handle(IMultipartMessage message){
            message.handled(this.getClass());
        }

    }

    public static class DefaultListener extends BaseListener {

        @Override
        public void handle(IMultipartMessage message){
            super.handle(message);
        }
    }

    public static class NoSubtypesListener extends BaseListener {

        @Override
        @Handler(rejectSubtypes = true)
        public void handle(IMultipartMessage message){
            super.handle(message);
        }
    }


    public static class DisabledListener extends BaseListener {

        @Override
        @Handler(enabled = false)
        public void handle(IMultipartMessage message){
            super.handle(message);
        }

    }


}
