package net.engio.mbassy.error;

/**
 * @author bennidi
 * @author dorkbox, llc
 *         Date: 2/2/15
 */
public interface ErrorHandlingSupport {

    /**
     * Publication errors may occur at various points of time during message delivery. A handler may throw an exception,
     * may not be accessible due to security constraints or is not annotated properly.
     * In any of all possible cases a publication error is created and passed to each of the registered error handlers.
     * A call to this method will add the given error handler to the chain
     */
    void addErrorHandler(IPublicationErrorHandler errorHandler);

    void handlePublicationError(PublicationError error);
}
