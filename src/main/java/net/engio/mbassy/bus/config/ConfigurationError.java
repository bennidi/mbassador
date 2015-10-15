package net.engio.mbassy.bus.config;

/**
 * Configuration errors represent specific invalid configurations of a feature in a {@link net.engio.mbassy.bus.config.IBusConfiguration}
 * An invalid feature configuration is assumed to render the bus dysfunctional and as such is thrown as an unchecked exception.
 *
 * @author bennidi
 *         Date: 8/29/14
 */
public class ConfigurationError extends RuntimeException{

    private String message;

    private ConfigurationError(String message) {
        this.message = message;
    }

    public static ConfigurationError MissingFeature(Class<? extends Feature> featureType){
        return new ConfigurationError("The expected feature " + featureType +  " was missing. Use addFeature() in IBusConfiguration to add features.");
    }

    @Override
    public String toString() {
        return message;
    }
}
