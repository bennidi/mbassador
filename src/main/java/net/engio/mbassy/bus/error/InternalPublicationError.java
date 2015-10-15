package net.engio.mbassy.bus.error;

import net.engio.mbassy.bus.IMessagePublication;

/**
 * This type of publication error is used to communicate technical/library related errors as opposed to errors in client code, i.e. message handlers)
 *
 * @author bennidi
 *         Date: 15.10.15
 */
public class InternalPublicationError extends PublicationError{

    public InternalPublicationError(Throwable cause, String message, IMessagePublication publication) {
        super(cause, message, publication);
    }

    public InternalPublicationError(Throwable cause, String message) {
        super(cause, message);
    }

}
