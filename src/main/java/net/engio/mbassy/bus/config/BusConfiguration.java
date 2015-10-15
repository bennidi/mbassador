package net.engio.mbassy.bus.config;

import net.engio.mbassy.bus.error.IPublicationErrorHandler;

import java.util.*;

/**
 * {@inheritDoc}
 */
public class BusConfiguration implements IBusConfiguration {

    // the registered properties
    private final Map<Object, Object> properties = new HashMap<Object, Object>();
    // these are transferred to the bus to receive all errors that occur during message dispatch or message handling
    private final List<IPublicationErrorHandler> publicationErrorHandlers = new ArrayList<IPublicationErrorHandler>();

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
    public final BusConfiguration addPublicationErrorHandler(IPublicationErrorHandler handler) {
        publicationErrorHandlers.add(handler);
        return this;
    }

    @Override
    public Collection<IPublicationErrorHandler> getRegisteredPublicationErrorHandlers() {
        return Collections.unmodifiableCollection(publicationErrorHandlers);
    }
}
