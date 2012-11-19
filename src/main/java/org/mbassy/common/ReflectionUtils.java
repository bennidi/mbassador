package org.mbassy.common;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * User: benni
 * Date: 2/16/12
 * Time: 12:14 PM
 */
public class ReflectionUtils {

	public static List<Method> getMethods(IPredicate<Method> condition, Class<?> target) {
		List<Method> methods = new LinkedList<Method>();
		try {
			for (Method method : target.getDeclaredMethods()) {
				if (condition.apply(method)) {
					methods.add(method);
				}
			}
		} catch (Exception e) {
			//nop
		}
		if (!target.equals(Object.class)) {
			methods.addAll(getMethods(condition, target.getSuperclass()));
		}
		return methods;
	}

	public static List<Field> getFields(IPredicate<Field> condition, Class<?> target) {
		List<Field> methods = new LinkedList<Field>();
		try {
			for (Field method : target.getDeclaredFields()) {
				if (condition.apply(method)) {
					methods.add(method);
				}
			}
		} catch (Exception e) {
			//nop
		}
		if (!target.equals(Object.class)) {
			methods.addAll(getFields(condition, target.getSuperclass()));
		}
		return methods;
	}

	public static Object callMethod(Object o, final String methodName, Object... args) {

		if(o == null || methodName == null) {
			return null;
		}

		Object res = null;
		try {
			Method m = o.getClass().getMethod(methodName);
			res = m.invoke(o, args);
		} catch (Exception e) {
			//logger.warn("Not possible to get value", e);
		}
		return res;
	}
}
