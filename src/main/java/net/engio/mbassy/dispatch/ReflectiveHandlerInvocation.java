package net.engio.mbassy.dispatch;

import net.engio.mbassy.IPublicationErrorHandler;
import net.engio.mbassy.PublicationError;
import net.engio.mbassy.subscription.AbstractSubscriptionContextAware;
import net.engio.mbassy.subscription.SubscriptionContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Uses reflection to invoke a message handler for a given message.
 *
 * @author bennidi
 *         Date: 11/23/12
 */
public class ReflectiveHandlerInvocation extends AbstractSubscriptionContextAware implements IHandlerInvocation {

    public ReflectiveHandlerInvocation(SubscriptionContext context) {
        super(context);
    }

    protected void handlePublicationError(PublicationError error) {
        Collection<IPublicationErrorHandler> handlers = getContext().getOwningBus().getRegisteredErrorHandlers();
        for (IPublicationErrorHandler handler : handlers) {
            handler.handleError(error);
        }
    }

    protected void invokeHandler(final Object message, final Object listener, Method handler) {
        try {
            handler.invoke(listener, message);
        } catch (IllegalAccessException e) {
            handlePublicationError(
                    new PublicationError(e, "Error during messageHandler notification. " +
                            "The class or method is not accessible",
                            handler, listener, message));
        } catch (IllegalArgumentException e) {
            handlePublicationError(
                    new PublicationError(e, "Error during messageHandler notification. " +
                            "Wrong arguments passed to method. Was: " + message.getClass()
                            + "Expected: " + handler.getParameterTypes()[0],
                            handler, listener, message));
        } catch (InvocationTargetException e) {
            handlePublicationError(
                    new PublicationError(e, "Error during messageHandler notification. " +
                            "Message handler threw exception",
                            handler, listener, message));
        } catch (Throwable e) {
            handlePublicationError(
                    new PublicationError(e, "Error during messageHandler notification. " +
                            "Unexpected exception",
                            handler, listener, message));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invoke(final Object listener, final Object message) {
        invokeHandler(message, listener, getContext().getHandlerMetadata().getHandler());
    }
}
