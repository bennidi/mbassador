package org.mbassy.listener;

import java.lang.annotation.*;

/**
 * Mark any method of any object as a message handler and configure the handler
 * using different properties.
 *
 * @author bennidi
 * Date: 2/8/12
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Inherited
@Target(value = {ElementType.METHOD})
public @interface Listener {


	Filter[] filters() default {}; // no filters by default

    Mode dispatch() default Mode.Synchronous;

    int priority() default 0;

    boolean rejectSubtypes() default false;

}
