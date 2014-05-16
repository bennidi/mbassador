package net.engio.mbassy.dispatch.el;

import java.lang.reflect.Method;

import javax.el.BeanELResolver;
import javax.el.CompositeELResolver;
import javax.el.ELContext;
import javax.el.ELResolver;
import javax.el.FunctionMapper;
import javax.el.ValueExpression;
import javax.el.VariableMapper;

/*****************************************************************************
 * An EL context that knows how to resolve everything from a 
 * given message but event.
 ****************************************************************************/

public class EventContext extends ELContext {

	private final CompositeELResolver resolver;
	private final FunctionMapper functionMapper;
	private final VariableMapper variableMapper;
	private RootResolver rootResolver;

	/*************************************************************************
	 * Constructor
	 * 
	 * @param me
	 ************************************************************************/

	public EventContext() {
		super();
		this.functionMapper = new NoopFunctionMapper();
		this.variableMapper = new NoopMapperImpl();
		
		this.resolver = new CompositeELResolver();
		this.rootResolver = new RootResolver();
		this.resolver.add(rootResolver);
		this.resolver.add(new BeanELResolver(true));
	}
	
	 /*************************************************************************
	 * Binds an event object with the EL expression. This will allow access
	 * to all properties of a given event.
	 * @param event to bind.
	 ************************************************************************/
	
	public void bindToEvent(Object event) {
		 this.rootResolver.setRoot(event);
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

	/*****************************************************************************
	 *  Dummy implementation.
	 ****************************************************************************/
	
	private class NoopMapperImpl extends VariableMapper {
		public ValueExpression resolveVariable(String s) {
			return null;
		}

		public ValueExpression setVariable(String s,
				ValueExpression valueExpression) {
			return null;
		}
	}

	/*****************************************************************************
	 *  Dummy implementation.
	 ****************************************************************************/
	
	private class NoopFunctionMapper extends FunctionMapper {
		public Method resolveFunction(String s, String s1) {
			return null;
		}
	}

}
