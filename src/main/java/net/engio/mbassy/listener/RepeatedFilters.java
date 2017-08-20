package net.engio.mbassy.listener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The repeated filters annotation is used to add multiple {@link Filter}s to
 * a single {@link ElementType#ANNOTATION_TYPE annotation type} when using
 * "inherited filters".
 *
 * @see Filter
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface RepeatedFilters {

    /**
     * An array of filters to be used for filtering {@link Handler}s.
     */
    Filter[] value();
}
