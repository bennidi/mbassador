package net.engio.mbassy.common;


import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This data structure is optimized for non-blocking reads even when write operations occur.
 * Running read iterators will not be affected by add operations since writes always insert at the head of the
 * structure. Remove operations can affect any running iterator such that a removed element that has not yet
 * been reached by the iterator will not appear in that iterator anymore.
 *
 * @author bennidi
 *         Date: 2/12/12
 */
public abstract class AbstractConcurrentSet<T> implements IConcurrentSet<T> {

    // Internal state
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<T, ISetEntry<T>> entries; // maintain a map of entries for O(log n) lookup
    protected Entry<T> head; // reference to the first element

    protected AbstractConcurrentSet(Map<T, ISetEntry<T>> entries) {
        this.entries = entries;
    }

    protected abstract Entry<T> createEntry(T value, Entry<T> next);

    @Override
    public IConcurrentSet<T> add(T element) {
        if (element == null) return this;
        Lock writeLock = lock.writeLock();
        writeLock.lock();
        if (element == null || entries.containsKey(element)) {
            writeLock.unlock();
            return this;
        } else {
            insert(element);
            writeLock.unlock();
        }
        return this;
    }

    @Override
    public boolean contains(T element) {
        Lock readLock = lock.readLock();
        ISetEntry<T> entry;
        try {
            readLock.lock();
            entry = entries.get(element);

        } finally {
            readLock.unlock();
        }
        return entry != null && entry.getValue() != null;
    }

    private void insert(T element) {
        if (!entries.containsKey(element)) {
            head = createEntry(element, head);
            entries.put(element, head);
        }
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public IConcurrentSet<T> addAll(Iterable<T> elements) {
        Lock writeLock = lock.writeLock();
        try {
            writeLock.lock();
            for (T element : elements) {
                if (element != null) {
                    insert(element);
                }
            }
        } finally {
            writeLock.unlock();
        }
        return this;
    }

    @Override
    public boolean remove(T element) {
        if (!contains(element)) {
            return false;
        } else {
            Lock writeLock = lock.writeLock();
            try {
                writeLock.lock();
                ISetEntry<T> listelement = entries.get(element);
                if (listelement == null) {
                    return false; //removed by other thread
                }
                if (listelement != head) {
                    listelement.remove();
                } else {
                    ISetEntry<T> oldHead = head;
                    head = head.next();
                    //oldHead.clear(); // optimize for GC not possible because of potentially running iterators
                }
                entries.remove(element);
            } finally {
                writeLock.unlock();
            }
            return true;
        }
    }


    public abstract static class Entry<T> implements ISetEntry<T> {

        private Entry<T> next;

        private Entry<T> predecessor;

        protected Entry(Entry<T> next) {
            this.next = next;
            next.predecessor = this;
        }

        protected Entry() {
        }

        // not thread-safe! must be synchronized in enclosing context
        @Override
        public void remove() {
            if (predecessor != null) {
                predecessor.next = next;
                if (next != null) {
                    next.predecessor = predecessor;
                }
            } else if (next != null) {
                next.predecessor = null;
            }
            // can not nullify references to help GC since running iterators might not see the entire set
            // if this element is their current element
            //next = null;
            //predecessor = null;
        }

        @Override
        public Entry<T> next() {
            return next;
        }

        @Override
        public void clear() {
            next = null;
        }
    }
}
