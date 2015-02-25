package net.engio.mbassy;

import java.util.Collection;

import net.engio.mbassy.common.StrongConcurrentSet;

/**
 * Todo: Add javadoc
 *
 * @author bennidi
 *         Date: 3/29/13
 */
public class StrongConcurrentSetTest extends ConcurrentSetTest{

    @Override
    protected Collection createSet() {
        return new StrongConcurrentSet();
    }
}
