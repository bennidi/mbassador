package net.engio.mbassy.listener;

import net.engio.mbassy.dispatch.HandlerInvocation;
import net.engio.mbassy.dispatch.MethodHandleInvocation;
import net.engio.mbassy.dispatch.ReflectiveHandlerInvocation;

import java.lang.annotation.*;

/**
 * Mark any method of any class(=listener) as a message handler and configure the handler
 * using different properties.
 *
 * <p>This annotation can be placed on:</p>
 * <ul>
 *     <li><strong>Class methods</strong> - Traditional usage</li>
 *     <li><strong>Interface methods</strong> - Implementing classes inherit the annotation</li>
 *     <li><strong>Meta-annotations</strong> - Create custom handler annotations</li>
 * </ul>
 *
 * <h3>Interface Annotation Inheritance</h3>
 * <p>When {@code @Handler} is placed on an interface method, implementing classes automatically
 * inherit the handler configuration:</p>
 *
 * <pre>
 * interface EventProcessor {
 *     {@literal @}Handler(priority = 10)
 *     void process(Event event);
 * }
 *
 * class MyProcessor implements EventProcessor {
 *     {@literal @}Override  // No {@literal @}Handler needed - inherited from interface
 *     public void process(Event event) {
 *         // Automatically registered with priority = 10
 *     }
 * }
 * </pre>
 *
 * <h3>Precedence Rules</h3>
 * <ul>
 *     <li>Class annotations override interface annotations</li>
 *     <li>When implementing multiple interfaces with the same method, last interface wins</li>
 *     <li>Filters, priority, and all handler settings are inherited from interfaces</li>
 * </ul>
 *
 * @author bennidi
 *         Date: 2/8/12
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Inherited
@Target(value = {ElementType.METHOD,ElementType.ANNOTATION_TYPE})
public @interface Handler {

    /**
     * Add any number of filters to the handler. All filters are evaluated before the handler
     * is actually invoked, which is only if all the filters accept the message.
     *
     * <p>Filters must implement {@link IMessageFilter} and provide a no-arg constructor.</p>
     *
     * <p>Example:</p>
     * <pre>
     * class LargeFileFilter implements IMessageFilter&lt;File&gt; {
     *     public boolean accepts(File file, SubscriptionContext context) {
     *         return file.length() &gt;= 10000;
     *     }
     * }
     *
     * {@literal @}Handler(filters = {@literal @}Filter(LargeFileFilter.class))
     * public void handleLargeFile(File file) { ... }
     * </pre>
     */
    Filter[] filters() default {};

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
     * The modern implementation uses MethodHandle for high-performance dispatch.
     *
     */
    Class<? extends HandlerInvocation> invocation() default MethodHandleInvocation.class;


}
