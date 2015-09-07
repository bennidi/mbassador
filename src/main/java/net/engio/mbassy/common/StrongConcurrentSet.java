package net.engio.mbassy.common;


import java.util.HashMap;
import java.util.Iterator;

/**
 * This implementation uses strong references to the elements.
 * <p/>
 *
 * @author bennidi
 *         Date: 2/12/12
 */
public class StrongConcurrentSet<T> extends AbstractConcurrentSet<T>{


    public StrongConcurrentSet() {
        super(new HashMap<T, ISetEntry<T>>());
    }

    public Iterator<T> iterator() {
        return new Iterator<T>() {

            private ISetEntry<T> current = head;

            public boolean hasNext() {
                return current != null;
            }

            public T next() {
                if (current == null) {
                    return null;
                }
               else {
                    T value = current.getValue();
                    current = current.next();
                    return value;
                }
            }

            public void remove() {
                if (current == null) {
                    return;
                }
                ISetEntry<T> newCurrent = current.next();
                StrongConcurrentSet.this.remove(current.getValue());
                current = newCurrent;
            }
        };
    }

    @Override
    protected Entry<T> createEntry(T value, Entry<T> next) {
        return next != null ? new StrongEntry<T>(value, next) : new StrongEntry<T>(value);
    }


    public static class StrongEntry<T> extends Entry<T> {

        private T value;

        private StrongEntry(T value, Entry<T> next) {
            super(next);
            this.value = value;
        }

        private StrongEntry(T value) {
            super();
            this.value = value;
        }

        @Override
        public T getValue() {
            return value;
        }




    }
}
