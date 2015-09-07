package net.engio.mbassy.common;


import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.WeakHashMap;
import java.util.concurrent.locks.Lock;

/**
 * This implementation uses weak references to the elements. Iterators automatically perform cleanups of
 * garbage collected objects during iteration -> no dedicated maintenance operations need to be called or run in background.
 * <p/>
 * <p/>
 * <p/>
 *
 * @author bennidi
 *         Date: 2/12/12
 */
public class WeakConcurrentSet<T> extends AbstractConcurrentSet<T>{


    public WeakConcurrentSet() {
        super(new WeakHashMap<T, ISetEntry<T>>());
    }

    public Iterator<T> iterator() {
        return new Iterator<T>() {

            // the current listelement of this iterator
            // used to keep track of the iteration process
            private ISetEntry<T> current = head;

            // this method will remove all orphaned entries
            // until it finds the first entry whose value has not yet been garbage collected
            // the method assumes that the current element is already orphaned and will remove it
            private void removeOrphans(){
                Lock writelock = lock.writeLock();
                try{
                    writelock.lock();
                    do {
                        ISetEntry orphaned = current;
                        current = current.next();
                        if (orphaned  == head) {
                            head = head.next();
                        }
                        orphaned.remove();
                    } while(current != null && current.getValue() == null);
                }
                finally {
                    writelock.unlock();
                }
            }


            public boolean hasNext() {
                if (current == null) return false;
                if (current.getValue() == null) {
                // trigger removal of orphan references
                // because a null value indicates that the value has been garbage collected
                    removeOrphans();
                    return current != null; // if any entry is left then it will have a value
                } else {
                    return true;
                }
            }

            public T next() {
                if (current == null) {
                    return null;
                }
                T value = current.getValue();
                if (value == null) {    // auto-removal of orphan references
                    removeOrphans();
                    return next();
                } else {
                    current = current.next();
                    return value;
                }
            }

            public void remove() {
                //throw new UnsupportedOperationException("Explicit removal of set elements is only allowed via the controlling set. Sorry!");
                if (current == null) {
                    return;
                }
                ISetEntry<T> newCurrent = current.next();
                WeakConcurrentSet.this.remove(current.getValue());
                current = newCurrent;
            }
        };
    }

    @Override
    protected Entry<T> createEntry(T value, Entry<T> next) {
        return next != null ? new WeakEntry<T>(value, next) : new WeakEntry<T>(value);
    }


    public static class WeakEntry<T> extends Entry<T> {

        private WeakReference<T> value;

        private WeakEntry(T value, Entry<T> next) {
            super(next);
            this.value = new WeakReference<T>(value);
        }

        private WeakEntry(T value) {
            super();
            this.value = new WeakReference<T>(value);
        }

        @Override
        public T getValue() {
            return value.get();
        }




    }
}
