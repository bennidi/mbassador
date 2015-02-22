package net.engio.mbassy.bus.config;

/**
 * Configuration errors represent specific misconfigurations of features in a {@link net.engio.mbassy.bus.config.IBusConfiguration}
 *
 * @author bennidi
 *         Date: 8/29/14
 */
public class ConfigurationError {

    private Class<? extends Feature> featureType;
    private Feature feature;
    private String message;

    public ConfigurationError(Class<? extends Feature> featureType, Feature feature, String message) {
        this.featureType = featureType;
        this.feature = feature;
        this.message = message;
    }

    public static ConfigurationError Missing(Class<? extends Feature> featureType){
        return new ConfigurationError(featureType, null, "An expected feature was missing. Use addFeature() in IBusConfiguration to add features.");
    }

    @Override
    public String toString() {
        return "Error for " + featureType + ":" + message +
                ", (" + feature + ")";
    }
}
