package net.engio.mbassy.listener;

import java.lang.annotation.*;

/**
 *
 * Configure how the listener is referenced in the event bus.
 * The bus will use either strong or weak references to its registered listeners,
 *  depending on which reference type (@see References) is set.
 *
 * @author bennidi
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Inherited
public @interface Listener {

    /**
     * BY DEFAULT, MBassador uses {@link java.lang.ref.WeakReference}. It is possible to use
     * strong instead.
     *
     */
    References references() default References.Weak;

}
