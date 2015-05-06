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

    public static StrongConcurrentSet<Method> getMethods( IPredicate<Method> condition, Class<?> target ) {
        StrongConcurrentSet<Method> strongConcurrentSet = new StrongConcurrentSet<Method>();
        getMethods(condition, target, strongConcurrentSet);
        return strongConcurrentSet;
    }

    private static void getMethods( IPredicate<Method> condition, Class<?> target, StrongConcurrentSet<Method> methods ) {
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
            getMethods( condition, target.getSuperclass(), methods );
            methods.addAll( methods );
        }
    }

    /**
    * Traverses the class hierarchy upwards, starting at the given subclass, looking
    * for an override of the given methods -> finds the bottom most override of the given
    * method if any exists
    *
    * @param overridingMethod
    * @param subclass
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

    /**
     * Collect all directly and indirectly related super types (classes and interfaces) of
     * a given class.
     *
     * @param from The root class to start with
     * @return A set of classes, each representing a super type of the root class
     */
    public static Set<Class> getSuperTypes(Class from) {
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

    public static boolean containsOverridingMethod( final StrongConcurrentSet<Method> allMethods, final Method methodToCheck ) {
        ISetEntry<Method> current = allMethods.head;
        Method method;
        while (current != null) {
            method = current.getValue();
            current = current.next();

            if ( isOverriddenBy( methodToCheck, method ) ) {
                return true;
            }
        }
        return false;
    }



    /**
    * Searches for an Annotation of the given type on the class.  Supports meta annotations.
    *
    * @param from AnnotatedElement (class, method...)
    * @param annotationType Annotation class to look for.
    * @param <A> Class of annotation type
    * @return Annotation instance or null
    */
    private static <A extends Annotation> A getAnnotation( AnnotatedElement from, Class<A> annotationType, Set<AnnotatedElement> visited) {
        if( visited.contains(from) ) return null;
        visited.add(from);
        A ann = from.getAnnotation( annotationType );
        if( ann != null) return ann;
        for ( Annotation metaAnn : from.getAnnotations() ) {
            ann = getAnnotation(metaAnn.annotationType(), annotationType, visited);
            if ( ann != null ) {
                return ann;
            }
        }
        return null;
    }

    public static <A extends Annotation> A getAnnotation( AnnotatedElement from, Class<A> annotationType){
       return getAnnotation(from, annotationType, new HashSet<AnnotatedElement>());
    }

    private static boolean isOverriddenBy( Method superclassMethod, Method subclassMethod ) {
        // if the declaring classes are the same or the subclass method is not defined in the subclass
        // hierarchy of the given superclass method or the method names are not the same then
        // subclassMethod does not override superclassMethod
        if ( superclassMethod.getDeclaringClass().equals(subclassMethod.getDeclaringClass() )
                || !superclassMethod.getDeclaringClass().isAssignableFrom( subclassMethod.getDeclaringClass() )
                || !superclassMethod.getName().equals(subclassMethod.getName())) {
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

}
