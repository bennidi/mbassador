package net.engio.mbassy.bus.config;

import net.engio.mbassy.bus.error.IPublicationErrorHandler;

import java.util.Collection;

/**
 * The configuration of message bus instances is feature driven, e.g. configuration parameters
 * are grouped into {@link Feature}.
 *
 * Features can be added to a bus configuration to be used later in the instantiation process of the message bus.
 * Each bus will look for the features it requires and configure them according to the provided configuration. If a required feature is not found the bus will publish a {@link ConfigurationError}
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
     * Add a handler that is called when a misconfiguration is detected.
     */
    IBusConfiguration addConfigurationErrorHandler(ConfigurationErrorHandler handler);

    /**
     * Calls all ConfigurationErrorHandlers
     */
    void handleError(ConfigurationError error);

    BusConfiguration addPublicationErrorHandler(IPublicationErrorHandler handler);

    Collection<IPublicationErrorHandler> getRegisteredPublicationErrorHandlers();


}
