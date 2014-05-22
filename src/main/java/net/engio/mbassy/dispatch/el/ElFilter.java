package net.engio.mbassy.dispatch.el;

import net.engio.mbassy.listener.IMessageFilter;
import net.engio.mbassy.listener.MessageHandler;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

/*****************************************************************************
 * A filter that will use a expression from the handler annotation and 
 * parse it as EL.
 ****************************************************************************/

public class ElFilter implements IMessageFilter {

    // thread-safe initialization of EL factory singleton
    public static final class ExpressionFactoryHolder{

        // if runtime exception is thrown, this will
        public static final ExpressionFactory ELFactory = getELFactory();

        /*************************************************************************
         * Get an implementation of the ExpressionFactory. This uses the
         * Java service lookup mechanism to find a proper implementation.
         * If none if available we do not support EL filters.
         ************************************************************************/
        private static final ExpressionFactory getELFactory(){
            try {
                return ExpressionFactory.newInstance();
            } catch (RuntimeException e) {
                return null;
            }
        }
    }

    public static final boolean isELAvailable(){
        return ExpressionFactoryHolder.ELFactory != null;
    }

    public static final ExpressionFactory ELFactory(){
        return ExpressionFactoryHolder.ELFactory;
    }

    /**
     * Accepts a message if the associated EL expression of the message handler resolves to 'true'
     *
     * @param message the message to be handled by the handler
     * @param  metadata the metadata object which describes the message handler
     * @return
     */
	@Override
	public boolean accepts(Object message, MessageHandler metadata) {
		String expression = metadata.getCondition();
		StandardELResolutionContext context = new StandardELResolutionContext(message);
		return evalExpression(expression, context);
	}

	private boolean evalExpression(String expression, StandardELResolutionContext context) {
		ValueExpression ve = ELFactory().createValueExpression(context, expression, Boolean.class);
		try{
            Object result = ve.getValue(context);
            return (Boolean)result;
             }
        catch(Throwable exception){
            // TODO: BusRuntime should be available in this filter to propagate resolution errors
            // -> this is generally a good feature for filters
            return false;
            //throw new IllegalStateException("A handler uses an EL filter but the output is not \"true\" or \"false\".");
        }
	}

}
