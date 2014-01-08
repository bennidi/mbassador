package net.engio.mbassy.common;

import net.engio.mbassy.subscription.Subscription;
import net.engio.mbassy.subscription.SubscriptionManager;

import java.util.*;

/**
*
* @author bennidi
*         Date: 5/25/13
*/
public class SubscriptionValidator extends AssertSupport{


    private List<ValidationEntry> validations = new LinkedList<ValidationEntry>();
    private Set<Class> messageTypes = new HashSet<Class>();
    private ListenerFactory subscribedListener; // the subscribed listeners are used to assert the size of the subscriptions

    public SubscriptionValidator(ListenerFactory subscribedListener) {
        this.subscribedListener = subscribedListener;
    }

    public Expectation listener(Class subscriber){
        return new Expectation(subscriber);
    }

    private SubscriptionValidator expect(Class subscriber, Class messageType){
        validations.add(new ValidationEntry(messageType, subscriber));
        messageTypes.add(messageType);
        return this;
    }

    // match subscriptions with existing validation entries
    // for each tuple of subscriber and message type the specified number of listeners must exist
    public void validate(SubscriptionManager manager){
        for(Class messageType : messageTypes){
            Collection<Subscription> subscriptions = manager.getSubscriptionsByMessageType(messageType);
            ensureOrdering(subscriptions);
            Collection<ValidationEntry> validationEntries = getEntries(EntriesByMessageType(messageType));
            assertEquals(subscriptions.size(), validationEntries.size());
            for(ValidationEntry validationValidationEntry : validationEntries){
                Subscription matchingSub = null;
                // one of the subscriptions must belong to the subscriber type
                for(Subscription sub : subscriptions){
                    if(sub.belongsTo(validationValidationEntry.subscriber)){
                        matchingSub = sub;
                        break;
                    }
                }
                assertNotNull(matchingSub);
                assertEquals(subscribedListener.getNumberOfListeners(validationValidationEntry.subscriber), matchingSub.size());
            }
        }
    }

    private void ensureOrdering(Collection<Subscription> subscriptions){
        int lastPriority = Integer.MAX_VALUE;// highest priority possible
        for(Subscription sub : subscriptions){
            assertTrue("Subscriptions should be ordered by priority (DESC)", lastPriority >= sub.getPriority());
            lastPriority = sub.getPriority();
        }
    }


    private Collection<ValidationEntry> getEntries(IPredicate<ValidationEntry> filter){
        Collection<ValidationEntry> matching = new LinkedList<ValidationEntry>();
        for (ValidationEntry validationValidationEntry : validations){
            if(filter.apply(validationValidationEntry))matching.add(validationValidationEntry);
        }
        return matching;
    }

    private IPredicate<ValidationEntry> EntriesByMessageType(final Class messageType){
        return new IPredicate<ValidationEntry>() {
            @Override
            public boolean apply(ValidationEntry target) {
                return target.messageType.equals(messageType);
            }
        };
    }



    public class Expectation{

        private Class listener;

        private Expectation(Class listener) {
            this.listener = listener;
        }

        public SubscriptionValidator handles(Class ...messages){
            for(Class message : messages)
                expect(listener, message);
            return SubscriptionValidator.this;
        }
    }

    private class ValidationEntry {


        private Class subscriber;

        private Class messageType;

        private ValidationEntry(Class messageType, Class subscriber) {
            this.messageType = messageType;
            this.subscriber = subscriber;
        }


    }

}
