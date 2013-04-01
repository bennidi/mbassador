package net.engio.mbassy.common;

/**
 * Todo: Add javadoc
 *
 * @author bennidi
 *         Date: 3/29/13
 */
public interface ISetEntry<T> {

    T getValue();

    // not thread-safe! must be synchronized in enclosing context
    void remove();

    ISetEntry<T> next();

    void clear();
}
