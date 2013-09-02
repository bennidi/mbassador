package net.engio.mbassy.bus;

/**
 * Each message bus provides a runtime object to access its dynamic features and runtime configuration.
 */
public interface RuntimeProvider {

    BusRuntime getRuntime();
}
