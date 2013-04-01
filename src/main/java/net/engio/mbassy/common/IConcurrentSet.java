package net.engio.mbassy.common;

/**
 * Todo: Add javadoc
 *
 * @author bennidi
 *         Date: 3/29/13
 */
public interface IConcurrentSet<T> extends Iterable<T> {

    IConcurrentSet<T> add(T element);

    boolean contains(T element);

    int size();

    IConcurrentSet<T> addAll(Iterable<T> elements);

    boolean remove(T element);
}
