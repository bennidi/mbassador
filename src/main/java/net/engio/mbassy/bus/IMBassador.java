package net.engio.mbassy.bus;

import java.util.concurrent.TimeUnit;

import net.engio.mbassy.PublicationError;

/**
*
*
* @author durron597
*         Date: 8/15/13
*/
public interface IMBassador<T, P extends IMessageBus.IPostCommand> extends IMessageBus<T, P> {
	/**
	 * Publish a message asynchronously. The returned {@link IMessagePublication}
	 * has information that indicates the state of this newly published message.
	 * 
	 * @param message the message to be published
	 * @return the IMessagePublication that represents this asynchronous dispatch.
	 */
	IMessagePublication publishAsync(T message);
	
	/**
	 * Publish a message asynchronously. The returned {@link IMessagePublication}
	 * has information that indicates the state of this newly published message.
	 * The message dispatch will fail if it takes longer than the given timeout.
	 * 
	 * @param message the message to be published
	 * @param timeout the maximum time to wait
	 * @param unit the time unit of the timeout argument
	 * @return the IMessagePublication that represents this asynchronous dispatch.
	 */
	IMessagePublication publishAsync(T message, long timeout, TimeUnit unit);
}
