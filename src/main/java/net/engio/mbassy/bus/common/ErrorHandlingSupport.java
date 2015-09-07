package net.engio.mbassy.bus.common;

import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;

import java.util.Collection;

/**
 * Publication errors may occur at various points of time during message delivery. A handler may throw an exception,
 * may not be accessible due to security constraints or is not annotated properly.
 * In any of all possible cases a publication error is created and passed to each of the registered error handlers.
 * Error handlers can be added via the {@link IBusConfiguration}.
 *
 */

public interface ErrorHandlingSupport {


    /**
     * Get all registered instance of {@link IPublicationErrorHandler}
     *
     * @return  an immutable collection containing all the registered error handlers
     */
    Collection<IPublicationErrorHandler> getRegisteredErrorHandlers();

}
