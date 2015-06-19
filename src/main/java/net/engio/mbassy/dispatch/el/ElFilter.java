package net.engio.mbassy.dispatch.el;

import net.engio.mbassy.bus.error.PublicationError;
import net.engio.mbassy.listener.IMessageFilter;
import net.engio.mbassy.listener.MessageHandler;
import net.engio.mbassy.subscription.SubscriptionContext;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

/**
 * A filter that will use a expression from the handler annotation and
 * parse it as EL.
 * <p/>
 * Accepts a message if the associated EL expression evaluates to <code>true</code>
 */
public class ElFilter implements IMessageFilter {

    // thread-safe initialization of EL factory singleton
    public static final class ExpressionFactoryHolder {

        // if runtime exception is thrown, this will
        public static final ExpressionFactory ELFactory = getELFactory();

        /**
         * **********************************************************************
         * Get an implementation of the ExpressionFactory. This uses the
         * Java service lookup mechanism to find a proper implementation.
         * If none is available we do not support EL filters.
         * **********************************************************************
         */
        private static final ExpressionFactory getELFactory() {
            try {
                return ExpressionFactory.newInstance();
            } catch (RuntimeException e) {
                return null;
            }
        }
    }

    public static final boolean isELAvailable() {
        return ExpressionFactoryHolder.ELFactory != null;
    }

    public static final ExpressionFactory ELFactory() {
        return ExpressionFactoryHolder.ELFactory;
    }


    @Override
    public boolean accepts(Object message, final SubscriptionContext context) {
        final MessageHandler metadata = context.getHandler();
        String expression = metadata.getCondition();
        StandardELResolutionContext resolutionContext = new StandardELResolutionContext(message);
        return evalExpression(expression, resolutionContext, context, message);
    }

    private boolean evalExpression(final String expression,
                                   final StandardELResolutionContext resolutionContext,
                                   final SubscriptionContext context,
                                   final Object message) {
        ValueExpression ve = ELFactory().createValueExpression(resolutionContext, expression, Boolean.class);
        try {
            return (Boolean)ve.getValue(resolutionContext);
        } catch (Throwable exception) {
            PublicationError publicationError = new PublicationError(exception, "Error while evaluating EL expression on message", context)
                    .setPublishedMessage(message);
            context.handleError(publicationError);
            return false;
        }
    }

}
