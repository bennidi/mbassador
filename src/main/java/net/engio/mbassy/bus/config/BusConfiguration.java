package net.engio.mbassy.bus.config;

import java.util.HashMap;
import java.util.Map;

/**
 * The bus configuration holds various parameters that can be used to customize the bus' runtime behaviour.
 */
public class BusConfiguration implements IBusConfiguration {

    // the registered features
    private Map<Class<? extends Feature>, Feature> features = new HashMap<Class<? extends Feature>, Feature>();

    public BusConfiguration() {
        super();
    }

    @Override
    public <T extends Feature> T getFeature(Class<T> feature) {
        return (T)features.get(feature);
    }

    @Override
    public IBusConfiguration addFeature(Feature feature) {
        features.put(feature.getClass(), feature);
        return this;
    }

    @Override
    public IBusConfiguration addErrorHandler(ConfigurationErrorHandler handler) {
        return null;  // TODO: implement configuration validation
    }
}
