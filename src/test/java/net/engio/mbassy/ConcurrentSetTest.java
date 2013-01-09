package net.engio.mbassy;

import junit.framework.Assert;
import org.junit.Test;
import net.engio.mbassy.common.ConcurrentExecutor;
import net.engio.mbassy.common.ConcurrentSet;
import net.engio.mbassy.common.UnitTest;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;

/**
 * This test ensures the correct behaviour of the set implementation that is the building
 * block of the subscription implementations used by the Mbassador message bus.
 * <p/>
 * It should behave exactly like other set implementations do and as such all tests are based
 * on comparing the outcome of sequence of operations applied to a standard set implementation
 * and the concurrent set.
 *
 * @author bennidi
 *         Date: 11/12/12
 */
public class ConcurrentSetTest extends UnitTest {

    private int numberOfElements = 100000;

    private int numberOfThreads = 50;


    @Test
    public void testIteratorCleanup() {
        final HashSet<Object> persistingCandidates = new HashSet<Object>();
        final ConcurrentSet<Object> testSet = new ConcurrentSet<Object>();
        Random rand = new Random();

        for (int i = 0; i < numberOfElements; i++) {
            Object candidate = new Object();

            if (rand.nextInt() % 3 == 0) {
                persistingCandidates.add(candidate);
            }
            testSet.add(candidate);
        }

        // this will remove all objects that have not been inserted into the set of persisting candidates
        runGC();

        ConcurrentExecutor.runConcurrent(new Runnable() {
            @Override
            public void run() {
                for (Object testObject : testSet) {
                    // do nothing
                    // just iterate to trigger automatic clean up
                    System.currentTimeMillis();
                }
            }
        }, numberOfThreads);

        assertEquals(persistingCandidates.size(), testSet.size());
        for (Object test : testSet) {
            assertTrue(persistingCandidates.contains(test));
        }


    }


    @Test
    public void testUniqueness() {
        final LinkedList<Object> duplicates = new LinkedList<Object>();
        final HashSet<Object> distinct = new HashSet<Object>();

        final ConcurrentSet<Object> testSet = new ConcurrentSet<Object>();
        Random rand = new Random();

        // build set of distinct objects and list of duplicates
        Object candidate = new Object();
        for (int i = 0; i < numberOfElements; i++) {
            if (rand.nextInt() % 3 == 0) {
                candidate = new Object();
            }
            duplicates.add(candidate);
            distinct.add(candidate);
        }

        // insert all elements (containing duplicates) into the set
        ConcurrentExecutor.runConcurrent(new Runnable() {
            @Override
            public void run() {
                for (Object src : duplicates) {
                    testSet.add(src);
                }
            }
        }, numberOfThreads);

        // check that the control set and the test set contain the exact same elements
        assertEquals(distinct.size(), testSet.size());
        for (Object uniqueObject : distinct) {
            assertTrue(testSet.contains(uniqueObject));
        }


    }

    @Test
    public void testPerformance() {
        final HashSet<Object> source = new HashSet<Object>();

        final HashSet<Object> hashSet = new HashSet<Object>();

        final ConcurrentSet<Object> concurrentSet = new ConcurrentSet<Object>();

        for (int i = 0; i < 1000000; i++) {
            source.add(new Object());
        }


        long start = System.currentTimeMillis();
        for (Object o : source) {
            hashSet.add(o);
        }
        long duration = System.currentTimeMillis() - start;
        System.out.println("Performance of HashSet for 1.000.000 object insertions " + duration);

        start = System.currentTimeMillis();
        for (Object o : source) {
            concurrentSet.add(o);
        }
        duration = System.currentTimeMillis() - start;
        System.out.println("Performance of ConcurrentSet for 1.000.000 object insertions " + duration);
    }


    @Test
    public void testRemove2() {
        final HashSet<Object> source = new HashSet<Object>();
        final HashSet<Object> toRemove = new HashSet<Object>();

        final ConcurrentSet<Object> testSet = new ConcurrentSet<Object>();
        // build set of distinct objects and mark a subset of those for removal
        for (int i = 0; i < numberOfElements; i++) {
            Object candidate = new Object();
            source.add(candidate);
            if (i % 3 == 0) {
                toRemove.add(candidate);
            }
        }

        // build the test set from the set of candidates
        ConcurrentExecutor.runConcurrent(new Runnable() {
            @Override
            public void run() {
                for (Object src : source) {
                    testSet.add(src);
                }
            }
        }, numberOfThreads);

        // remove all candidates that have previously been marked for removal from the test set
        ConcurrentExecutor.runConcurrent(new Runnable() {
            @Override
            public void run() {
                for (Object src : toRemove) {
                    testSet.remove(src);
                }
            }
        }, numberOfThreads);

        // ensure that the test set does not contain any of the elements that have been removed from it
        for (Object tar : testSet) {
            Assert.assertTrue(!toRemove.contains(tar));
        }
        // ensure that the test set still contains all objects from the source set that have not been marked
        // for removal
        assertEquals(source.size() - toRemove.size(), testSet.size());
        for (Object src : source) {
            if (!toRemove.contains(src)) assertTrue(testSet.contains(src));
        }
    }

    @Test
    public void testRemoval() {
        final HashSet<Object> source = new HashSet<Object>();
        final HashSet<Object> toRemove = new HashSet<Object>();

        final ConcurrentSet<Object> testSet = new ConcurrentSet<Object>();
        // build set of candidates and mark subset for removal
        for (int i = 0; i < numberOfElements; i++) {
            Object candidate = new Object();
            source.add(candidate);
            if (i % 3 == 0) {
                toRemove.add(candidate);
            }
        }

        // build test set by adding the candidates
        // and subsequently removing those marked for removal
        ConcurrentExecutor.runConcurrent(new Runnable() {
            @Override
            public void run() {
                for (Object src : source) {
                    testSet.add(src);
                    if (toRemove.contains(src))
                        testSet.remove(src);
                }
            }
        }, numberOfThreads);

        // ensure that the test set does not contain any of the elements that have been removed from it
        for (Object tar : testSet) {
            Assert.assertTrue(!toRemove.contains(tar));
        }
        // ensure that the test set still contains all objects from the source set that have not been marked
        // for removal
        assertEquals(source.size() - toRemove.size(), testSet.size());
        for (Object src : source) {
            if (!toRemove.contains(src)) assertTrue(testSet.contains(src));
        }
    }

    @Test
    public void testCompleteRemoval() {
        final HashSet<Object> source = new HashSet<Object>();
        final ConcurrentSet<Object> testSet = new ConcurrentSet<Object>();

        // build set of candidates and mark subset for removal
        for (int i = 0; i < numberOfElements; i++) {
            Object candidate = new Object();
            source.add(candidate);
            testSet.add(candidate);
        }

        // build test set by adding the candidates
        // and subsequently removing those marked for removal
        ConcurrentExecutor.runConcurrent(new Runnable() {
            @Override
            public void run() {
                for (Object src : source) {
                    testSet.remove(src);
                }
            }
        }, numberOfThreads);


        // ensure that the test set still contains all objects from the source set that have not been marked
        // for removal
        assertEquals(0, testSet.size());
        for(Object src : source){
            assertFalse(testSet.contains(src));
        }
    }

    @Test
    public void testRemovalViaIterator() {
        final HashSet<Object> source = new HashSet<Object>();
        final ConcurrentSet<Object> testSet = new ConcurrentSet<Object>();

        // build set of candidates and mark subset for removal
        for (int i = 0; i < numberOfElements; i++) {
            Object candidate = new Object();
            source.add(candidate);
            testSet.add(candidate);
        }

        // build test set by adding the candidates
        // and subsequently removing those marked for removal
        ConcurrentExecutor.runConcurrent(new Runnable() {
            @Override
            public void run() {
                Iterator<Object> iterator = testSet.iterator();
                while(iterator.hasNext()){
                    iterator.remove();
                }
            }
        }, numberOfThreads);


        // ensure that the test set still contains all objects from the source set that have not been marked
        // for removal
        assertEquals(0, testSet.size());
        for(Object src : source){
            assertFalse(testSet.contains(src));
        }
    }


}
