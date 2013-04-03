package net.engio.mbassy;

/**
 * Todo: Add javadoc
 *
 * @author bennidi
 *         Date: 3/29/13
 */
public class MessageBusException extends Exception{

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
