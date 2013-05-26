package net.engio.mbassy;

import net.engio.mbassy.common.ConcurrentExecutor;
import net.engio.mbassy.common.IConcurrentSet;
import net.engio.mbassy.common.WeakConcurrentSet;
import org.junit.Test;

import java.util.HashSet;
import java.util.Random;

/**
 *
 *
 * @author bennidi
 *         Date: 3/29/13
 */
public class WeakConcurrentSetTest extends ConcurrentSetTest{

    @Override
    protected IConcurrentSet createSet() {
        return new WeakConcurrentSet();
    }

    @Test
    public void testIteratorCleanup() {

        // Assemble
        final HashSet<Object> permanentElements = new HashSet<Object>();
        final IConcurrentSet testSetWeak = createSet();
        final Random rand = new Random();

        for (int i = 0; i < numberOfElements; i++) {
            Object candidate = new Object();

            if (rand.nextInt() % 3 == 0) {
                permanentElements.add(candidate);
            }
            testSetWeak.add(candidate);
        }

        // Remove/Garbage collect all objects that have not
        // been inserted into the set of permanent candidates.
        runGC();

        ConcurrentExecutor.runConcurrent(new Runnable() {
            @Override
            public void run() {
                for (Object testObject : testSetWeak) {
                    // do nothing
                    // just iterate to trigger automatic clean up
                    System.currentTimeMillis();
                }
            }
        }, numberOfThreads);

        // the set should have cleaned up the garbage collected elements
        // it must still contain all of the permanent objects
        // since different GC mechanisms can be used (not necessarily full, stop-the-world) not all dead objects
        // must have been collected
        assertTrue(permanentElements.size() <= testSetWeak.size() && testSetWeak.size() < numberOfElements);
        for (Object test : testSetWeak) {
            assertTrue(permanentElements.contains(test));
        }
    }


}
