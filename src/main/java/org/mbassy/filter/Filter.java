package org.mbassy.filter;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * THe filter annotation is used to add filters to message listeners.
 * It references a class that implements the MessageFilter interface.
 * The object filter will be used to check whether a message should be delivered
 * to the message listener or not.
 *
 * <p/>
 * @author  benni
 * Date: 2/14/12
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target(value = {ElementType.ANNOTATION_TYPE})
public @interface Filter {

	Class<? extends MessageFilter> value();
}
