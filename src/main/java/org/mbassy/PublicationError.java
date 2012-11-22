package org.mbassy;

import java.lang.reflect.Method;

/**
 * Publication errors are created when object publication fails for some reason and contain details
 * as to the cause and location where they occured.
 * <p/>
 * @author bennidi
 * Date: 2/22/12
 * Time: 4:59 PM
 */
public class PublicationError {

	private Throwable cause;

	private String message;

	private Method listener;

	private Object listeningObject;

	private Object publishedObject;


	public PublicationError(Throwable cause, String message, Method listener, Object listeningObject, Object publishedObject) {
		this.cause = cause;
		this.message = message;
		this.listener = listener;
		this.listeningObject = listeningObject;
		this.publishedObject = publishedObject;
	}

	public PublicationError(){
		super();
	}

	public Throwable getCause() {
		return cause;
	}

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

    @Override
    public String toString() {
        return "PublicationError{" +
                "\n" +
                "\tcause=" + cause +
                "\n" +
                "\tmessage='" + message + '\'' +
                "\n" +
                "\tlistener=" + listener +
                "\n" +
                "\tlisteningObject=" + listeningObject +
                "\n" +
                "\tpublishedObject=" + publishedObject +
                '}';
    }
}
