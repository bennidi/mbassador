package org.mbassy;

/**
 * Publication error handlers are provided with a publication error every time an error occurs during message publication.
 * A handler might fail with an exception, not be accessible because of the presence of a security manager
 * or other reasons might lead to failures during the message publication process.
 *
 * <p/>
 * @author bennidi
 * Date: 2/22/12
 */
public interface IPublicationErrorHandler {

    /**
     * Handle the given publication error.
     *
     * @param error
     */
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
