package net.engio.mbassy.bus;

import net.engio.mbassy.subscription.Subscription;

/**
*
*
* @author durron597
*         Date: 8/15/13
*/
public interface IMessagePublication {
	/**
	 * Add a {@link net.engio.mbassy.dispatch.Subscription} to this IMessagePublication
	 * 
	 * @param subscription
	 * @return true if the {@link net.engio.mbassy.dispatch.Subscription} was added successfully
	 */
	boolean add(Subscription subscription);
	
	/**
	 * Defines whether this publication has finished 
	 * 
	 * @return true if this publication has finished
	 */
	boolean isFinished();
	
	/**
	 * Defines whether this publication is running
	 * 
	 * @return true if this publication is running
	 */
	boolean isRunning();
	
	/**
	 * Defines whether this publication is still scheduled
	 * 
	 * @return true if this publication is still scheduled
	 */
	boolean isScheduled();
	
	/**
	 * Defines whether this publication is still scheduled
	 * 
	 * @return true if this publication is still scheduled
	 */
	boolean isError();
	
	/**
	 * Used internally by the dispatcher to indicate whether
	 * this Publication has been delivered. Users should not
	 * need this method.
	 */
	void markDelivered();
	
	/**
	 * Used internally to mark the publication as being scheduled. Uses the
	 * builder pattern for convenience.
	 * 
	 * @return this IMessagePublication as part of the builder pattern.
	 */
	IMessagePublication markScheduled();
	
	/**
	 * Used internally to mark the publication as having returned an error.
	 * Uses the builder pattern for convenience.
	 * 
	 * @return this IMessagePublication as part of the builder pattern.
	 */
	IMessagePublication setError();
	
	/**
	 * Indicates whether this is a publication of a
	 * {@Link net.engio.mbassy.common.DeadMessage}.
	 * 
	 * @return true if this is publication of a
	 * {@Link net.engio.mbassy.common.DeadMessage}.
	 */
	boolean isDeadEvent();
	
	/**
	 * Indicates whether this is a publication of a
	 * {@Link net.engio.mbassy.common.FilteredMessage}.
	 * 
	 * @return true if this is publication of a
	 * {@Link net.engio.mbassy.common.FilteredMessage}.
	 */
	boolean isFilteredEvent();
}