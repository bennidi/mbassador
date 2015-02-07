package net.engio.mbassy.bus;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.engio.mbassy.PubSubSupport;
import net.engio.mbassy.bus.error.ErrorHandlingSupport;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;
import net.engio.mbassy.subscription.Subscription;
import net.engio.mbassy.subscription.SubscriptionManager;

/**
 * The base class for all message bus implementations.
 * @author bennidi
 * @author dorkbox, llc
 *         Date: 2/2/15
 */
public abstract class AbstractPubSubSupport implements PubSubSupport, ErrorHandlingSupport {

    // error handling is first-class functionality
    // this handler will receive all errors that occur during message dispatch or message handling
    private final List<IPublicationErrorHandler> errorHandlers = new ArrayList<IPublicationErrorHandler>();

    private final SubscriptionManager subscriptionManager;


    public AbstractPubSubSupport() {
        this.subscriptionManager = new SubscriptionManager();
    }

    @Override
    public final void handlePublicationError(PublicationError error) {
        for (IPublicationErrorHandler errorHandler : this.errorHandlers) {
            errorHandler.handleError(error);
        }
    }

    @Override
    public boolean unsubscribe(Object listener) {
        return this.subscriptionManager.unsubscribe(listener);
    }


    @Override
    public void subscribe(Object listener) {
        this.subscriptionManager.subscribe(listener);
    }


    @Override
    public final void addErrorHandler(IPublicationErrorHandler handler) {
        synchronized (this.errorHandlers) {
            this.errorHandlers.add(handler);
        }
    }

    public void publishMessage(Object message) {
        Class<?> messageClass = message.getClass();

        SubscriptionManager manager = this.subscriptionManager;
        Collection<Subscription> subscriptions = manager.getSubscriptionsByMessageType(messageClass);

        if (subscriptions == null || subscriptions.isEmpty()) {
            // Dead Event
            subscriptions = manager.getSubscriptionsByMessageType(DeadMessage.class);
            DeadMessage deadMessage = new DeadMessage(new Object[] {message});

            for (Subscription sub : subscriptions) {
                sub.publishToSubscription(this, deadMessage);
            }
        } else {
            for (Subscription sub : subscriptions) {
                Object msg = message;
                if (sub.isVarArg()) {
                    if (!message.getClass().isArray()) {
                        // messy, but the ONLY way to do it.
                        Object[] vararg = (Object[]) Array.newInstance(message.getClass(), 1);
                        vararg[0] = message;
                        msg = vararg;
                    }
                }
                sub.publishToSubscription(this, msg);
            }

            // if the message did not have any listener/handler accept it
            if (subscriptions.isEmpty()) {
                if (!DeadMessage.class.equals(messageClass.getClass())) {
                    // Dead Event
                    subscriptions = manager.getSubscriptionsByMessageType(DeadMessage.class);
                    DeadMessage deadMessage = new DeadMessage(new Object[] {message});

                    for (Subscription sub : subscriptions) {
                        sub.publishToSubscription(this, deadMessage);
                    }
                }
            }
        }
    }

    // cannot have DeadMessage published to this!
    public void publishMessage(Object message1, Object message2) {
        Class<?> messageClass1 = message1.getClass();
        Class<?> messageClass2 = message2.getClass();

        SubscriptionManager manager = this.subscriptionManager;
        Collection<Subscription> subscriptions = manager.getSubscriptionsByMessageType(messageClass1, messageClass2);

        if (subscriptions == null || subscriptions.isEmpty()) {
            // Dead Event
            subscriptions = manager.getSubscriptionsByMessageType(DeadMessage.class);
            DeadMessage deadMessage = new DeadMessage(new Object[] {message1, message2});

            for (Subscription sub : subscriptions) {
                sub.publishToSubscription(this, deadMessage);
            }
        } else {
            for (Subscription sub : subscriptions) {
                boolean handled = false;
                if (sub.isVarArg()) {
                    Class<?> class1 = message1.getClass();
                    Class<?> class2 = message2.getClass();
                    if (!class1.isArray() && class1 == class2) {
                        // messy, but the ONLY way to do it.
                        Object[] vararg = (Object[]) Array.newInstance(class1.getClass(), 2);
                        vararg[0] = message1;
                        vararg[1] = message2;

                        handled = true;
                        sub.publishToSubscription(this, vararg);
                    }
                }

                if (!handled) {
                    sub.publishToSubscription(this, message1, message2);
                }
            }

            // if the message did not have any listener/handler accept it
            if (subscriptions.isEmpty()) {
                // cannot have DeadMessage published to this, so no extra check necessary
                // Dead Event
                subscriptions = manager.getSubscriptionsByMessageType(DeadMessage.class);
                DeadMessage deadMessage = new DeadMessage(new Object[] {message1, message2});

                for (Subscription sub : subscriptions) {
                    sub.publishToSubscription(this, deadMessage);
                }
            }
        }
    }

    // cannot have DeadMessage published to this!
    public void publishMessage(Object message1, Object message2, Object message3) {
        Class<?> messageClass1 = message1.getClass();
        Class<?> messageClass2 = message2.getClass();
        Class<?> messageClass3 = message3.getClass();

        SubscriptionManager manager = this.subscriptionManager;
        Collection<Subscription> subscriptions = manager.getSubscriptionsByMessageType(messageClass1, messageClass2, messageClass3);

        if (subscriptions == null || subscriptions.isEmpty()) {
            // Dead Event
            subscriptions = manager.getSubscriptionsByMessageType(DeadMessage.class);
            DeadMessage deadMessage = new DeadMessage(new Object[] {message1, message2, message3});

            for (Subscription sub : subscriptions) {
                sub.publishToSubscription(this, deadMessage);
            }
        } else {
            for (Subscription sub : subscriptions) {
                boolean handled = false;
                if (sub.isVarArg()) {
                    Class<?> class1 = message1.getClass();
                    Class<?> class2 = message2.getClass();
                    Class<?> class3 = message3.getClass();
                    if (!class1.isArray() && class1 == class2 && class2 == class3) {
                        // messy, but the ONLY way to do it.
                        Object[] vararg = (Object[]) Array.newInstance(class1.getClass(), 3);
                        vararg[0] = message1;
                        vararg[1] = message2;
                        vararg[2] = message3;

                        handled = true;
                        sub.publishToSubscription(this, vararg);
                    }
                }

                if (!handled) {
                    sub.publishToSubscription(this, message1, message2, message3);
                }
            }

            // if the message did not have any listener/handler accept it
            if (subscriptions.isEmpty()) {
                // cannot have DeadMessage published to this, so no extra check necessary
                // Dead Event
                subscriptions = manager.getSubscriptionsByMessageType(DeadMessage.class);
                DeadMessage deadMessage = new DeadMessage(new Object[] {message1, message2});

                for (Subscription sub : subscriptions) {
                    sub.publishToSubscription(this, deadMessage);
                }
            }
        }
    }

    // cannot have DeadMessage published to this!
    public void publishMessage(Object... messages) {
        int size = messages.length;
        Class<?>[] messageClasses = new Class[size];
        for (int i=0;i<size;i++) {
            messageClasses[i] = messages[i].getClass();
        }

        SubscriptionManager manager = this.subscriptionManager;
        Collection<Subscription> subscriptions = manager.getSubscriptionsByMessageType(messageClasses);

        if (subscriptions == null || subscriptions.isEmpty()) {
            // Dead Event
            subscriptions = manager.getSubscriptionsByMessageType(DeadMessage.class);
            DeadMessage deadMessage = new DeadMessage(messages);

            for (Subscription sub : subscriptions) {
                sub.publishToSubscription(this, deadMessage);
            }
        } else {
            for (Subscription sub : subscriptions) {
                sub.publishToSubscription(this, messages);
            }

            // if the message did not have any listener/handler accept it
            if (subscriptions.isEmpty()) {
                // cannot have DeadMessage published to this, so no extra check necessary
                // Dead Event

                subscriptions = manager.getSubscriptionsByMessageType(DeadMessage.class);
                DeadMessage deadMessage = new DeadMessage(messages);

                for (Subscription sub : subscriptions) {
                    sub.publishToSubscription(this, deadMessage);
                }
            }
        }
    }
}
