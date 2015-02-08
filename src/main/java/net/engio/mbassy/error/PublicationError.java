package net.engio.mbassy.error;

import java.util.Arrays;

/**
 * Publication errors are created when object publication fails
 * for some reason and contain details as to the cause and location
 * where they occurred.
 * <p/>
 *
 * @author bennidi
 *         Date: 2/22/12
 *         Time: 4:59 PM
 * @author dorkbox, llc
 *         Date: 2/2/15
 */
public class PublicationError {

    // Internal state
    private Throwable cause;
    private String message;
    private String methodName;
    private Object listener;
    private Object[] publishedObjects;


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
        return this.cause;
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
        return this.message;
    }

    public PublicationError setMessage(String message) {
        this.message = message;
        return this;
    }

    public String getMethodName() {
        return this.methodName;
    }

    public PublicationError setMethodName(String methodName) {
        this.methodName = methodName;
        return this;
    }

    public Object getListener() {
        return this.listener;
    }

    public PublicationError setListener(Object listener) {
        this.listener = listener;
        return this;
    }

    public Object[] getPublishedObject() {
        return this.publishedObjects;
    }

    public PublicationError setPublishedObject(Object publishedObject) {
        this.publishedObjects = new Object[1];
        this.publishedObjects[0] = publishedObject;

        return this;
    }

    public PublicationError setPublishedObject(Object publishedObject1, Object publishedObject2) {
        this.publishedObjects = new Object[2];
        this.publishedObjects[0] = publishedObject1;
        this.publishedObjects[1] = publishedObject2;

        return this;
    }

    public PublicationError setPublishedObject(Object publishedObject1, Object publishedObject2, Object publishedObject3) {
        this.publishedObjects = new Object[3];
        this.publishedObjects[0] = publishedObject1;
        this.publishedObjects[1] = publishedObject2;
        this.publishedObjects[2] = publishedObject3;

        return this;
    }

    public PublicationError setPublishedObject(Object[] publishedObjects) {
        this.publishedObjects = publishedObjects;
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
                "\tcause=" + this.cause +
                newLine +
                "\tmessage='" + this.message + '\'' +
                newLine +
                "\tmethod=" + this.methodName +
                newLine +
                "\tlistener=" + this.listener +
                newLine +
                "\tpublishedObject=" + Arrays.deepToString(this.publishedObjects) +
                '}';
    }
}
