package org.mbassy;

import org.mbassy.filter.Filter;

import java.lang.annotation.*;

/**
 * TODO. Insert class description here
 * <p/>
 * User: benni
 * Date: 2/8/12
 * Time: 3:35 PM
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Inherited
@Target(value = {ElementType.METHOD})
public @interface Listener {

	Filter[] value() default {}; // no filters by default

    Dispatch mode() default Dispatch.Synchronous;

    public static enum Dispatch{
        Synchronous,Asynchronous
    }

}
