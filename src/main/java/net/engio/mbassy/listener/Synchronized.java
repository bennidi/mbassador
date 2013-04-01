package net.engio.mbassy.listener;

import java.lang.annotation.*;

/**
 * A handler marked with this annotation is guaranteed to be invoked in a thread-safe manner, that is, no
 * other running message publication will be able to invoke this handler as long as it has not done its work.
 *
 * @author bennidi
 *         Date: 3/31/13
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Inherited
@Target(value = {ElementType.METHOD})
public @interface Synchronized {
}
