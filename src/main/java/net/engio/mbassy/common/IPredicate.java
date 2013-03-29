package net.engio.mbassy.common;

/**
 * Created with IntelliJ IDEA.
 *
 * @author bennidi
 *         Date: 10/22/12
 *         Time: 9:33 AM
 *         To change this template use File | Settings | File Templates.
 */
public interface IPredicate<T> {

    boolean apply(T target);
}
