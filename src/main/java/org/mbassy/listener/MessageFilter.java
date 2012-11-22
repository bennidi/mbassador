package org.mbassy.listener;

/**
 * Object filters can be used to prevent certain messages to be delivered to a specific listener.
 * If a filter is used the message will only be delivered if it passes the filter(s)
 *
 * @author bennidi
 * Date: 2/8/12
 */
public interface MessageFilter {

    /**
     * Evaluate the message and listener to ensure that the message should be handled by the listener
     *
     *
     * @param event the event to be delivered
     * @param listener the listener instance that would receive the event if it passes the filter
     * @return
     */
	public boolean accepts(Object event, Object listener);


	public static final class All implements MessageFilter {

		@Override
		public boolean accepts(Object event, Object listener) {
			return true;
		}
	}

    public static final class None implements MessageFilter {

        @Override
        public boolean accepts(Object event, Object listener) {
            return false;
        }
    }


}
