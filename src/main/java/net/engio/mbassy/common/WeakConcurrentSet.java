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

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {

            // the current listelement of this iterator
            // used to keep track of the iteration process
            private ISetEntry<T> current = WeakConcurrentSet.this.head;

            // this method will remove all orphaned entries
            // until it finds the first entry whose value has not yet been garbage collected
            // the method assumes that the current element is already orphaned and will remove it
            private void removeOrphans(){
                Lock writelock = WeakConcurrentSet.this.lock.writeLock();
                try{
                    writelock.lock();
                    do {
                        ISetEntry<T> orphaned = this.current;
                        this.current = this.current.next();
                        orphaned.remove();
                    } while(this.current != null && this.current.getValue() == null);
                }
                finally {
                    writelock.unlock();
                }
            }


            @Override
            public boolean hasNext() {
                if (this.current == null) {
                    return false;
                }
                if (this.current.getValue() == null) {
                // trigger removal of orphan references
                // because a null value indicates that the value has been garbage collected
                    removeOrphans();
                    return this.current != null; // if any entry is left then it will have a value
                } else {
                    return true;
                }
            }

            @Override
            public T next() {
                if (this.current == null) {
                    return null;
                }
                T value = this.current.getValue();
                if (value == null) {    // auto-removal of orphan references
                    removeOrphans();
                    return next();
                } else {
                    this.current = this.current.next();
                    return value;
                }
            }

            @Override
            public void remove() {
                //throw new UnsupportedOperationException("Explicit removal of set elements is only allowed via the controlling set. Sorry!");
                if (this.current == null) {
                    return;
                }
                ISetEntry<T> newCurrent = this.current.next();
                WeakConcurrentSet.this.remove(this.current.getValue());
                this.current = newCurrent;
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
            return this.value.get();
        }




    }
}
