package net.engio.mbassy.bus.config;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * The bus configuration holds various parameters that can be used to customize the bus' runtime behaviour.
 */
public class BusConfiguration implements IBusConfiguration {

    // the registered properties
    private final Map<Object, Object> properties = new HashMap<Object, Object>();
    private final List<ConfigurationErrorHandler> errorHandlerList = new LinkedList<ConfigurationErrorHandler>();

    public BusConfiguration() {
        super();
    }

    @Override
    public IBusConfiguration setProperty(String name, Object value) {
        properties.put(name, value);
        return this;
    }

    @Override
    public <T> T getProperty(String name, T defaultValue) {
        return properties.containsKey(name) ? (T) properties.get(name) : defaultValue;
    }

    @Override
    public boolean hasProperty(String name) {
        return properties.containsKey(name);
    }

    @Override
    public <T extends Feature> T getFeature(Class<T> feature) {
        return (T) properties.get(feature);
    }

    @Override
    public IBusConfiguration addFeature(Feature feature) {
        properties.put(feature.getClass(), feature);
        return this;
    }

    @Override
    public IBusConfiguration addConfigurationErrorHandler(ConfigurationErrorHandler handler) {
        errorHandlerList.add(handler);
        return this;
    }

    @Override
    public void handleError(ConfigurationError error) {
        for(ConfigurationErrorHandler errorHandler : errorHandlerList){
            errorHandler.handle(error);
        }
    }
}
