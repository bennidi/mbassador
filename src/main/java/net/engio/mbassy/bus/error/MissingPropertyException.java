package net.engio.mbassy.bus.error;

/**
 * This exception is thrown when a property value that is unavailable at runtime is accessed.
 * It indicates that some parts of the runtime configuration are actually incompatible,
 * i.e. one component is trying to access a feature of another component that does not
 * provide the feature (e.g. asynchronous functionality within a synchronous bus)
 */
public class MissingPropertyException extends RuntimeException{

    public MissingPropertyException(String message) {
        super(message);
    }
}
