package net.engio.mbassy.common;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.common.PubSubSupport;
import net.engio.mbassy.subscription.SubscriptionManager;

import java.util.Iterator;
import java.util.List;

/**
 * Todo: Add javadoc
 *
 * @author bennidi
 *         Date: 11/22/12
 */
public class TestUtil {


    public static Runnable subscriber(final SubscriptionManager manager, final ListenerFactory listeners){
        final Iterator source = listeners.iterator();
        return new Runnable() {
            @Override
            public void run() {
                Object next;
                while((next = source.next()) != null){
                    manager.subscribe(next);
                }
            }
        };
    }

    public static Runnable unsubscriber(final SubscriptionManager manager, final ListenerFactory listeners){
        final Iterator source = listeners.iterator();
        return new Runnable() {
            @Override
            public void run() {
                Object next;
                while((next = source.next()) != null){
                    manager.unsubscribe(next);
                }
            }
        };
    }

    public static Runnable subscriber(final PubSubSupport bus, final ListenerFactory listeners){
        final Iterator source = listeners.iterator();
        return new Runnable() {
            @Override
            public void run() {
                Object next;
                while((next = source.next()) != null){
                    bus.subscribe(next);
                }
            }
        };
    }

    public static Runnable unsubscriber(final PubSubSupport bus, final ListenerFactory listeners){
        final Iterator source = listeners.iterator();
        return new Runnable() {
            @Override
            public void run() {
                Object next;
                while((next = source.next()) != null){
                    bus.unsubscribe(next);
                }
            }
        };
    }

    public static void setup(final PubSubSupport bus, final List<Object> listeners, int numberOfThreads) {
        Runnable[] setupUnits = new Runnable[numberOfThreads];
        int partitionSize;
        if(listeners.size() >= numberOfThreads){
          partitionSize =  (int)Math.floor(listeners.size() / numberOfThreads);
        }
        else{
            partitionSize = 1;
            numberOfThreads = listeners.size();
        }

        for(int i = 0; i < numberOfThreads; i++){
            final int partitionStart = i * partitionSize;
            final int partitionEnd = (i+1 < numberOfThreads)
                    ? partitionStart + partitionSize + 1
                    : listeners.size();
            setupUnits[i] = new Runnable() {

                private List<Object> listenerSubset = listeners.subList(partitionStart, partitionEnd);

                public void run() {
                   for(Object listener : listenerSubset){
                       bus.subscribe(listener);
                   }
                }
            };

        }

        ConcurrentExecutor.runConcurrent(setupUnits);

    }

    public static void setup(MBassador bus, ListenerFactory listeners, int numberOfThreads) {
        setup(bus, listeners.getAll(), numberOfThreads);

    }
}
