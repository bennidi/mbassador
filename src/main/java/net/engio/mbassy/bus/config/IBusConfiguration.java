package net.engio.mbassy.bus.config;

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
     * Get a registered feature by its type (class).
     *
     * @param feature
     * @param <T>
     * @return
     */
    <T extends Feature> T getFeature(Class<T> feature);

    IBusConfiguration addFeature(Feature feature);
}
