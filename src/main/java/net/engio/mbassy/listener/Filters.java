package net.engio.mbassy.listener;

/**
 * Some sample filters that are not particularly useful in production environment
 * but illustrate how filters are meant to be used.
 *
 * @author bennidi
 *         Date: 12/12/12
 */
public class Filters {

    public static final class AllowAll implements IMessageFilter {

        @Override
        public boolean accepts(Object event, MessageHandler metadata) {
            return true;
        }
    }

    public static final class RejectAll implements IMessageFilter {

        @Override
        public boolean accepts(Object event, MessageHandler metadata) {
            return false;
        }
    }


    public static final class RejectSubtypes implements IMessageFilter {

        @Override
        public boolean accepts(Object event, MessageHandler metadata) {
            for (Class handledMessage : metadata.getHandledMessages()) {
                if (handledMessage.equals(event.getClass())) {
                    return true;
                }
            }
            return false;
        }
    }
}
