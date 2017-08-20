package net.engio.mbassy.listener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The include filters directive can be used to add multiple {@link Filter}s to
 * a single {@link ElementType#ANNOTATION_TYPE annotation type}. This allows to add
 * filters to message handlers with one or more user-defined annotations.
 *
 * @see Filter
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface IncludeFilters {

    /**
     * An array of filters to be used for filtering {@link Handler}s.
     */
    Filter[] value();
}
