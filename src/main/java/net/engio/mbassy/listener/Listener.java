package net.engio.mbassy.listener;

import java.lang.annotation.*;

/**
 *
 * This annotation is meant to carry configuration that is shared among all instances of the annotated
 * listener. Supported configurations are:
 *
 *  Reference type: The bus will use either strong or weak references to its registered listeners,
 *  depending on which reference type (@see References) is set
 *
 * @author bennidi
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.TYPE, ElementType.ANNOTATION_TYPE})
@Inherited
public @interface Listener {

    /**
     * By default, references to message listeners are weak to eliminate risks of memory leaks.
     * It is possible to use strong references instead.
     *
     * @return
     */
    References references() default References.Weak;

}
