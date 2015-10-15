package net.engio.mbassy;

import net.engio.mbassy.common.StrongConcurrentSet;
import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author bennidi
 *         Date: 3/29/13
 */
public class StrongConcurrentSetTest extends ConcurrentSetTest{

    protected Collection createSet() {
        return new StrongConcurrentSet();
    }


    @Test
    public void testToArray() {
        Collection<Integer> set = createSet();
        assertFalse(set.contains(1));
        set.add(1);
        set.add(3);
        set.add(5);
        Object [] asArray = set.toArray();
        // TODO: To array returns set of entries?!
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testContainsAll() {
        createSet().containsAll(new HashSet<Object>());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRemoveAll() {
        createSet().removeAll(new HashSet<Object>());
    }

    @Test(expected = UnsupportedOperationException.class)
    public void testRetainAll() {
        createSet().retainAll(new HashSet<Object>());
    }

    @Test
    public void testClear() {
        Collection set = createSet();
        assertFalse(set.contains(1));
        set.add(1);
        assertTrue(set.contains(1));
        assertEquals(1, set.size());
        set.clear();
        assertFalse(set.contains(1));
        assertEquals(0, set.size());
    }
}
