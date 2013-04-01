package net.engio.mbassy;

import net.engio.mbassy.common.IConcurrentSet;
import net.engio.mbassy.common.StrongConcurrentSet;

/**
 * Todo: Add javadoc
 *
 * @author bennidi
 *         Date: 3/29/13
 */
public class StrongConcurrentSetTest extends ConcurrentSetTest{

    @Override
    protected IConcurrentSet createSet() {
        return new StrongConcurrentSet();
    }
}
