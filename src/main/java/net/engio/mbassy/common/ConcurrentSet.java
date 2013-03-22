package net.engio.mbassy.common;


import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.WeakHashMap;

/**
 * This data structure is optimized for non-blocking reads even when write operations occur.
 * Running read iterators will not be affected by add operations since writes always insert at the head of the
 * structure. Remove operations can affect any running iterator such that a removed element that has not yet
 * been reached by the iterator will not appear in that iterator anymore.
 * <p/>
 * The structure uses weak references to the elements. Iterators automatically perform cleanups of
 * garbage collected objects during iteration -> no dedicated maintenance operations need to be called or run in background.
 * <p/>
 * <p/>
 * <p/>
 *
 * @author bennidi
 *         Date: 2/12/12
 */
public class ConcurrentSet<T> implements Iterable<T>{

    private WeakHashMap<T, Entry<T>> entries = new WeakHashMap<T, Entry<T>>(); // maintain a map of entries for O(log n) lookup

    private Entry<T> head; // reference to the first element

    public ConcurrentSet<T> add(T element) {
        if (element == null || entries.containsKey(element)) return this;
        synchronized (this) {
            insert(element);
        }
        return this;
    }

    public boolean contains(T element){
        Entry<T> entry = entries.get(element);
        return entry != null && entry.getValue() != null;
    }

    private void insert(T element) {
        if (entries.containsKey(element)) return;
        if (head == null) {
            head = new Entry<T>(element);
        } else {
            head = new Entry<T>(element, head);
        }
        entries.put(element, head);
    }

    public int size(){
        return entries.size();
    }

    public ConcurrentSet<T> addAll(Iterable<T> elements) {
        synchronized (this) {
            for (T element : elements) {
                if (element == null || entries.containsKey(element)) return this;

                insert(element);
            }
        }
        return this;
    }

    public boolean remove(T element) {
        if (!entries.containsKey(element)) return false;
        synchronized (this) {
            Entry<T> listelement = entries.get(element);
            if(listelement == null)return false; //removed by other thread
            if (listelement != head) {
                listelement.remove();
            } else {
                Entry<T> oldHead = head;
                head = head.next();
                oldHead.next = null; // optimize for GC
            }
            entries.remove(element);
        }
        return true;
    }

    public Iterator<T> iterator() {
        return new Iterator<T>() {

            private Entry<T> current = head;

            public boolean hasNext() {
                if (current == null) return false;
                if (current.getValue() == null) {    // auto-removal of orphan references
                    do {
                        remove();
                    } while(current != null && current.getValue() == null);
                    return hasNext();
                } else {
                    return true;
                }
            }

            public T next() {
                if (current == null) return null;
                T value = current.getValue();
                if (value == null) {    // auto-removal of orphan references
                    do {
                        remove();
                    } while(current != null && current.getValue() == null);
                    return next();
                } else {
                    current = current.next();
                    return value;
                }
            }

            public void remove() {
                if (current == null) return;
                Entry<T> newCurrent = current.next();
                ConcurrentSet.this.remove(current.getValue());
                current = newCurrent;
            }
        };
    }


    public class Entry<T> {

        private WeakReference<T> value;

        private Entry<T> next;

        private Entry<T> predecessor;


        private Entry(T value) {
            this.value = new WeakReference<T>(value);
        }

        private Entry(T value, Entry<T> next) {
            this(value);
            this.next = next;
            next.predecessor = this;
        }

        public T getValue() {
            return value.get();
        }

        // not thread-safe! must be synchronized in enclosing context
        public void remove() {
            if (predecessor != null) {
                predecessor.next = next;
                if(next != null)next.predecessor = predecessor;
            } else if (next != null) {
                next.predecessor = null;
            }
            next = null;
            predecessor = null;
        }

        public Entry<T> next() {
            return next;
        }


    }
}
