package net.engio.mbassy.common;

import junit.framework.Assert;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The factory can be used to declaratively specify how many instances of some given classes
 * should be created. It will create those instances using reflection and provide a list containing those instances.
 * The factory also holds strong references to the instances such that GC will not interfere with tests unless the
 * factory is explicitly cleared.
 *
 * @author bennidi
 *         Date: 11/22/12
 */
public class ListenerFactory {

    private Map<Class, Integer> requiredBeans = new HashMap<Class, Integer>();
    private volatile List generatedListeners;
    private int requiredSize = 0;

    public int getNumberOfListeners(Class listener){
        return requiredBeans.containsKey(listener) ? requiredBeans.get(listener) : 0;
    }

    public ListenerFactory create(int numberOfInstances, Class clazz){
        requiredBeans.put(clazz, numberOfInstances);
        requiredSize +=numberOfInstances;
        return this;
    }

    public ListenerFactory create(int numberOfInstances, Class ...classes){
        for(Class clazz : classes)
            create(numberOfInstances,clazz);
        return this;
    }

    public ListenerFactory create(int numberOfInstances, Collection<Class> classes){
        for(Class clazz : classes)
            create(numberOfInstances,clazz);
        return this;
    }


    public synchronized List<Object> getAll(){
        if(generatedListeners != null)
            return generatedListeners;
        List listeners = new ArrayList(requiredSize);
        try {
            for(Class clazz : requiredBeans.keySet()){
                int numberOfRequiredBeans = requiredBeans.get(clazz);
                for(int i = 0; i < numberOfRequiredBeans; i++){
                    listeners.add(clazz.newInstance());
                }
            }
        } catch (Exception e) {
            // if instantiation fails, counts will be incorrect
            // -> fail early here
            Assert.fail("There was a problem instantiating a listener " + e);
        }
        Collections.shuffle(listeners);
        generatedListeners  = Collections.unmodifiableList(listeners);
        return generatedListeners;
    }

    // not thread-safe but not yet used concurrently
    public synchronized  void clear(){
        generatedListeners = null;
        requiredBeans.clear();
    }

    /**
     * Create a thread-safe read-only iterator
     *
     * NOTE: Iterator is not perfectly synchronized with mutator methods of the list of generated listeners
     * In theory, it is possible that the list is changed while iterators are still running which should be avoided.
     */
    public Iterator iterator(){
        getAll();
        final AtomicInteger current = new AtomicInteger(0);

        return new Iterator() {
            @Override
            public boolean hasNext() {
                return current.get() < generatedListeners.size();
            }

            @Override
            public Object next() {
                int index =  current.getAndIncrement();
                return index < generatedListeners.size() ? generatedListeners.get(index) : null;
            }

            @Override
            public void remove() {
                throw new UnsupportedOperationException("Iterator is read only");
            }
        };
    }


}
