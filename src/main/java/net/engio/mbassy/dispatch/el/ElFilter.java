package net.engio.mbassy.dispatch.el;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;

import net.engio.mbassy.listener.IMessageFilter;
import net.engio.mbassy.listener.MessageHandler;

/*****************************************************************************
 * A filter that will use a expression from the handler annotation and 
 * parse it as EL.
 ****************************************************************************/

public class ElFilter implements IMessageFilter {

	private static ElFilter instance;
	
	static {
		try {
			instance = new ElFilter();
		} catch (Exception e) {
			// Most likely the javax.el package is not available.
			instance = null;
		}
	}
	
	private ExpressionFactory elFactory;
	
	/*************************************************************************
	 * Constructor
	 ************************************************************************/
	
	private ElFilter() {
		super();
		initELFactory();
	}
	
	/*************************************************************************
	 * Get an implementation of the ExpressionFactory. This uses the 
	 * Java service lookup mechanism to find a proper implementation.
	 * If none if available we do not support EL filters.
	 ************************************************************************/

	private void initELFactory() {
		try {
			this.elFactory = ExpressionFactory.newInstance();
		} catch (RuntimeException e) {
			// No EL implementation on the class path.
			elFactory = null;
		}
	}
	
	/*************************************************************************
	 * accepts
	 * @see net.engio.mbassy.listener.IMessageFilter#accepts(java.lang.Object, net.engio.mbassy.listener.MessageHandler)
	 ************************************************************************/
	@Override
	public boolean accepts(Object message, MessageHandler metadata) {
		String expression = metadata.getCondition();
		if (expression == null || expression.trim().length() == 0) {
			return true;
		}
		if (elFactory == null) {
			// TODO should we test this some where earlier? Perhaps in MessageHandler.validate()  ?
			throw new IllegalStateException("A handler uses an EL filter but no EL implementation is available.");
		}
		
		expression = cleanupExpression(expression);
		
		EventContext context = new EventContext();
		context.bindToEvent(message);
		
		return evalExpression(expression, context);
	}

	/*************************************************************************
	 * @param expression
	 * @param context
	 * @return
	 ************************************************************************/
	
	private boolean evalExpression(String expression, EventContext context) {
		ValueExpression ve = elFactory.createValueExpression(context, expression, Boolean.class);
		Object result = ve.getValue(context);
		if (!(result instanceof Boolean)) {
			throw new IllegalStateException("A handler uses an EL filter but the output is not \"true\" or \"false\".");
		}
		return (Boolean)result;
	}

	/*************************************************************************
	 * Make it a valid expression because the parser expects it like this.
	 * @param expression
	 * @return
	 ************************************************************************/
	
	private String cleanupExpression(String expression) {
		 
		if (!expression.trim().startsWith("${") && !expression.trim().startsWith("#{")) {
			expression = "${"+expression+"}";
		}
		return expression;
	}

	/*************************************************************************
	 * @return the one and only
	 ************************************************************************/
	
	public static synchronized ElFilter getInstance() {
		return instance;
	}

}
