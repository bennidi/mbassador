package net.engio.mbassy;

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
public class PublicationError {

    // Internal state
    private Throwable cause;
    private String message;
    private Method listener;
    private Object listeningObject;
    private Object publishedObject;

    /**
     * Compound constructor, creating a PublicationError from the supplied objects.
     *
     * @param cause           The Throwable giving rise to this PublicationError.
     * @param message         The message to send.
     * @param listener        The method where the error was created.
     * @param listeningObject The object in which the PublicationError was generated.
     * @param publishedObject The published object which gave rise to the error.
     */
    public PublicationError(final Throwable cause,
                            final String message,
                            final Method listener,
                            final Object listeningObject,
                            final Object publishedObject) {

        this.cause = cause;
        this.message = message;
        this.listener = listener;
        this.listeningObject = listeningObject;
        this.publishedObject = publishedObject;
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

    public Method getListener() {
        return listener;
    }

    public PublicationError setListener(Method listener) {
        this.listener = listener;
        return this;
    }

    public Object getListeningObject() {
        return listeningObject;
    }

    public PublicationError setListeningObject(Object listeningObject) {
        this.listeningObject = listeningObject;
        return this;
    }

    public Object getPublishedObject() {
        return publishedObject;
    }

    public PublicationError setPublishedObject(Object publishedObject) {
        this.publishedObject = publishedObject;
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
                "\tlistener=" + listener +
                newLine +
                "\tlisteningObject=" + listeningObject +
                newLine +
                "\tpublishedObject=" + publishedObject +
                '}';
    }
}
