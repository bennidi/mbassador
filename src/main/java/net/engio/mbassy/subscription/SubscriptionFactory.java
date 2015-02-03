package net.engio.mbassy.subscription;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.Collection;

import net.engio.mbassy.bus.BusRuntime;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.MessageBusException;
import net.engio.mbassy.common.WeakConcurrentSet;
import net.engio.mbassy.dispatch.HandlerInvocation;
import net.engio.mbassy.dispatch.IHandlerInvocation;
import net.engio.mbassy.dispatch.IMessageDispatcher;
import net.engio.mbassy.dispatch.MessageDispatcher;
import net.engio.mbassy.dispatch.ReflectiveHandlerInvocation;
import net.engio.mbassy.dispatch.SynchronizedHandlerInvocation;
import net.engio.mbassy.listener.MessageHandler;

/**
 * The subscription factory is used to create an empty subscription for specific message handler.
 * The message handler's configuration is evaluated and a corresponding subscription is built.
 */
public class SubscriptionFactory {

    public Subscription createSubscription(BusRuntime runtime, MessageHandler handlerMetadata) throws MessageBusException{
        try {
            Collection<IPublicationErrorHandler> errorHandlers = runtime.get(BusRuntime.Properties.ErrorHandlers);
            SubscriptionContext context = new SubscriptionContext(runtime, handlerMetadata, errorHandlers);
            IHandlerInvocation invocation = buildInvocationForHandler(context);
            IMessageDispatcher dispatcher = buildDispatcher(context, invocation);
            return new Subscription(context, dispatcher, new WeakConcurrentSet<Object>());
        } catch (Exception e) {
            throw new MessageBusException(e);
        }
    }

    protected IHandlerInvocation buildInvocationForHandler(SubscriptionContext context) throws Exception {
        IHandlerInvocation invocation = createBaseHandlerInvocation(context);
        if(context.getHandlerMetadata().isSynchronized()){
            invocation = new SynchronizedHandlerInvocation(invocation);
        }

        return invocation;
    }

    protected IMessageDispatcher buildDispatcher(SubscriptionContext context, IHandlerInvocation invocation) {
        IMessageDispatcher dispatcher = new MessageDispatcher(context, invocation);

        return dispatcher;
    }

    protected IHandlerInvocation createBaseHandlerInvocation(SubscriptionContext context) throws MessageBusException {
        Class<? extends HandlerInvocation> invocation = ReflectiveHandlerInvocation.class;
        if(invocation.isMemberClass() && !Modifier.isStatic(invocation.getModifiers())){
            throw new MessageBusException("The handler invocation must be top level class or nested STATIC inner class");
        }
        try {
            Constructor<? extends IHandlerInvocation> constructor = invocation.getConstructor(SubscriptionContext.class);
            return constructor.newInstance(context);
        } catch (NoSuchMethodException e) {
            throw new MessageBusException("The provided handler invocation did not specify the necessary constructor "
                    + invocation.getSimpleName() + "(SubscriptionContext);", e);
        } catch (Exception e) {
            throw new MessageBusException("Could not instantiate the provided handler invocation "
                    + invocation.getSimpleName(), e);
        }
    }
}
