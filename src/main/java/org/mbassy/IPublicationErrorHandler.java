package org.mbassy;

/**
 * TODO. Insert class description here
 * <p/>
 * @author bennidi
 * Date: 2/22/12
 */
public interface IPublicationErrorHandler {

	public void handleError(PublicationError error);

    // This is the default error handler it will simply log to standard out and
    // print stack trace if available
    static final class ConsoleLogger implements IPublicationErrorHandler {
        @Override
        public void handleError(PublicationError error) {
            System.out.println(error);
            if (error.getCause() != null) error.getCause().printStackTrace();
        }
    }

    ;
}
