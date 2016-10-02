package net.engio.mbassy.dispatch;

import net.engio.mbassy.bus.MessagePublication;
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void invoke(final Object listener, final Object message, MessagePublication publication){
        final Method handler = getContext().getHandler().getMethod();
        try {
            handler.invoke(listener, message);
        } catch (IllegalAccessException e) {
            handlePublicationError(publication, new PublicationError(e, "Error during invocation of message handler. " +
                    "The class or method is not accessible",
                    handler, listener, publication));
        } catch (IllegalArgumentException e) {
            handlePublicationError(publication, new PublicationError(e, "Error during invocation of message handler. " +
                    "Wrong arguments passed to method. Was: " + message.getClass()
                    + "Expected: " + handler.getParameterTypes()[0],
                    handler, listener, publication));
        } catch (InvocationTargetException e) {
            handlePublicationError(publication, new PublicationError(e, "Error during invocation of message handler. " +
                    "There might be an access rights problem. Do you use non public inner classes?",
                    handler, listener, publication));
        } catch (Throwable e) {
            handlePublicationError(publication, new PublicationError(e, "Error during invocation of message handler. " +
                    "The handler code threw an exception",
                    handler, listener, publication));
        }
    }
}
