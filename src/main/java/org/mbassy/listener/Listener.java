package org.mbassy.listener;

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

	Filter[] filters() default {}; // no filters by default

    Mode dispatch() default Mode.Synchronous;

    int priority() default 0;

}
