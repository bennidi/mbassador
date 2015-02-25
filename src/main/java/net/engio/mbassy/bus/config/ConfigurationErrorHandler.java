package net.engio.mbassy.bus.config;

/**
 * Respond to a {@link net.engio.mbassy.bus.config.ConfigurationError} with any kind of action.
 *
 * @author bennidi
 *         Date: 8/29/14
 */
public interface ConfigurationErrorHandler {

    /**
     * Called when a misconfiguration is detected on a {@link net.engio.mbassy.bus.config.IBusConfiguration}
     * @param error The error that represents the detected misconfiguration.
     */
    void handle(ConfigurationError error);
}
