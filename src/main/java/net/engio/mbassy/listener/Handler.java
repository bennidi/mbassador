package net.engio.mbassy.listener;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark any method of any object(=listener) as a message handler and configure the handler
 * using different properties.
 *
 * @author bennidi
 *         Date: 2/8/12
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Inherited
@Target(value = {ElementType.METHOD})
public @interface Handler {

    /**
     * Add any numbers of filters to the handler. All filters are evaluated before the handler
     * is actually invoked, which is only if all the filters accept the message.
     */
    Filter[] filters() default {};

    /**
     * Define the mode in which a message is delivered to each listener. Listeners can be notified
     * sequentially or concurrently.
     */
    Mode delivery() default Mode.Sequential;

    /**
     * Handlers are ordered by priority and handlers with higher priority are processed before
     * those with lower priority, i.e. Influence the order in which different handlers that consume
     * the same message type are invoked.
     */
    int priority() default 0;

    /**
     * Define whether or not the handler accepts sub types of the message type it declares in its
     * signature.
     */
    boolean rejectSubtypes() default false;


    /**
     * Enable or disable the handler. Disabled handlers do not receive any messages.
     * This property is useful for quick changes in configuration and necessary to disable
     * handlers that have been declared by a superclass but do not apply to the subclass
     */
    boolean enabled() default true;

}
