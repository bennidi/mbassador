package net.engio.mbassy.dispatch.el;

import java.beans.FeatureDescriptor;
import java.util.Iterator;

import javax.el.ELContext;
import javax.el.ELResolver;

/*****************************************************************************
 * A resolver that will resolve the "msg" variable to the event object that 
 * is posted.
 ****************************************************************************/

public class RootResolver extends ELResolver {

	private static final String ROOT_VAR_NAME = "msg";
	public Object rootObject; 
	
	/*************************************************************************
	 * @param rootObject
	 ************************************************************************/
	
	public void setRoot(Object rootObject) {
		this.rootObject = rootObject;
	}
	
	/*************************************************************************
	 * getValue
	 * @see javax.el.ELResolver#getValue(javax.el.ELContext, java.lang.Object, java.lang.Object)
	 ************************************************************************/
	@Override
	public Object getValue(ELContext context, Object base, Object property) {
		if (context == null) {
			throw new NullPointerException();
		}
		if (base == null && ROOT_VAR_NAME.equals(property)) {
			context.setPropertyResolved(true);
			return this.rootObject;
		}
		return null;
	}

	
	/*************************************************************************
	 * getCommonPropertyType
	 * @see javax.el.ELResolver#getCommonPropertyType(javax.el.ELContext, java.lang.Object)
	 ************************************************************************/
	@Override
	public Class<?> getCommonPropertyType(ELContext context, Object base) {
		return String.class;
	}

	/*************************************************************************
	 * getFeatureDescriptors
	 * @see javax.el.ELResolver#getFeatureDescriptors(javax.el.ELContext, java.lang.Object)
	 ************************************************************************/
	@Override
	public Iterator<FeatureDescriptor> getFeatureDescriptors(ELContext context, Object base) {
		return null;
	}

	/*************************************************************************
	 * getType
	 * @see javax.el.ELResolver#getType(javax.el.ELContext, java.lang.Object, java.lang.Object)
	 ************************************************************************/
	@Override
	public Class<?> getType(ELContext context, Object base, Object property) {
		return null;
	}

	/*************************************************************************
	 * isReadOnly
	 * @see javax.el.ELResolver#isReadOnly(javax.el.ELContext, java.lang.Object, java.lang.Object)
	 ************************************************************************/
	@Override
	public boolean isReadOnly(ELContext context, Object base, Object property) {
		return true;
	}

	/*************************************************************************
	 * setValue
	 * @see javax.el.ELResolver#setValue(javax.el.ELContext, java.lang.Object, java.lang.Object, java.lang.Object)
	 ************************************************************************/
	@Override
	public void setValue(ELContext context, Object base, Object property, Object value) {
		// Do nothing
	}

}
