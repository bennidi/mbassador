package net.engio.mbassy.listener;

import net.engio.mbassy.dispatch.HandlerInvocation;
import net.engio.mbassy.dispatch.ReflectiveHandlerInvocation;

import java.lang.annotation.*;

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
     * Add any numbers of filters to the handler. All filters are evaluated before the handler
     * is actually invoked, which is only if all the filters accept the message.
     */
    Filter[] filters() default {};
    
    
    /**
     * Defines a filter condition as Expression Language. This can be used to filter the events based on 
     * attributes of the event object. Note that the expression must resolve to either
     * <code>true</code> to allow the event or <code>false</code> to block it from delivery to the handler. 
     * The message itself is available as "msg" variable. 
     * @return the condition in EL syntax.
     */
    String condition() default "";

    /**
     * Define the mode in which a message is delivered to each listener. Listeners can be notified
     * sequentially or concurrently.
     */
    Invoke delivery() default Invoke.Synchronously;

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


    /**
     * Each handler call is implemented as an invocation object that implements the invocation mechanism.
     * The basic implementation uses reflection and is the default. It is possible though to provide a custom
     * invocation to add additional logic.
     *
     * Note: Providing a custom invocation will most likely reduce performance, since the JIT-Compiler
     * can not do some of its sophisticated byte code optimizations.
     *
     */
    Class<? extends HandlerInvocation> invocation() default ReflectiveHandlerInvocation.class;


}
