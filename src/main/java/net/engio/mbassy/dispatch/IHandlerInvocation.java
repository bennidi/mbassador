package net.engio.mbassy.dispatch;

import com.esotericsoftware.reflectasm.MethodAccess;

/**
 * A handler invocation encapsulates the logic that is used to invoke a single
 * message handler to process a given message.
 *
 * A handler invocation might come in different flavours and can be composed
 * of various independent invocations by means of delegation (-> decorator pattern)
 *
 * If an exception is thrown during handler invocation it is wrapped and propagated
 * as a publication error
 *
 * @author bennidi
 *         Date: 11/23/12
 * @author dorkbox, llc
 *         Date: 2/2/15
 */
public interface IHandlerInvocation {

    /**
     * Invoke the message delivery logic of this handler
     *
     * @param listener The listener that will receive the message. This can be a reference to a method object
     *                 from the java reflection api or any other wrapper that can be used to invoke the handler
     * @param message  The message to be delivered to the handler. This can be any object compatible with the object
     *                 type that the handler consumes
     * @param handler  The handler (method) that will be called via reflection
     */
    void invoke(Object listener, MethodAccess handler, int methodIndex, Object message) throws Throwable;

    /**
     * Invoke the message delivery logic of this handler
     *
     * @param listener The listener that will receive the message. This can be a reference to a method object
     *                 from the java reflection api or any other wrapper that can be used to invoke the handler
     * @param message  The message to be delivered to the handler. This can be any object compatible with the object
     *                 type that the handler consumes
     * @param handler  The handler (method) that will be called via reflection
     */
    void invoke(Object listener, MethodAccess handler, int methodIndex, Object message1, Object message2) throws Throwable;

    /**
     * Invoke the message delivery logic of this handler
     *
     * @param listener The listener that will receive the message. This can be a reference to a method object
     *                 from the java reflection api or any other wrapper that can be used to invoke the handler
     * @param message  The message to be delivered to the handler. This can be any object compatible with the object
     *                 type that the handler consumes
     * @param handler  The handler (method) that will be called via reflection
     */
    void invoke(Object listener, MethodAccess handler, int methodIndex, Object message1, Object message2, Object message3) throws Throwable;

    /**
     * Invoke the message delivery logic of this handler
     *
     * @param listener The listener that will receive the message. This can be a reference to a method object
     *                 from the java reflection api or any other wrapper that can be used to invoke the handler
     * @param message  The message to be delivered to the handler. This can be any object compatible with the object
     *                 type that the handler consumes
     * @param handler  The handler (method) that will be called via reflection
     */
    void invoke(Object listener, MethodAccess handler, int methodIndex, Object... message) throws Throwable;
}
