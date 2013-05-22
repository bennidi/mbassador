package net.engio.mbassy;

import net.engio.mbassy.common.ConcurrentExecutor;
import net.engio.mbassy.common.IPredicate;
import net.engio.mbassy.common.UnitTest;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.MetadataReader;
import net.engio.mbassy.messages.ITestMessage;
import net.engio.mbassy.messages.TestMessage;
import net.engio.mbassy.subscription.Subscription;
import net.engio.mbassy.subscription.SubscriptionFactory;
import net.engio.mbassy.subscription.SubscriptionManager;
import org.junit.Test;

import java.util.*;

/**
 * Todo: Add javadoc
 *
 * @author bennidi
 *         Date: 5/12/13
 */
public class SubscriptionManagerTest extends UnitTest{


    @Test
    public void testSimpleSynchronousHandler(){
        final SubscriptionManager subMan = new SubscriptionManager(new MetadataReader(), new SubscriptionFactory());
        final Set listeners = Collections.synchronizedSet(new HashSet());
        final int concurrentUnits = 5;
        final int numberOfLoops = 100;
        final int numberOfListeners =  numberOfLoops * concurrentUnits;

        ConcurrentExecutor.runConcurrent(new Runnable() {
            @Override
            public void run() {
                for(int i = 0 ; i < numberOfLoops ; i++){
                    SimpleSynchronousMessageHandler
                            listener1 = new SimpleSynchronousMessageHandler();
                    SimpleSynchronousMessageHandler2 listener2 = new SimpleSynchronousMessageHandler2();
                    subMan.subscribe(listener1);
                    subMan.subscribe(listener2);
                    listeners.add(listener1);
                    listeners.add(listener2);
                }

            }
        }, concurrentUnits);

        SubscriptionValidator validator = new SubscriptionValidator();
        validator.expect(numberOfListeners, SimpleSynchronousMessageHandler.class, ITestMessage.class);
        validator.expect(numberOfListeners, SimpleSynchronousMessageHandler2.class, ITestMessage.class);
        validator.expect(numberOfListeners, SimpleSynchronousMessageHandler.class, TestMessage.class);
        validator.expect(numberOfListeners, SimpleSynchronousMessageHandler2.class, TestMessage.class);

        validator.validate(subMan);

    }


    class SubscriptionValidator{


        private List<Entry> validations = new LinkedList<Entry>();
        private Set<Class> messageTypes = new HashSet<Class>();
        private Set<Class> subsribers = new HashSet<Class>();


        public SubscriptionValidator expect(int numberOfSubscriber, Class subscriber, Class messageType){
            validations.add(new Entry(messageType, numberOfSubscriber, subscriber));
            messageTypes.add(messageType);
            subsribers.add(subscriber);
            return this;
        }

        public void validate(SubscriptionManager manager){
            for(Class messageType : messageTypes){
                Collection<Subscription> subscriptions = manager.getSubscriptionsByMessageType(messageType);
                Collection<Entry> validationEntries = getEntries(EntriesByMessageType(messageType));
                assertEquals(subscriptions.size(), validationEntries.size());
                for(Entry validationEntry : validationEntries){
                    Subscription matchingSub = null;
                    // one of the subscriptions must belong to the subscriber type
                    for(Subscription sub : subscriptions){
                        if(sub.belongsTo(validationEntry.subscriber)){
                            matchingSub = sub;
                            break;
                        }
                    }
                    assertNotNull(matchingSub);
                    assertEquals(validationEntry.numberOfSubscribers, matchingSub.size());
                }
            }
        }


        private Collection<Entry> getEntries(IPredicate<Entry> filter){
            Collection<Entry> matching = new LinkedList<Entry>();
            for (Entry validationEntry : validations){
                if(filter.apply(validationEntry))matching.add(validationEntry);
            }
            return matching;
        }

        private IPredicate<Entry> EntriesByMessageType(final Class messageType){
            return new IPredicate<Entry>() {
                @Override
                public boolean apply(Entry target) {
                    return target.messageType.equals(messageType);
                }
            };
        }

        private IPredicate<Entry> EntriesBySubscriberType(final Class subscriberType){
            return new IPredicate<Entry>() {
                @Override
                public boolean apply(Entry target) {
                    return target.subscriber.equals(subscriberType);
                }
            };
        }



        private class Entry{

            private int numberOfSubscribers;

            private Class subscriber;

            private Class messageType;

            private Entry(Class messageType, int numberOfSubscribers, Class subscriber) {
                this.messageType = messageType;
                this.numberOfSubscribers = numberOfSubscribers;
                this.subscriber = subscriber;
            }


        }

    }


    static class SimpleSynchronousMessageHandler{

        @Handler
        public void handle(TestMessage message) {
        }

        @Handler
        public void handle(ITestMessage message) {
        }
    }


    static class SimpleSynchronousMessageHandler2{

        @Handler
        public void handle(TestMessage message) {
        }

        @Handler
        public void handle(ITestMessage message) {
        }
    }
}
