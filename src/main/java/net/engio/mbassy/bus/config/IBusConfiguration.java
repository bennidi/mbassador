package net.engio.mbassy.bus.config;

import net.engio.mbassy.bus.error.IPublicationErrorHandler;

import java.util.Collection;

/**
 * The configuration of message bus instances is feature driven, e.g. configuration parameters
 * are grouped into {@link Feature}.
 *
 * Each bus will look for the features it requires and configure them according to the provided configuration.
 * If a required feature is not found the bus will publish a {@link ConfigurationError}
 * to the {@link ConfigurationErrorHandler}
 *
 * @author bennidi.
 */
public interface IBusConfiguration{

    /**
     * Set a property which will be read by the message bus constructor. Existing value will be overwritten.
     * Null values are supported (checking for existence of property will return <code>true</code> even if set to <code>null</code>).
     *
     * @param name The name of the property. Note: Each implementation may support different properties.
     * @param value The value of the property.
     * @return  A reference to <code>this</code> bus configuration.
     */
    IBusConfiguration setProperty(String name, Object value);

    /**
     * Read a property from this configuration.
     *
     * @param name  The name of the property to be read.
     * @param defaultValue  The value to be returned if property was not found
     * @param <T>  The type of property
     * @return The value associated with the given property name or <code>defaultValue</code> if not present
     */
    <T> T getProperty(String name, T defaultValue);

    /**
     * Check whether a property has been set.
     *
     * @return true if property was set (even if set to null)
     *         false otherwise
     */
    boolean hasProperty(String name);


    /**
     * Get a registered feature by its type (class).
     *
     */
    <T extends Feature> T getFeature(Class<T> feature);

    /**
     * Add a feature to the given configuration, replacing any existing feature of the same type.
     *
     * @param feature The feature to add
     * @return  A reference to <code>this</code> bus configuration.
     */
    IBusConfiguration addFeature(Feature feature);

    /**
     * Add a handler that will be called whenever a publication error occurs.
     * See {@link net.engio.mbassy.bus.error.PublicationError}
     *
     * @param handler  The handler to be added to the list of handlers
     * @return A reference to <code>this</code> bus configuration.
     */
    BusConfiguration addPublicationErrorHandler(IPublicationErrorHandler handler);

    /**
     * Get an unmodifiable collection of all registered publication error handlers
     */
    Collection<IPublicationErrorHandler> getRegisteredPublicationErrorHandlers();


    /**
     * A collection of properties commonly used by different parts of the library.
     *
     * @author bennidi
     *         Date: 22.02.15
     */
    final class Properties {

        public static final String BusId = "bus.id";
        public static final String PublicationErrorHandlers = "bus.handlers.error";
        public static final String AsynchronousHandlerExecutor = "bus.handlers.async-executor";

    }
}
