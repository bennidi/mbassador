package net.engio.mbassy.common;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author bennidi
 *         Date: 2/16/12
 *         Time: 12:14 PM
 */
public class ReflectionUtils
{
    public static Method[] getMethods(IPredicate<Method> condition, Class<?> target) {
        ArrayList<Method> methods = new ArrayList<Method>();

        getMethods(condition, target, methods);

        final Method[] array = new Method[methods.size()];
        methods.toArray(array);
        return array;
    }

    public static void getMethods(IPredicate<Method> condition, Class<?> target, ArrayList<Method> methods) {
        try {
            for (Method method : target.getDeclaredMethods()) {
                if (condition.apply(method)) {
                    methods.add(method);
                }
            }

            for (Class superType : getSuperTypes(target)) {
                if (superType.equals(Object.class)) {
                    continue;
                }

                for (Method superTypeMethod : superType.getDeclaredMethods()) {
                    if (condition.apply(superTypeMethod )) {
                        methods.add(superTypeMethod);
                    }
                }
            }
        }
        catch (Exception e) {
            //nop
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
    public static Class[] getSuperTypes(Class from) {
        ArrayList<Class> superclasses = new ArrayList<Class>();

        collectInterfaces( from, superclasses );
        while ( !from.equals( Object.class ) && !from.isInterface() ) {
            superclasses.add( from.getSuperclass() );
            from = from.getSuperclass();
            collectInterfaces( from, superclasses );
        }

        final Class[] classes = new Class[superclasses.size()];
        superclasses.toArray(classes);
        return classes;
    }

    public static void collectInterfaces( Class from, Collection<Class> accumulator ) {
        for ( Class intface : from.getInterfaces() ) {
            accumulator.add( intface );
            collectInterfaces( intface, accumulator );
        }
    }

    public static boolean containsOverridingMethod( final Method[] allMethods, final Method methodToCheck ) {
        final int length = allMethods.length;
        Method method;
        for (int i = 0; i < length; i++) {
            method = allMethods[i];

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

    /**
     * Collects all methods from interfaces that match the given condition.
     * Methods are collected in reverse order to ensure proper precedence:
     * - Later interfaces override earlier interfaces (for diamond problem)
     * - Child interfaces override parent interfaces
     *
     * This ordering ensures that when looking for annotations, the last interface
     * in the implements clause wins in case of conflicts.
     *
     * @param condition Predicate to filter methods
     * @param target The class whose interfaces to scan
     * @return Array of methods from interfaces matching the condition
     */
    public static Method[] getInterfaceMethods(IPredicate<Method> condition, Class<?> target) {
        ArrayList<Method> methods = new ArrayList<Method>();
        collectInterfaceMethods(condition, target, methods, new HashSet<Class<?>>());

        Method[] array = new Method[methods.size()];
        methods.toArray(array);
        return array;
    }

    /**
     * Recursively collects methods from interfaces.
     * Uses visited set to avoid processing the same interface twice (diamond problem).
     */
    private static void collectInterfaceMethods(IPredicate<Method> condition, Class<?> target,
                                                 ArrayList<Method> methods, Set<Class<?>> visited) {
        if (target == null || target.equals(Object.class)) {
            return;
        }

        // First, collect from superclass interfaces (depth-first)
        if (!target.isInterface()) {
            Class<?> superclass = target.getSuperclass();
            if (superclass != null && !superclass.equals(Object.class)) {
                collectInterfaceMethods(condition, superclass, methods, visited);
            }
        }

        // Then collect from directly implemented/extended interfaces (left to right)
        // This ensures later interfaces can override earlier ones
        Class<?>[] interfaces = target.getInterfaces();
        for (Class<?> iface : interfaces) {
            if (!visited.contains(iface)) {
                visited.add(iface);

                // Recursively collect from parent interfaces first
                collectInterfaceMethods(condition, iface, methods, visited);

                // Then collect from this interface
                try {
                    for (Method method : iface.getDeclaredMethods()) {
                        if (condition.apply(method)) {
                            methods.add(method);
                        }
                    }
                } catch (Exception e) {
                    // Ignore exceptions during method collection
                }
            }
        }
    }

    /**
     * Finds the interface method that corresponds to the given class method.
     * Returns the method from the "winning" interface according to precedence rules:
     * - Last interface in implements clause wins over earlier interfaces
     * - Child interface wins over parent interface
     *
     * @param classMethod The method from the implementing class
     * @param targetClass The class that implements the interface
     * @return The corresponding interface method, or null if not found
     */
    public static Method getInterfaceMethod(Method classMethod, Class<?> targetClass) {
        // Collect all interfaces in precedence order (last wins)
        ArrayList<Class<?>> interfacesInOrder = new ArrayList<Class<?>>();
        collectInterfacesInOrder(targetClass, interfacesInOrder, new HashSet<Class<?>>());

        // Iterate in reverse order so last interface wins
        for (int i = interfacesInOrder.size() - 1; i >= 0; i--) {
            Class<?> iface = interfacesInOrder.get(i);
            try {
                Method interfaceMethod = iface.getDeclaredMethod(
                    classMethod.getName(),
                    classMethod.getParameterTypes()
                );
                if (interfaceMethod != null) {
                    return interfaceMethod;
                }
            } catch (NoSuchMethodException e) {
                // This interface doesn't have this method, continue searching
            }
        }

        return null;
    }

    /**
     * Collects all interfaces in precedence order (first to last).
     * Later interfaces in the list have higher precedence.
     */
    private static void collectInterfacesInOrder(Class<?> target, ArrayList<Class<?>> interfaces,
                                                  Set<Class<?>> visited) {
        if (target == null || target.equals(Object.class)) {
            return;
        }

        // First collect from superclass
        if (!target.isInterface()) {
            Class<?> superclass = target.getSuperclass();
            if (superclass != null && !superclass.equals(Object.class)) {
                collectInterfacesInOrder(superclass, interfaces, visited);
            }
        }

        // Then collect from directly implemented/extended interfaces (left to right)
        Class<?>[] directInterfaces = target.getInterfaces();
        for (Class<?> iface : directInterfaces) {
            if (!visited.contains(iface)) {
                visited.add(iface);

                // First collect parent interfaces
                collectInterfacesInOrder(iface, interfaces, visited);

                // Then add this interface (so child comes after parent)
                interfaces.add(iface);
            }
        }
    }

    /**
     * Finds an annotation on interface methods for a given class method.
     * Respects precedence rules: last interface wins in diamond problem.
     *
     * @param classMethod The method from the implementing class
     * @param targetClass The class that implements the interface
     * @param annotationType The annotation type to look for
     * @return The annotation from the interface method, or null if not found
     */
    public static <A extends Annotation> A getInterfaceAnnotation(Method classMethod,
                                                                    Class<?> targetClass,
                                                                    Class<A> annotationType) {
        Method interfaceMethod = getInterfaceMethod(classMethod, targetClass);
        if (interfaceMethod != null) {
            return getAnnotation(interfaceMethod, annotationType);
        }
        return null;
    }

}
