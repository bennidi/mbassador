package net.engio.mbassy.common;


import java.util.Map;

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
    private final Object lock = new Object();
    private final Map<T, ISetEntry<T>> entries; // maintain a map of entries for O(log n) lookup
    protected Entry<T> head; // reference to the first element

    protected AbstractConcurrentSet(Map<T, ISetEntry<T>> entries) {
        this.entries = entries;
    }

    protected abstract Entry<T> createEntry(T value, Entry<T> next);

    @Override
    public IConcurrentSet<T> add(T element) {
        if (element == null || entries.containsKey(element)) {
            return this;
        }
        synchronized (lock) {
            insert(element);
        }
        return this;
    }

    @Override
    public boolean contains(T element) {
        ISetEntry<T> entry = entries.get(element);
        return entry != null && entry.getValue() != null;
    }

    private void insert(T element) {
        if (entries.containsKey(element)) {
            return;
        }
        head = createEntry(element, head);
        entries.put(element, head);
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public IConcurrentSet<T> addAll(Iterable<T> elements) {
        synchronized (lock) {
            for (T element : elements) {
                if (element == null || entries.containsKey(element)) {
                    return this;
                }

                insert(element);
            }
        }
        return this;
    }

    @Override
    public boolean remove(T element) {
        if (!entries.containsKey(element)) {
            return false;
        }
        synchronized (lock) {
            ISetEntry<T> listelement = entries.get(element);
            if (listelement == null) {
                return false; //removed by other thread
            }
            if (listelement != head) {
                listelement.remove();
            } else {
                ISetEntry<T> oldHead = head;
                head = head.next();
                oldHead.clear(); // optimize for GC
            }
            entries.remove(element);
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
            next = null;
            predecessor = null;
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
