package net.engio.mbassy.bus.config;

import java.util.HashMap;
import java.util.Map;

/**
 * The bus configuration holds various parameters that can be used to customize the bus' runtime behaviour.
 */
public class BusConfiguration implements IBusConfiguration {

    /**
     * Creates a new instance, using the default settings of 2 dispatchers, and
     * asynchronous handlers with an initial count equal to the number of
     * available processors in the machine, with maximum count equal to
     * 2 * the number of available processors. Uses {@link Runtime#availableProcessors()} to
     * determine the number of available processors
     *
     * @deprecated Use feature driven configuration instead
     **/
    @Deprecated()
    public static BusConfiguration SyncAsync() {
        BusConfiguration defaultConfig = new BusConfiguration();
        defaultConfig.addFeature(Feature.SyncPubSub.Default());
        defaultConfig.addFeature(Feature.AsynchronousHandlerInvocation.Default());
        defaultConfig.addFeature(Feature.AsynchronousMessageDispatch.Default());
        return defaultConfig;
    }

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
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
