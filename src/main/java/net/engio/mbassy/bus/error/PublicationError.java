package net.engio.mbassy.bus.error;

import net.engio.mbassy.bus.IMessagePublication;
import net.engio.mbassy.subscription.SubscriptionContext;

import java.lang.reflect.Method;

/**
 * Publication errors are created when object publication fails
 * for some reason and contain details as to the cause and location
 * where they occurred.
 * <p/>
 *
 * @author bennidi
 *         Date: 2/22/12
 *         Time: 4:59 PM
 */
public class PublicationError{

    // Internal state
    private Throwable cause;
    private String message;
    private Method handler;
    private Object listener;
    private Object publishedMessage;


    /**
     * Compound constructor, creating a PublicationError from the supplied objects.
     *
     * @param cause           The Throwable giving rise to this PublicationError.
     * @param message         The message to send.
     * @param handler        The method where the error was created.
     * @param listener The object in which the PublicationError was generated.
     * @param publishedObject The published object which gave rise to the error.
     */
    public PublicationError(final Throwable cause,
                            final String message,
                            final Method handler,
                            final Object listener,
                            final Object publishedObject) {

        this.cause = cause;
        this.message = message;
        this.handler = handler;
        this.listener = listener;
        this.publishedMessage = publishedObject;
    }

    public PublicationError(final Throwable cause,
                            final String message,
                            final IMessagePublication publication) {
        this.cause = cause;
        this.message = message;
        this.publishedMessage = publication != null ? publication.getMessage() : null;
    }

    public PublicationError(final Throwable cause,
                            final String message,
                            final SubscriptionContext context) {
        this.cause = cause;
        this.message = message;
        this.handler = context.getHandler().getMethod();
    }


    /**
     * Default constructor.
     */
    public PublicationError() {
        super();
    }

    /**
     * @return The Throwable giving rise to this PublicationError.
     */
    public Throwable getCause() {
        return cause;
    }

    /**
     * Assigns the cause of this PublicationError.
     *
     * @param cause A Throwable which gave rise to this PublicationError.
     * @return This PublicationError.
     */
    public PublicationError setCause(Throwable cause) {
        this.cause = cause;
        return this;
    }

    public String getMessage() {
        return message;
    }

    public PublicationError setMessage(String message) {
        this.message = message;
        return this;
    }

    public Method getHandler() {
        return handler;
    }

    public PublicationError setHandler(Method handler) {
        this.handler = handler;
        return this;
    }

    public Object getListener() {
        return listener;
    }

    public PublicationError setListener(Object listener) {
        this.listener = listener;
        return this;
    }

    public Object getPublishedMessage() {
        return publishedMessage;
    }

    public PublicationError setPublishedMessage(Object publishedMessage) {
        this.publishedMessage = publishedMessage;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
    	String newLine = System.getProperty("line.separator");
        return "PublicationError{" +
                newLine +
                "\tcause=" + cause +
                newLine +
                "\tmessage='" + message + '\'' +
                newLine +
                "\thandler=" + handler +
                newLine +
                "\tlistener=" + listener +
                newLine +
                "\tpublishedMessage=" + publishedMessage +
                '}';
    }
}
