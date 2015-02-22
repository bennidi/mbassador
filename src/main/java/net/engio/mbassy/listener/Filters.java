package net.engio.mbassy.listener;

import net.engio.mbassy.subscription.SubscriptionContext;

/**
 * A set of standard filters for common use cases.
 *
 * @author bennidi
 *         Date: 12/12/12
 */
public class Filters {



    /**
     * This filter will only accept messages of the exact same type
     * as specified for the handler. Subclasses (this includes interface implementations)
     * will be rejected.
     *
     * NOTE: The same functionality (with better performance) is achieved using {@code rejectSubtypes = true}
     * in the @Handler annotation
     */
    public static final class RejectSubtypes implements IMessageFilter {

        @Override
        public boolean accepts(final Object event, final SubscriptionContext context) {
            final MessageHandler metadata = context.getHandler();
            for (Class handledMessage : metadata.getHandledMessages()) {
                if (handledMessage.equals(event.getClass())) {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     * This filter will only accept messages that are real subtypes
     * of the specified message types handled by the message handler.
     * Example: If the handler handles Object.class the filter accepts
     * all objects except any direct instance of Object.class {@code new Object()}
     */
    public static final class SubtypesOnly implements IMessageFilter{

        @Override
        public boolean accepts(final Object message, final SubscriptionContext context) {
            final MessageHandler metadata = context.getHandler();
            for(Class acceptedClasses : metadata.getHandledMessages()){
                if(acceptedClasses.isAssignableFrom(message.getClass())
                        && ! acceptedClasses.equals(message.getClass()))
                    return true;
            }
            return false;
        }
    }

}
