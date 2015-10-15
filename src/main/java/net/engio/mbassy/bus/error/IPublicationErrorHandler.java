package net.engio.mbassy.bus.error;

/**
 * Publication error handlers are provided with a publication error every time an
 * error occurs during message publication.
 * A handler might fail with an exception, not be accessible because of the presence
 * of a security manager or other reasons might lead to failures during the message publication process.
 * <p/>
 *
 * @author bennidi
 *         Date: 2/22/12
 */
@SuppressWarnings("PMD.UnusedModifier")
public interface IPublicationErrorHandler {

    /**
     * Handle the given publication error.
     *
     * @param error The PublicationError to handle.
     */
    void handleError(PublicationError error);

    /**
     * The default error handler will simply log to standard out and
     * print the stack trace if available.
     */
    final class ConsoleLogger implements IPublicationErrorHandler {

        private final boolean printStackTrace;

        public ConsoleLogger() {
            this(false);
        }

        public ConsoleLogger(boolean printStackTrace) {
            this.printStackTrace = printStackTrace;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void handleError(final PublicationError error) {

            // Printout the error itself
            System.out.println(error);

            // Printout the stacktrace from the cause.
            if (printStackTrace && error.getCause() != null) {
                error.getCause().printStackTrace();
            }
        }
    }
}
