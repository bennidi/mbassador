package net.engio.mbassy.listeners;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.messages.ICountable;

/**
 *
 * @author bennidi
 *         Date: 5/24/13
 */
public class ICountableListener {

    private static abstract class BaseListener {

        @Handler
        public void handle(ICountable message){
            message.handled(this.getClass());
        }

    }

    public static class DefaultListener extends BaseListener {

        @Override
        public void handle(ICountable message){
            super.handle(message);
        }
    }

    public static class NoSubtypesListener extends BaseListener {

        @Override
        @Handler(rejectSubtypes = true)
        public void handle(ICountable message){
            super.handle(message);
        }
    }


    public static class DisabledListener extends BaseListener {

        @Override
        @Handler(enabled = false)
        public void handle(ICountable message){
            super.handle(message);
        }

    }


}
