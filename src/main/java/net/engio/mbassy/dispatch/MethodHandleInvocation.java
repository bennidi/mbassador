package net.engio.mbassy.dispatch;

import net.engio.mbassy.bus.MessagePublication;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;
import net.engio.mbassy.subscription.SubscriptionContext;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Method;
import java.util.Collection;

/**
 * Uses java.lang.invoke.MethodHandle for high-performance dispatch.
 * Replaces the old Reflection-based invocation.
 */
public class MethodHandleInvocation extends HandlerInvocation {

  private final MethodHandle handle;

  public MethodHandleInvocation(SubscriptionContext context) {
    super(context);
    Method method = context.getHandler().getMethod();
    try {
      MethodHandles.Lookup lookup = MethodHandles.lookup();
      this.handle = lookup.unreflect(method);
    } catch (IllegalAccessException e) {
      throw new RuntimeException("Could not create MethodHandle for handler: " + method.getName(), e);
    }
  }

  @Override
  public void invoke(final Object listener, final Object message, MessagePublication publication) {
    try {
      handle.invoke(listener, message);
    } catch (Throwable e) {
      handleError(publication, e, listener);
    }
  }

  private void handleError(MessagePublication publication, Throwable e, Object listener) {
    PublicationError error = new PublicationError(
        e,
        "Error during invocation of message handler via MethodHandle.",
        getContext().getHandler().getMethod(),
        listener,
        publication
    );

    publication.markError(error);

    Collection<IPublicationErrorHandler> errorHandlers =
        getContext().getRuntime().get(IBusConfiguration.Properties.PublicationErrorHandlers);

    if (errorHandlers != null) {
      for (IPublicationErrorHandler handler : errorHandlers) {
        handler.handleError(error);
      }
    }
  }
}