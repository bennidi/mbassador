package net.engio.mbassy.common;


import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
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
 * @author dorkbox
 *         Date: 22/2/15
 */
public abstract class AbstractConcurrentSet<T> implements Set<T> {
    private static final AtomicLong id = new AtomicLong();
    private final long ID = id.getAndIncrement();

    // Internal state
    protected final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private final Map<T, ISetEntry<T>> entries; // maintain a map of entries for O(1) lookup
    protected Entry<T> head; // reference to the first element

    protected AbstractConcurrentSet(Map<T, ISetEntry<T>> entries) {
        this.entries = entries;
    }

    protected abstract Entry<T> createEntry(T value, Entry<T> next);

    @Override
    public boolean add(T element) {
        if (element == null) return false;
        Lock writeLock = lock.writeLock();
        boolean changed;
        try {
            writeLock.lock();
            changed = insert(element);
        } finally {
            writeLock.unlock();
        }
        return changed;
    }

    @Override
    public boolean contains(Object element) {
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


    /**
     * Inserts a new element at the head of the set.
     * Note: This method is expected to be synchronized by the calling code
     */
    private boolean insert(T element) {
        if (!entries.containsKey(element)) {
            head = createEntry(element, head);
            entries.put(element, head);
            return true;
        }
        return false;
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public boolean isEmpty() {
        return head == null;
    }

    @Override
    public boolean addAll(Collection<? extends T> elements) {
        boolean changed = false;
        Lock writeLock = lock.writeLock();
        try {
            writeLock.lock();
            for (T element : elements) {
                if (element != null) {
                    changed |= insert(element);
                }
            }
        } finally {
            writeLock.unlock();
        }
        return changed;
    }

    @Override
    public boolean remove(Object element) {
        if (!contains(element)) {
            return false;
        } else {
            Lock writeLock = lock.writeLock();
            try {
                writeLock.lock();
                ISetEntry<T> listelement = entries.get(element);
                if (listelement == null) {
                    return false; //removed by other thread in the meantime
                }
                if (listelement != head) {
                    listelement.remove();
                } else {
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

    @Override
    public Object[] toArray() {
        return this.entries.entrySet().toArray();
    }

    @SuppressWarnings("hiding")
    @Override
    public <T> T[] toArray(T[] a) {
        return this.entries.entrySet().toArray(a);
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void clear() {
        Lock writeLock = this.lock.writeLock();
        try {
            writeLock.lock();
                head = null;
                entries.clear();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + (int) (this.ID ^ this.ID >>> 32);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        @SuppressWarnings("rawtypes")
        AbstractConcurrentSet other = (AbstractConcurrentSet) obj;
        if (this.ID != other.ID) {
            return false;
        }
        return true;
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

        // Not thread-safe! must be synchronized in enclosing context
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
            // Can not nullify references to help GC because running iterators might not see the entire set
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
