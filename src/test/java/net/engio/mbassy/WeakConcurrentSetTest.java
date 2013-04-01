package net.engio.mbassy;

import net.engio.mbassy.common.ConcurrentExecutor;
import net.engio.mbassy.common.IConcurrentSet;
import net.engio.mbassy.common.WeakConcurrentSet;
import org.junit.Test;

import java.util.HashSet;
import java.util.Random;

/**
 * Todo: Add javadoc
 *
 * @author bennidi
 *         Date: 3/29/13
 */
public class WeakConcurrentSetTest extends ConcurrentSetTest{

    @Override
    protected IConcurrentSet createSet() {
        return new WeakConcurrentSet();
    }

    //@Ignore("Currently fails when building as a suite with JDK 1.7.0_15 and Maven 3.0.5 on a Mac")
    @Test
    public void testIteratorCleanup() {

        // Assemble
        final HashSet<Object> persistingCandidates = new HashSet<Object>();
        final IConcurrentSet testSetWeak = createSet();
        final Random rand = new Random();

        for (int i = 0; i < numberOfElements; i++) {
            Object candidate = new Object();

            if (rand.nextInt() % 3 == 0) {
                persistingCandidates.add(candidate);
            }
            testSetWeak.add(candidate);
        }

        // Remove/Garbage collect all objects that have not
        // been inserted into the set of persisting candidates.
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

        assertEquals(persistingCandidates.size(), testSetWeak.size());
        for (Object test : testSetWeak) {
            assertTrue(persistingCandidates.contains(test));
        }
    }


}
