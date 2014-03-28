package net.engio.mbassy.messages;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Enum used to test handlers that consume enumerations.
 *
 * @author bennidi
 *         Date: 5/24/13
 */
public enum MessageTypes implements IMessage{

    Simple,
    Persistent,
    Multipart;

    private Map<Class, Integer> handledByListener = new HashMap<Class, Integer>();
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public static void resetAll(){
        for(MessageTypes m : values())
            m.reset();
    }


    @Override
    public void reset() {
        try {
            lock.writeLock().lock();
            handledByListener.clear();
        }finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public void handled(Class listener) {
        try {
            lock.writeLock().lock();
            Integer count = handledByListener.get(listener);
            if(count == null){
                handledByListener.put(listener, 1);
            }
            else{
                handledByListener.put(listener, count + 1);
            }
        }finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public int getTimesHandled(Class listener) {
        try {
            lock.readLock().lock();
            return handledByListener.containsKey(listener)
                    ? handledByListener.get(listener)
                    : 0;
        }finally {
            lock.readLock().unlock();
        }
    }
}
