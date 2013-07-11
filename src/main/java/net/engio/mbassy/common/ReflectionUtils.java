package net.engio.mbassy.common;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author bennidi
 *         Date: 2/16/12
 *         Time: 12:14 PM
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

    /**
     * Traverses the class hierarchy upwards, starting at the given subclass, looking
     * for an override of the given methods -> finds the bottom most override of the given
     * method if any exists
     *
     * @param overridingMethod
     * @param subclass
     * @return
     */
    public static Method getOverridingMethod(final Method overridingMethod, final Class subclass) {
        Class current = subclass;
        while (!current.equals(overridingMethod.getDeclaringClass())) {
            try {
                return current.getDeclaredMethod(overridingMethod.getName(), overridingMethod.getParameterTypes());
            } catch (NoSuchMethodException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    public static Set<Class> getSuperclasses(Class from) {
        Set<Class> superclasses = new HashSet<Class>();
        collectInterfaces(from, superclasses);
        while (!from.equals(Object.class) && !from.isInterface()) {
            superclasses.add(from.getSuperclass());
            from = from.getSuperclass();
            collectInterfaces(from, superclasses);
        }
        return superclasses;
    }

    public static void collectInterfaces(Class from, Set<Class> accumulator){
        for(Class intface : from.getInterfaces()){
            accumulator.add(intface);
            collectInterfaces(intface, accumulator);
        }
    }

    public static boolean containsOverridingMethod(final List<Method> allMethods, final Method methodToCheck) {
        for (Method method : allMethods) {
            if (isOverriddenBy(methodToCheck, method)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isOverriddenBy(Method superclassMethod, Method subclassMethod) {
        // if the declaring classes are the same or the subclass method is not defined in the subclass
        // hierarchy of the given superclass method or the method names are not the same then
        // subclassMethod does not override superclassMethod
        if (superclassMethod.getDeclaringClass().equals(subclassMethod.getDeclaringClass())
                || !superclassMethod.getDeclaringClass().isAssignableFrom(subclassMethod.getDeclaringClass())
                || !superclassMethod.getName().equals(subclassMethod.getName())) {
            return false;
        }

        Class[] superClassMethodParameters = superclassMethod.getParameterTypes();
        Class[] subClassMethodParameters = subclassMethod.getParameterTypes();
        // method must specify the same number of parameters
        //the parameters must occur in the exact same order
        for (int i = 0; i < subClassMethodParameters.length; i++) {
            if (!superClassMethodParameters[i].equals(subClassMethodParameters[i])) {
                return false;
            }
        }
        return true;
    }

}
