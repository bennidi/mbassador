package net.engio.mbassy.listener;

import net.engio.mbassy.subscription.SubscriptionContext;

/**
 * Message filters can be used to control what messages are delivered to a specific message handler.
 * Filters are attached to message handler using the @Handler annotation.
 * If a message handler specifies filters, the filters accepts(...) method will be checked before the actual handler is invoked.
 * The handler will be invoked only if each filter accepted the message.
 *
 * <p>This is a functional interface and can be implemented using lambda expressions:</p>
 *
 * <pre>
 * <code>
 * // As a lambda expression
 * IMessageFilter&lt;String&gt; urlFilter = (msg, ctx) -&gt; msg.startsWith("http");
 *
 * // As a class implementation
 * class UrlFilter implements IMessageFilter&lt;String&gt; {
 *     public boolean accepts(String message, SubscriptionContext context) {
 *         return message.startsWith("http");
 *     }
 * }
 *
 * // Usage in handler annotation
 * {@literal @}Handler(filters = {@literal @}Filter(UrlFilter.class))
 * public void handleUrl(String message) { ... }
 *
 * bus.post("http://www.infoq.com"); // will be delivered
 * bus.post("www.stackoverflow.com"); // will not be delivered
 *
 * </code>
 * </pre>
 *
 * NOTE: When using class-based filters, the filter must provide a no-arg constructor.
 *
 * @param <M> The type of message this filter accepts
 * @author bennidi
 *         Date: 2/8/12
 */
@FunctionalInterface
public interface IMessageFilter<M> {

    /**
     * Check whether the message matches some criteria
     *
     * @param message The message to be handled by the handler
     * @param  context The context object containing a description of the message handler and the bus environment
     * @return  true: if the message matches the criteria and should be delivered to the handler
     *          false: otherwise
     */
    boolean accepts(M message, SubscriptionContext context);
}
