package net.engio.mbassy.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Mark any method of any class(=listener) as a message handler and configure the handler
 * using different properties.
 *
 * @author bennidi
 *         Date: 2/8/12
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Inherited
@Target(value = {ElementType.METHOD,ElementType.ANNOTATION_TYPE})
public @interface Handler {

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

    /**
     * Var-Arg. Should this handler accept variable arguments?
     * <p>
     * IE: should <b>foo</b> get dispatched to a handler registered as: <b>blah(String... args){}</b>
     * <p>
     * <p>
     * <b>Normally</b> the only message to be received would be <b>new String[]{"boo", "bar"}</b>
     */
    boolean vararg() default false;
}
