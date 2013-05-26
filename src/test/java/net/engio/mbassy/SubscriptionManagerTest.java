package net.engio.mbassy;

import net.engio.mbassy.common.AssertSupport;
import net.engio.mbassy.common.ConcurrentExecutor;
import net.engio.mbassy.common.SubscriptionValidator;
import net.engio.mbassy.common.TestUtil;
import net.engio.mbassy.listener.MetadataReader;
import net.engio.mbassy.listeners.*;
import net.engio.mbassy.messages.*;
import net.engio.mbassy.subscription.Subscription;
import net.engio.mbassy.subscription.SubscriptionFactory;
import net.engio.mbassy.subscription.SubscriptionManager;
import org.junit.Test;

import java.util.Collection;

/**
 * @author bennidi
 *         Date: 5/12/13
 */
public class SubscriptionManagerTest extends AssertSupport {

    private static final int InstancesPerListener = 5000;
    private static final int ConcurrentUnits = 10;

    @Test
    public void testIMessageListener(){
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
    public void testAbstractMessageListener(){
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
    public void testMessagesListener(){
        ListenerFactory listeners = listeners(
                MessagesListener.DefaultListener.class,
                MessagesListener.AsyncListener.class,
                MessagesListener.DisabledListener.class,
                MessagesListener.NoSubtypesListener.class);

        SubscriptionValidator expectedSubscriptions = new SubscriptionValidator(listeners)
                .listener(MessagesListener.NoSubtypesListener.class).handles(MessageTypes.class)
                .listener(MessagesListener.DefaultListener.class).handles(MessageTypes.class)
                .listener(MessagesListener.AsyncListener.class).handles(MessageTypes.class);

        runTestWith(listeners, expectedSubscriptions);
    }

    @Test
    public void testMultipartMessageListener(){
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
    public void testIMultipartMessageListener(){
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
    public void testStandardMessageListener(){
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
    public void testICountableListener(){
        ListenerFactory listeners = listeners(
                ICountableListener.DefaultListener.class,
                ICountableListener.AsyncListener.class,
                ICountableListener.DisabledListener.class,
                ICountableListener.NoSubtypesListener.class);

        SubscriptionValidator expectedSubscriptions = new SubscriptionValidator(listeners)
                .listener(ICountableListener.DefaultListener.class).handles(ICountable.class)
                .listener(ICountableListener.DefaultListener.class).handles(MultipartMessage.class, IMultipartMessage.class, ICountable.class, StandardMessage.class)
                .listener(ICountableListener.AsyncListener.class).handles(MultipartMessage.class, IMultipartMessage.class, ICountable.class,  StandardMessage.class);

        runTestWith(listeners, expectedSubscriptions);
    }

    @Test
    public void testMultipleMessageListeners(){
        ListenerFactory listeners = listeners(
                ICountableListener.DefaultListener.class,
                ICountableListener.AsyncListener.class,
                ICountableListener.DisabledListener.class,
                IMultipartMessageListener.DefaultListener.class,
                IMultipartMessageListener.AsyncListener.class,
                IMultipartMessageListener.DisabledListener.class,
                MessagesListener.DefaultListener.class,
                MessagesListener.AsyncListener.class,
                MessagesListener.DisabledListener.class);

        SubscriptionValidator expectedSubscriptions = new SubscriptionValidator(listeners)
                .listener(ICountableListener.DefaultListener.class)
                .handles(MultipartMessage.class, IMultipartMessage.class, ICountable.class, StandardMessage.class)
                .listener(ICountableListener.AsyncListener.class)
                .handles(MultipartMessage.class, IMultipartMessage.class, ICountable.class,  StandardMessage.class)
                .listener(IMultipartMessageListener.DefaultListener.class).handles(MultipartMessage.class, IMultipartMessage.class)
                .listener(IMultipartMessageListener.AsyncListener.class).handles(MultipartMessage.class, IMultipartMessage.class)
                .listener(MessagesListener.DefaultListener.class).handles(MessageTypes.class)
                .listener(MessagesListener.AsyncListener.class).handles(MessageTypes.class);

        runTestWith(listeners, expectedSubscriptions);
    }

    @Test
    public void testStrongListenerSubscription() throws Exception {
        ListenerFactory listeners = listeners(CustomInvocationListener.class);
        SubscriptionManager subscriptionManager = new SubscriptionManager(new MetadataReader(), new SubscriptionFactory());
        ConcurrentExecutor.runConcurrent(TestUtil.subscriber(subscriptionManager, listeners), ConcurrentUnits);


        listeners.clear();
        runGC();

        Collection<Subscription> subscriptions = subscriptionManager.getSubscriptionsByMessageType(StandardMessage.class);
        assertEquals(1, subscriptions.size());
        for(Subscription sub : subscriptions)
            assertEquals(InstancesPerListener,  sub.size());
    }


    private ListenerFactory listeners(Class ...listeners){
        ListenerFactory factory = new ListenerFactory();
        for(Class listener : listeners){
            factory.create(InstancesPerListener, listener);
        }
        return factory;
    }

    private void runTestWith(final ListenerFactory listeners, final SubscriptionValidator validator){
        final SubscriptionManager subscriptionManager = new SubscriptionManager(new MetadataReader(), new SubscriptionFactory());

        ConcurrentExecutor.runConcurrent(TestUtil.subscriber(subscriptionManager, listeners), ConcurrentUnits);

        validator.validate(subscriptionManager);

        ConcurrentExecutor.runConcurrent(TestUtil.unsubscriber(subscriptionManager, listeners), ConcurrentUnits);

        listeners.clear();

        validator.validate(subscriptionManager);
    }





}
