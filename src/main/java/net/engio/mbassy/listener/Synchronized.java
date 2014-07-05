package net.engio.mbassy.listener;

import java.lang.annotation.*;

/**
 * A handler marked with this annotation is guaranteed to be invoked in a thread-safe manner, that is, no
 * other running message publication will be able to invoke this or any other synchronized handler of the same
 * listener until the handler completed. It is equal to wrapping the handler code in a synchronized{} block.
 * This feature will reduce performance of message publication. Try to avoid shared mutable state whenever possible
 * and use immutable data instead.
 *
 * Note: Unsynchronized handlers may still be invoked concurrently with synchronized ones
 *
 *
 *
 * @author bennidi
 *         Date: 3/31/13
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Inherited
@Target(value = {ElementType.METHOD, ElementType.ANNOTATION_TYPE})
public @interface Synchronized {
}
