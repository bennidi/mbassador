package net.engio.mbassy.error;

/**
 * The universal exception type for message bus implementations.
 *
 * @author bennidi
 *         Date: 3/29/13
 */
public class MessageBusException extends Exception {
    private static final long serialVersionUID = 1L;

    public MessageBusException() {
    }

    public MessageBusException(String message) {
        super(message);
    }

    public MessageBusException(String message, Throwable cause) {
        super(message, cause);
    }

    public MessageBusException(Throwable cause) {
        super(cause);
    }
}
