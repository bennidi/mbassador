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
    private Throwable cause;
    private String errorMsg;
    private Method handler;
    private Object listener;
    private IMessagePublication publication;
    private Object message;



    /**
     * Compound constructor, creating a PublicationError from the supplied objects.
     *
     * @param cause           The Throwable giving rise to this PublicationError.
     * @param errorMsg         The message to send.
     * @param handler        The method where the error was created.
     * @param listener The object in which the PublicationError was generated.
     * @param publication The publication that errored
     */
    public PublicationError(final Throwable cause,
                            final String errorMsg,
                            final Method handler,
                            final Object listener,
                            final IMessagePublication publication) {

        this.cause = cause;
        this.errorMsg = errorMsg;
        this.handler = handler;
        this.listener = listener;
        this.publication = publication;
        this.message = publication != null ? publication.getMessage() : null;
    }

    public PublicationError(final Throwable cause,
                            final String errorMsg,
                            final IMessagePublication publication) {
        this.cause = cause;
        this.errorMsg = errorMsg;
    }

    public PublicationError(final Throwable cause,
                            final String errorMsg,
                            final SubscriptionContext context) {
        this.cause = cause;
        this.errorMsg = errorMsg;
        this.handler = context.getHandler().getMethod();
    }

    public PublicationError(Throwable cause, String errorMsg) {
        this.cause = cause;
        this.errorMsg = errorMsg;
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
        return errorMsg;
    }

    public PublicationError setMessage(String message) {
        this.errorMsg = message;
        return this;
    }

    public PublicationError setPublishedMessage(Object message) {
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
        return message;
    }

    public PublicationError setPublication(IMessagePublication publication) {
        this.publication = publication;
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
                "\tmessage='" + errorMsg + '\'' +
                newLine +
                "\thandler=" + handler +
                newLine +
                "\tlistener=" + listener +
                newLine +
                "\tpublishedMessage=" + getPublishedMessage() +
                '}';
    }
}
