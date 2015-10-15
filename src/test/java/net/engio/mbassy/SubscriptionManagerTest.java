package net.engio.mbassy;

import net.engio.mbassy.bus.BusRuntime;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.common.*;
import net.engio.mbassy.listener.MetadataReader;
import net.engio.mbassy.listeners.*;
import net.engio.mbassy.messages.*;
import net.engio.mbassy.subscription.Subscription;
import net.engio.mbassy.subscription.SubscriptionFactory;
import net.engio.mbassy.subscription.SubscriptionManager;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

/**
 * Test the subscriptions as generated and organized by the subscription manager. Tests use different sets of listeners
 * and corresponding expected set of subscriptions that should result from subscribing the listeners. The subscriptions
 * are tested for the type of messages they should handle and
 *
 * @author bennidi
 *         Date: 5/12/13
 */
public class SubscriptionManagerTest extends AssertSupport {

    private static final int InstancesPerListener = 5000;
    private static final int ConcurrentUnits = 10;

    @Test
    public void testIMessageListener() {
        ListenerFactory listeners = listeners(
                IMessageListener.DefaultListener.class,
                IMessageListener.AsyncListener.class,
                IMessageListener.DisabledListener.class,
                IMessageListener.NoSubtypesListener.class);

        SubscriptionValidator expectedSubscriptions = new SubscriptionValidator(listeners)
                .listener(IMessageListener.DefaultListener.class).handles(IMessage.class,
                        AbstractMessage.class, IMultipartMessage.class, StandardMessage.class, MessageTypes.class)
                .listener(IMessageListener.AsyncListener.class).handles(IMessage.class,
                        AbstractMessage.class, IMultipartMessage.class, StandardMessage.class, MessageTypes.class)
                .listener(IMessageListener.NoSubtypesListener.class).handles(IMessage.class);

        runTestWith(listeners, expectedSubscriptions);
    }

    @Test
    public void testAbstractMessageListener() {
        ListenerFactory listeners = listeners(
                AbstractMessageListener.DefaultListener.class,
                AbstractMessageListener.AsyncListener.class,
                AbstractMessageListener.DisabledListener.class,
                AbstractMessageListener.NoSubtypesListener.class);

        SubscriptionValidator expectedSubscriptions = new SubscriptionValidator(listeners)
                .listener(AbstractMessageListener.NoSubtypesListener.class).handles(AbstractMessage.class)
                .listener(AbstractMessageListener.DefaultListener.class).handles(StandardMessage.class, AbstractMessage.class)
                .listener(AbstractMessageListener.AsyncListener.class).handles(StandardMessage.class, AbstractMessage.class);

        runTestWith(listeners, expectedSubscriptions);
    }

    @Test
    public void testMessagesListener() {
        ListenerFactory listeners = listeners(
                MessagesTypeListener.DefaultListener.class,
                MessagesTypeListener.AsyncListener.class,
                MessagesTypeListener.DisabledListener.class,
                MessagesTypeListener.NoSubtypesListener.class);

        SubscriptionValidator expectedSubscriptions = new SubscriptionValidator(listeners)
                .listener(MessagesTypeListener.NoSubtypesListener.class).handles(MessageTypes.class)
                .listener(MessagesTypeListener.DefaultListener.class).handles(MessageTypes.class)
                .listener(MessagesTypeListener.AsyncListener.class).handles(MessageTypes.class);

        runTestWith(listeners, expectedSubscriptions);
    }

    @Test
    public void testMultipartMessageListener() {
        ListenerFactory listeners = listeners(
                MultipartMessageListener.DefaultListener.class,
                MultipartMessageListener.AsyncListener.class,
                MultipartMessageListener.DisabledListener.class,
                MultipartMessageListener.NoSubtypesListener.class);

        SubscriptionValidator expectedSubscriptions = new SubscriptionValidator(listeners)
                .listener(MultipartMessageListener.NoSubtypesListener.class).handles(MultipartMessage.class)
                .listener(MultipartMessageListener.DefaultListener.class).handles(MultipartMessage.class)
                .listener(MultipartMessageListener.AsyncListener.class).handles(MultipartMessage.class);

        runTestWith(listeners, expectedSubscriptions);
    }

    @Test
    public void testIMultipartMessageListener() {
        ListenerFactory listeners = listeners(
                IMultipartMessageListener.DefaultListener.class,
                IMultipartMessageListener.AsyncListener.class,
                IMultipartMessageListener.DisabledListener.class,
                IMultipartMessageListener.NoSubtypesListener.class);

        SubscriptionValidator expectedSubscriptions = new SubscriptionValidator(listeners)
                .listener(IMultipartMessageListener.NoSubtypesListener.class).handles(IMultipartMessage.class)
                .listener(IMultipartMessageListener.DefaultListener.class).handles(MultipartMessage.class, IMultipartMessage.class)
                .listener(IMultipartMessageListener.AsyncListener.class).handles(MultipartMessage.class, IMultipartMessage.class);

        runTestWith(listeners, expectedSubscriptions);
    }

    @Test
    public void testStandardMessageListener() {
        ListenerFactory listeners = listeners(
                StandardMessageListener.DefaultListener.class,
                StandardMessageListener.AsyncListener.class,
                StandardMessageListener.DisabledListener.class,
                StandardMessageListener.NoSubtypesListener.class);

        SubscriptionValidator expectedSubscriptions = new SubscriptionValidator(listeners)
                .listener(StandardMessageListener.NoSubtypesListener.class).handles(StandardMessage.class)
                .listener(StandardMessageListener.DefaultListener.class).handles(StandardMessage.class)
                .listener(StandardMessageListener.AsyncListener.class).handles(StandardMessage.class);

        runTestWith(listeners, expectedSubscriptions);
    }

    @Test
    public void testICountableListener() {
        ListenerFactory listeners = listeners(
                ICountableListener.DefaultListener.class,
                ICountableListener.AsyncListener.class,
                ICountableListener.DisabledListener.class,
                ICountableListener.NoSubtypesListener.class);

        SubscriptionValidator expectedSubscriptions = new SubscriptionValidator(listeners)
                .listener(ICountableListener.DefaultListener.class).handles(ICountable.class)
                .listener(ICountableListener.DefaultListener.class).handles(MultipartMessage.class, IMultipartMessage.class, ICountable.class, StandardMessage.class)
                .listener(ICountableListener.AsyncListener.class).handles(MultipartMessage.class, IMultipartMessage.class, ICountable.class, StandardMessage.class);

        runTestWith(listeners, expectedSubscriptions);
    }

    @Test
    public void testMultipleMessageListeners() {
        ListenerFactory listeners = listeners(
                ICountableListener.DefaultListener.class,
                ICountableListener.AsyncListener.class,
                ICountableListener.DisabledListener.class,
                IMultipartMessageListener.DefaultListener.class,
                IMultipartMessageListener.AsyncListener.class,
                IMultipartMessageListener.DisabledListener.class,
                MessagesTypeListener.DefaultListener.class,
                MessagesTypeListener.AsyncListener.class,
                MessagesTypeListener.DisabledListener.class);

        SubscriptionValidator expectedSubscriptions = new SubscriptionValidator(listeners)
                .listener(ICountableListener.DefaultListener.class)
                .handles(MultipartMessage.class, IMultipartMessage.class, ICountable.class, StandardMessage.class)
                .listener(ICountableListener.AsyncListener.class)
                .handles(MultipartMessage.class, IMultipartMessage.class, ICountable.class, StandardMessage.class)
                .listener(IMultipartMessageListener.DefaultListener.class).handles(MultipartMessage.class, IMultipartMessage.class)
                .listener(IMultipartMessageListener.AsyncListener.class).handles(MultipartMessage.class, IMultipartMessage.class)
                .listener(MessagesTypeListener.DefaultListener.class).handles(MessageTypes.class)
                .listener(MessagesTypeListener.AsyncListener.class).handles(MessageTypes.class);

        runTestWith(listeners, expectedSubscriptions);
    }

    @Test
    public void testStrongListenerSubscription() throws Exception {
        ListenerFactory listeners = listeners(CustomInvocationListener.class);
        SubscriptionManager subscriptionManager = new SubscriptionManager(new MetadataReader(), new SubscriptionFactory(), mockedRuntime());
        ConcurrentExecutor.runConcurrent(TestUtil.subscriber(subscriptionManager, listeners), ConcurrentUnits);

        listeners.clear();
        runGC();

        Collection<Subscription> subscriptions = subscriptionManager.getSubscriptionsByMessageType(StandardMessage.class);
        assertEquals(1, subscriptions.size());
        for (Subscription sub : subscriptions)
            assertEquals(InstancesPerListener, sub.size());
    }

    @Test
    public void testOverloadedMessageHandlers() {
        ListenerFactory listeners = listeners(
                Overloading.ListenerBase.class,
                Overloading.ListenerSub.class);

        SubscriptionManager subscriptionManager = new SubscriptionManager(new MetadataReader(), new SubscriptionFactory(), mockedRuntime());
        ConcurrentExecutor.runConcurrent(TestUtil.subscriber(subscriptionManager, listeners), ConcurrentUnits);

        SubscriptionValidator expectedSubscriptions = new SubscriptionValidator(listeners)
                .listener(Overloading.ListenerBase.class).handles(Overloading.TestMessageA.class, Overloading.TestMessageA.class)
                .listener(Overloading.ListenerSub.class).handles(Overloading.TestMessageA.class, Overloading.TestMessageA.class, Overloading.TestMessageB.class);

        runTestWith(listeners, expectedSubscriptions);
    }

    @Test
    public void testPrioritizedMessageHandlers() {
        ListenerFactory listeners = listeners(PrioritizedListener.class);

        SubscriptionManager subscriptionManager = new SubscriptionManager(new MetadataReader(), new SubscriptionFactory(), mockedRuntime());
        ConcurrentExecutor.runConcurrent(TestUtil.subscriber(subscriptionManager, listeners), ConcurrentUnits);

        SubscriptionValidator expectedSubscriptions = new SubscriptionValidator(listeners)
                .listener(PrioritizedListener.class).handles(IMessage.class, IMessage.class, IMessage.class, IMessage.class);

        runTestWith(listeners, expectedSubscriptions);
    }

    private BusRuntime mockedRuntime() {
        return new BusRuntime(null)
                .add(IBusConfiguration.Properties.PublicationErrorHandlers, Collections.EMPTY_SET)
                .add(IBusConfiguration.Properties.AsynchronousHandlerExecutor, null);
    }

    private ListenerFactory listeners(Class... listeners) {
        ListenerFactory factory = new ListenerFactory();
        for (Class listener : listeners) {
            factory.create(InstancesPerListener, listener);
        }
        return factory;
    }

    private void runTestWith(final ListenerFactory listeners, final SubscriptionValidator validator) {
        final SubscriptionManager subscriptionManager = new SubscriptionManager(new MetadataReader(), new SubscriptionFactory(), mockedRuntime());

        ConcurrentExecutor.runConcurrent(TestUtil.subscriber(subscriptionManager, listeners), ConcurrentUnits);

        validator.validate(subscriptionManager);

        ConcurrentExecutor.runConcurrent(TestUtil.unsubscriber(subscriptionManager, listeners), ConcurrentUnits);

        listeners.clear();

        validator.validate(subscriptionManager);
    }


    /**
     * define handlers with different priorities which need to be executed
     * in their respective order
     */
    public static class PrioritizedListener {


        @net.engio.mbassy.listener.Handler(priority = 1)
        public void handlePrio1(IMessage message) {
            message.handled(this.getClass());
        }

        @net.engio.mbassy.listener.Handler(priority = 2)
        public void handlePrio2(IMessage message) {
            message.handled(this.getClass());
        }

        @net.engio.mbassy.listener.Handler(priority = 3)
        public void handlePrio3(IMessage message) {
            message.handled(this.getClass());
        }

        @net.engio.mbassy.listener.Handler(priority = 4)
        public void handlePrio4(IMessage message) {
            message.handled(this.getClass());
        }
    }


}
