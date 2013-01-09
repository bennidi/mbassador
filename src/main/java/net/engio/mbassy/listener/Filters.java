package net.engio.mbassy.listener;

/**
 * Todo: Add javadoc
 *
 * @author bennidi
 *         Date: 12/12/12
 */
public class Filters {

    public static final class AllowAll implements IMessageFilter {

        @Override
        public boolean accepts(Object event, MessageHandlerMetadata metadata) {
            return true;
        }
    }

    public static final class RejectAll implements IMessageFilter {

        @Override
        public boolean accepts(Object event, MessageHandlerMetadata metadata) {
            return false;
        }
    }


    public static final class RejectSubtypes implements IMessageFilter {

        @Override
        public boolean accepts(Object event, MessageHandlerMetadata metadata) {
            for(Class handledMessage : metadata.getHandledMessages()){
                if(handledMessage.equals(event.getClass()))return true;
            }
            return false;
        }
    }
}
