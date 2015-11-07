package net.engio.mbassy.dispatch;

import net.engio.mbassy.bus.IMessagePublication;
import net.engio.mbassy.bus.error.PublicationError;
import net.engio.mbassy.subscription.SubscriptionContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Uses reflection to invoke a message handler for a given message.
 *
 * @author bennidi
 *         Date: 11/23/12
 */
public class ReflectiveHandlerInvocation extends HandlerInvocation{

    public ReflectiveHandlerInvocation(SubscriptionContext context) {
        super(context);
    }

    protected void invokeHandler(final Object message, final Object listener, Method handler, IMessagePublication publication){
        try {
            handler.invoke(listener, message);
        } catch (IllegalAccessException e) {
            handlePublicationError(new PublicationError(e, "Error during invocation of message handler. " +
                            "The class or method is not accessible",
                            handler, listener, message, publication));
        } catch (IllegalArgumentException e) {
            handlePublicationError(new PublicationError(e, "Error during invocation of message handler. " +
                            "Wrong arguments passed to method. Was: " + message.getClass()
                            + "Expected: " + handler.getParameterTypes()[0],
                            handler, listener, message, publication));
        } catch (InvocationTargetException e) {
            handlePublicationError( new PublicationError(e, "Error during invocation of message handler. " +
                            "Message handler threw exception",
                            handler, listener, message, publication));
        } catch (Throwable e) {
            handlePublicationError( new PublicationError(e, "Error during invocation of message handler. " +
                            "The handler code threw an exception",
                            handler, listener, message, publication));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invoke(final Object listener, final Object message, IMessagePublication publication){
        invokeHandler(message, listener, getContext().getHandler().getMethod(),publication);
    }
}
