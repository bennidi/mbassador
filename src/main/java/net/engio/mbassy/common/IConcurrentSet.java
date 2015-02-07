package net.engio.mbassy.common;

/**
 * Todo: Add javadoc
 *
 * @author bennidi
 *         Date: 3/29/13
 */
public interface IConcurrentSet<T> extends Iterable<T> {

    void add(T element);

    boolean contains(T element);

    int size();

    boolean isEmpty();

    void addAll(Iterable<T> elements);

    /**
     * @return TRUE if the element was removed
     */
    boolean remove(T element);
}
