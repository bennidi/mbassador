package net.engio.mbassy.listener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The filter annotation is used to add filters to message listeners.
 * It references a class that implements the IMessageFilter interface.
 * The filter will be used to check whether a message should be delivered
 * to the listener or not.
 *
 * @author bennidi
 *         Date: 2/14/12
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.ANNOTATION_TYPE})
public @interface Filter {

    /**
     * The class that implements the filter.
     * IMPORTANT: A filter always needs to provide a non-arg constructor
     */
    Class<? extends IMessageFilter> value();
}
