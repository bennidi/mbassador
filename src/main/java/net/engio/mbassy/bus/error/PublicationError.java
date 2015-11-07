package net.engio.mbassy.bus.error;

import net.engio.mbassy.bus.IMessagePublication;
import net.engio.mbassy.subscription.SubscriptionContext;

import java.lang.reflect.Method;

/**
 * Publication errors are used to communicate exceptions that occur during message publication.
 * The most common reason is most likely an exception thrown during the execution of a message handler.
 *
 * The publication error contains details about to the cause and location where error occurred.
 * They are passed to all registered instances of {@link IPublicationErrorHandler} configured within
 * the {@link net.engio.mbassy.bus.config.IBusConfiguration}
 *
 * @author bennidi
 *         Date: 2/22/12
 *         Time: 4:59 PM
 */
public class PublicationError{

    // Internal state
    private final Throwable cause;
    private final String message;
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
                            final Object publishedObject,
                            IMessagePublication publication) {

        this.cause = cause;
        this.message = message;
        this.handler = handler;
        this.listener = listener;
        this.publishedMessage = publishedObject;
        if(publication!=null)
        {
            publication.addError(cause);
        }
    }

    public PublicationError(final Throwable cause,
                            final String message,
                            final IMessagePublication publication) {
        this.cause = cause;
        this.message = message;
        if (publication != null)
        {
            publication.addError(cause);
            this.publishedMessage = publication.getMessage();
        }
    }

    public PublicationError(final Throwable cause,
                            final String message,
                            final SubscriptionContext context, Object publishedMessage, IMessagePublication publication)
    {
        this.cause = cause;
        this.message = message;
        this.handler = context.getHandler().getMethod();
        this.publishedMessage = publishedMessage;
        if (publication != null)
        {
            publication.addError(cause);
        }
    }


    public PublicationError(Throwable cause, String message, Object publishedMessage, IMessagePublication publication) {
        this.cause = cause;
        this.message = message;
        this.publishedMessage = publishedMessage;
        if (publication != null)
        {
            publication.addError(cause);
        }
    }

    /**
     * @return The Throwable giving rise to this PublicationError.
     */
    public Throwable getCause() {
        return cause;
    }

    public String getMessage() {
        return message;
    }

    public Method getHandler() {
        return handler;
    }

    public Object getListener() {
        return listener;
    }

    public Object getPublishedMessage() {
        return publishedMessage;
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
