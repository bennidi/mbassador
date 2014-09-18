package net.engio.mbassy.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author bennidi
 *         Date: 2/16/12
 *         Time: 12:14 PM
 */
public class ReflectionUtils
{

	public static List<Method> getMethods( IPredicate<Method> condition, Class<?> target ) {
		List<Method> methods = new LinkedList<Method>();
		try {
			for ( Method method : target.getDeclaredMethods() ) {
				if ( condition.apply( method ) ) {
					methods.add( method );
				}
			}
		}
		catch ( Exception e ) {
			//nop
		}
		if ( !target.equals( Object.class ) ) {
			methods.addAll( getMethods( condition, target.getSuperclass() ) );
		}
		return methods;
	}

	/**
	 * Traverses the class hierarchy upwards, starting at the given subclass, looking
	 * for an override of the given methods -> finds the bottom most override of the given
	 * method if any exists
	 *
	 * @param overridingMethod
	 * @param subclass
	 * @return
	 */
	public static Method getOverridingMethod( final Method overridingMethod, final Class subclass ) {
		Class current = subclass;
		while ( !current.equals( overridingMethod.getDeclaringClass() ) ) {
			try {
				return current.getDeclaredMethod( overridingMethod.getName(), overridingMethod.getParameterTypes() );
			}
			catch ( NoSuchMethodException e ) {
				current = current.getSuperclass();
			}
		}
		return null;
	}

	public static Set<Class> getSuperclasses( Class from ) {
		Set<Class> superclasses = new HashSet<Class>();
		collectInterfaces( from, superclasses );
		while ( !from.equals( Object.class ) && !from.isInterface() ) {
			superclasses.add( from.getSuperclass() );
			from = from.getSuperclass();
			collectInterfaces( from, superclasses );
		}
		return superclasses;
	}

	public static void collectInterfaces( Class from, Set<Class> accumulator ) {
		for ( Class intface : from.getInterfaces() ) {
			accumulator.add( intface );
			collectInterfaces( intface, accumulator );
		}
	}

	public static boolean containsOverridingMethod( final List<Method> allMethods, final Method methodToCheck ) {
		for ( Method method : allMethods ) {
			if ( isOverriddenBy( methodToCheck, method ) ) {
				return true;
			}
		}
		return false;
	}

	public static <A extends Annotation> A getAnnotation( Method method, Class<A> annotationType ) {
		return getAnnotation( (AnnotatedElement) method, annotationType );
	}

	public static <A extends Annotation> A getAnnotation( Class from, Class<A> annotationType ) {
		return getAnnotation( (AnnotatedElement) from, annotationType );
	}

	/**
	 * Searches for an Annotation of the given type on the class.  Supports meta annotations.
	 *
	 * @param from AnnotatedElement (class, method...)
	 * @param annotationType Annotation class to look for.
	 * @param <A> Annotation class
	 * @return Annotation instance or null
	 */
	public static <A extends Annotation> A getAnnotation( AnnotatedElement from, Class<A> annotationType ) {
		A ann = from.getAnnotation( annotationType );
		if ( ann == null ) {
			for ( Annotation metaAnn : from.getAnnotations() ) {
				ann = metaAnn.annotationType().getAnnotation( annotationType );
				if ( ann != null ) {
					break;
				}
			}
		}
		return ann;
	}

	private static boolean isOverriddenBy( Method superclassMethod, Method subclassMethod ) {
		// if the declaring classes are the same or the subclass method is not defined in the subclass
		// hierarchy of the given superclass method or the method names are not the same then
		// subclassMethod does not override superclassMethod
		if ( superclassMethod.getDeclaringClass().equals(
				subclassMethod.getDeclaringClass() ) || !superclassMethod.getDeclaringClass().isAssignableFrom(
				subclassMethod.getDeclaringClass() ) || !superclassMethod.getName().equals(
				subclassMethod.getName() ) ) {
			return false;
		}

		Class[] superClassMethodParameters = superclassMethod.getParameterTypes();
		Class[] subClassMethodParameters = subclassMethod.getParameterTypes();
		// method must specify the same number of parameters
		//the parameters must occur in the exact same order
		for ( int i = 0; i < subClassMethodParameters.length; i++ ) {
			if ( !superClassMethodParameters[i].equals( subClassMethodParameters[i] ) ) {
				return false;
			}
		}
		return true;
	}

    public static <T> T getField(String filterRef, Class<T> intendedSubclass) {
        int lastDot = filterRef.lastIndexOf('.');
        if (lastDot == -1) throw new IllegalArgumentException("Field reference should be composed of <classname>.<fieldname>");
        String className = filterRef.substring(0, lastDot);
        String fieldName = filterRef.substring(lastDot+1);
        try {
            Class<?> clazz = Class.forName(className);
            return intendedSubclass.cast(clazz.getField(fieldName).get(null));
        } catch (ClassNotFoundException | IllegalArgumentException | IllegalAccessException | NoSuchFieldException | SecurityException e) {
            throw new IllegalArgumentException("Unable to access field", e);
        }
    }

}
