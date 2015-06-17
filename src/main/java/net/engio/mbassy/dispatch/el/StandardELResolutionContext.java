package net.engio.mbassy.dispatch.el;

import javax.el.*;
import java.lang.reflect.Method;

/**
 *  This ELContext implementation provides support for standard BeanEL resolution in conditional message handlers.
 *  The message parameter of the message handlers is bound to 'msg' such that it can be referenced int the EL expressions.
 *
 *  <pre>
 *  <code>
 *  {@literal @}Handler(condition = "msg.type == 'onClick'")
 *  public void handle(ButtonEvent event)
 *  </code>
 *  </pre>
 */
public class StandardELResolutionContext extends ELContext {

	private final ELResolver resolver;
	private final FunctionMapper functionMapper;
	private final VariableMapper variableMapper;
    private final Object message;


	public StandardELResolutionContext(Object message) {
		super();
        this.message = message;
		this.functionMapper = new NoopFunctionMapper();
		this.variableMapper = new MsgMapper();
        // Composite resolver not necessary as the only resolution type currently supported is standard BeanEL
		//this.resolver = new CompositeELResolver();
        this.resolver = new BeanELResolver(true);
	}
	


	/*************************************************************************
	 * The resolver for the event object.
	 * @see javax.el.ELContext#getELResolver()
	 ************************************************************************/
	@Override
	public ELResolver getELResolver() {
		return this.resolver;
	}

	/*************************************************************************
	 * @see javax.el.ELContext#getFunctionMapper()
	 ************************************************************************/
	@Override
	public FunctionMapper getFunctionMapper() {
		return this.functionMapper;
	}

	/*************************************************************************
	 * @see javax.el.ELContext#getVariableMapper()
	 ************************************************************************/
	@Override
	public VariableMapper getVariableMapper() {
		return this.variableMapper;
	}

    /**
     * This mapper resolves the variable identifies "msg" to the message
     * object of the current handler invocation
     */
	private class MsgMapper extends VariableMapper {
        private static final String msg = "msg";
        // reuse the same expression as it always resolves to the same object
        private final ValueExpression msgExpression = ElFilter.ELFactory().createValueExpression(message, message.getClass());

		public ValueExpression resolveVariable(final String s) {
            // resolve 'msg' to the message object of the handler invocation
            return !s.equals(msg) ? null : msgExpression;
		}

		public ValueExpression setVariable(String s,
				ValueExpression valueExpression) {
            // not necessary - the mapper resolves only "msg" and nothing else
			return null;
		}
	}

    /**
     * This function mapper does nothing, i.e. custom EL functions are not
     * supported by default. It may be supported in the future to pass in
     * custom function mappers at bus instanciation time.
     */
	private class NoopFunctionMapper extends FunctionMapper {
		public Method resolveFunction(String s, String s1) {
			return null;
		}
	}

}
