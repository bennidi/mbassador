package net.engio.mbassy.common;


import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.WeakHashMap;

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

            private ISetEntry<T> current = head;

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
                if (current == null) {
                    return null;
                }
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
