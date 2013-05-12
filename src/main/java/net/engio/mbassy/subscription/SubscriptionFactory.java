package net.engio.mbassy.subscription;

import net.engio.mbassy.MessageBusException;
import net.engio.mbassy.bus.ISyncMessageBus;
import net.engio.mbassy.common.StrongConcurrentSet;
import net.engio.mbassy.common.WeakConcurrentSet;
import net.engio.mbassy.dispatch.*;
import net.engio.mbassy.listener.MessageHandlerMetadata;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

/**
 * The subscription factory is used to create an empty subscription for specific message handler.
 * The message handler's configuration is evaluated and a corresponding subscription is built.
 */
public class SubscriptionFactory {

    private ISyncMessageBus bus;

    public SubscriptionFactory setBus(ISyncMessageBus bus) {
        this.bus = bus;
        return this;
    }

    public Subscription createSubscription(MessageHandlerMetadata handlerMetadata) throws MessageBusException{
        try {
            SubscriptionContext context = new SubscriptionContext(bus, handlerMetadata);
            IHandlerInvocation invocation = buildInvocationForHandler(context);
            IMessageDispatcher dispatcher = buildDispatcher(context, invocation);
            return new Subscription(context, dispatcher, context.getHandlerMetadata().useStrongReferences()
                ? new StrongConcurrentSet<Object>()
                : new WeakConcurrentSet<Object>());
        } catch (Exception e) {
            throw new MessageBusException(e);
        }
    }

    protected IHandlerInvocation buildInvocationForHandler(SubscriptionContext context) throws Exception {
        IHandlerInvocation invocation = createBaseHandlerInvocation(context);
        if(context.getHandlerMetadata().isSynchronized()){
            invocation = new SynchronizedHandlerInvocation(invocation);
        }
        if (context.getHandlerMetadata().isAsynchronous()) {
            invocation = new AsynchronousHandlerInvocation(invocation);
        }
        return invocation;
    }

    protected IMessageDispatcher buildDispatcher(SubscriptionContext context, IHandlerInvocation invocation) {
        IMessageDispatcher dispatcher = new MessageDispatcher(context, invocation);
        if (context.getHandlerMetadata().isEnveloped()) {
            dispatcher = new EnvelopedMessageDispatcher(dispatcher);
        }
        if (context.getHandlerMetadata().isFiltered()) {
            dispatcher = new FilteredMessageDispatcher(dispatcher);
        }
        return dispatcher;
    }

    protected IHandlerInvocation createBaseHandlerInvocation(SubscriptionContext context) throws Exception {
        Class<? extends HandlerInvocation> invocation = context.getHandlerMetadata().getHandlerInvocation();
        if(invocation.isMemberClass() && !Modifier.isStatic(invocation.getModifiers())){
            throw new MessageBusException("The handler invocation must be top level class or nested STATIC inner class");
        }
        Constructor<? extends IHandlerInvocation> constructor = invocation.getConstructor(SubscriptionContext.class);
        return constructor.newInstance(context);
    }
}
