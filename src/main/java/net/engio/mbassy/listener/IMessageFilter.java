package net.engio.mbassy.listener;

/**
 * Message filters can be used to prevent certain messages to be delivered to a specific listener.
 * If a filter is used the message will only be delivered if it passes the filter(s)
 *
 * NOTE: A message filter must provide either a no-arg constructor.
 *
 * @author bennidi
 * Date: 2/8/12
 */
public interface IMessageFilter {

    /**
     * Evaluate the message to ensure that it matches the handler configuration
     *
     *
     * @param message the message to be delivered
     * @return
     */
	public boolean accepts(Object message, MessageHandlerMetadata metadata);

}
