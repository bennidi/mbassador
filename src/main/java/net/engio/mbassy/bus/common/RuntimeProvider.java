package net.engio.mbassy.bus.common;

import net.engio.mbassy.bus.BusRuntime;

/**
 * Each message bus provides a runtime object to access its dynamic features and runtime configuration.
 */
public interface RuntimeProvider {

    BusRuntime getRuntime();
}
