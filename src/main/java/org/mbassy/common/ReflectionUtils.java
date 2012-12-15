package org.mbassy.common;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

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

    public static Method getOverridingMethod(Method overridingMethod, Class subclass) {
        Class current = subclass;
        while(!current.equals(overridingMethod.getDeclaringClass())){
            try {
                Method overridden = current.getDeclaredMethod(overridingMethod.getName(), overridingMethod.getParameterTypes());
                return overridden;
            } catch (NoSuchMethodException e) {
                current = current.getSuperclass();
            }
        }
        return null;
    }

    public static List<Method> withoutOverridenSuperclassMethods(List<Method> allMethods) {
        List<Method> filtered = new LinkedList<Method>();
        for (Method method : allMethods) {
            if (!containsOverridingMethod(allMethods, method)) filtered.add(method);
        }
        return filtered;
    }

    public static Collection<Class> getSuperclasses(Class from) {
        Collection<Class> superclasses = new LinkedList<Class>();
        while (!from.equals(Object.class)) {
            superclasses.add(from.getSuperclass());
            from = from.getSuperclass();
        }
        return superclasses;
    }

    public static boolean containsOverridingMethod(List<Method> allMethods, Method methodToCheck) {
        for (Method method : allMethods) {
            if (isOverriddenBy(methodToCheck, method)) return true;
        }
        return false;
    }

    private static boolean isOverriddenBy(Method superclassMethod, Method subclassMethod) {
        // if the declaring classes are the same or the subclass method is not defined in the subbclass
        // hierarchy of the given superclass method or the method names are not the same then
        // subclassMethod does not override superclassMethod
        if (superclassMethod.getDeclaringClass().equals(subclassMethod.getDeclaringClass())
                || !superclassMethod.getDeclaringClass().isAssignableFrom(subclassMethod.getDeclaringClass())
                || !superclassMethod.getName().equals(subclassMethod.getName())) {
            return false;
        }

        Class[] superClassMethodParameters = superclassMethod.getParameterTypes();
        Class[] subClassMethodParameters = superclassMethod.getParameterTypes();
        // method must specify the same number of parameters
        if(subClassMethodParameters.length != subClassMethodParameters.length){
            return false;
        }
        //the parameters must occur in the exact same order
        for(int i = 0 ; i< subClassMethodParameters.length; i++){
           if(!superClassMethodParameters[i].equals(subClassMethodParameters[i])){
               return false;
           }
        }
        return true;
    }

}
