package net.engio.mbassy.common;

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
    private List generatedListeners;
    private int requiredSize = 0;

    public int getNumberOfListeners(Class listener){
        return requiredBeans.containsKey(listener) ? requiredBeans.get(listener) : 0;
    }

    public ListenerFactory create(int numberOfInstances, Class clazz){
        requiredBeans.put(clazz, numberOfInstances);
        requiredSize +=numberOfInstances;
        return this;
    }


    public List<Object> getAll(){
        generatedListeners = new ArrayList(requiredSize);
        try {
            for(Class clazz : requiredBeans.keySet()){
                int numberOfRequiredBeans = requiredBeans.get(clazz);
                for(int i = 0; i < numberOfRequiredBeans; i++){
                    generatedListeners.add(clazz.newInstance());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        Collections.shuffle(generatedListeners);
        return generatedListeners;
    }

    // not thread-safe but not yet used concurrently
    public void clear(){
        generatedListeners = null;
        requiredBeans.clear();
    }

    /**
     * Create a thread-safe read-only iterator
     * @return
     */
    public Iterator iterator(){
        if(generatedListeners == null)getAll();
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
