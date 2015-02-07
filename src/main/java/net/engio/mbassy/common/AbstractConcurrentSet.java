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
    public void add(T element) {
        if (element == null) {
            return;
        }
        Lock writeLock = this.lock.writeLock();
        writeLock.lock();
        if (this.entries.containsKey(element)) {
        } else {
            insert(element);
        }
        writeLock.unlock();
    }

    @Override
    public boolean contains(T element) {
        Lock readLock = this.lock.readLock();
        ISetEntry<T> entry;
        try {
            readLock.lock();
            entry = this.entries.get(element);

        } finally {
            readLock.unlock();
        }
        return entry != null && entry.getValue() != null;
    }

    private void insert(T element) {
        if (!this.entries.containsKey(element)) {
            this.head = createEntry(element, this.head);
            this.entries.put(element, this.head);
        }
    }

    @Override
    public int size() {
        return this.entries.size();
    }

    @Override
    public void addAll(Iterable<T> elements) {
        Lock writeLock = this.lock.writeLock();
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
    }

    /**
     * The return on this is DIFFERENT than normal.
     *
     * @return TRUE if there are no more elements (aka: this set is now empty)
     */
    @Override
    public boolean remove(T element) {
        if (!contains(element)) {
            // return quickly
            Lock readLock = this.lock.readLock();
            readLock.lock();
            boolean headIsNull = this.head == null;
            readLock.unlock();

            return headIsNull;
        } else {
            boolean wasLastElement = false;
            Lock writeLock = this.lock.writeLock();
            try {
                writeLock.lock();
                ISetEntry<T> listelement = this.entries.get(element);
                if (listelement == null) {
                    return false; //removed by other thread in the meantime
                }
                if (listelement != this.head) {
                    listelement.remove();
                } else {
                    // if it was second, now it's first
                    this.head = this.head.next();
                    //oldHead.clear(); // optimize for GC not possible because of potentially running iterators
                }
                this.entries.remove(element);

                if (this.head == null) {
                    wasLastElement = true;
                }
            } finally {
                writeLock.unlock();
            }
            return wasLastElement;
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
            if (this.predecessor != null) {
                this.predecessor.next = this.next;
                if (this.next != null) {
                    this.next.predecessor = this.predecessor;
                }
            } else if (this.next != null) {
                this.next.predecessor = null;
            }
            // can not nullify references to help GC since running iterators might not see the entire set
            // if this element is their current element
            //next = null;
            //predecessor = null;
        }

        @Override
        public Entry<T> next() {
            return this.next;
        }

        @Override
        public void clear() {
            this.next = null;
        }
    }
}
