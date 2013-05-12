package net.engio.mbassy;

import junit.framework.Assert;
import net.engio.mbassy.common.ConcurrentExecutor;
import net.engio.mbassy.common.IConcurrentSet;
import net.engio.mbassy.common.UnitTest;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;

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
public abstract class ConcurrentSetTest extends UnitTest {

    // Shared state
    protected final int numberOfElements = 100000;
    protected final int numberOfThreads = 50;
    
    
    protected abstract IConcurrentSet createSet();


    @Test
    public void testUniqueness() {
        final LinkedList<Object> duplicates = new LinkedList<Object>();
        final HashSet<Object> distinct = new HashSet<Object>();

        final IConcurrentSet testSetWeak = createSet();
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
                    testSetWeak.add(src);
                }
            }
        }, numberOfThreads);

        // check that the control set and the test set contain the exact same elements
        assertEquals(distinct.size(), testSetWeak.size());
        for (Object uniqueObject : distinct) {
            assertTrue(testSetWeak.contains(uniqueObject));
        }
    }

    @Test
    public void testPerformance() {
        final HashSet<Object> source = new HashSet<Object>();

        final HashSet<Object> hashSet = new HashSet<Object>();

        final IConcurrentSet weakConcurrentSet = createSet();

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
            weakConcurrentSet.add(o);
        }
        duration = System.currentTimeMillis() - start;
        System.out.println("Performance of ConcurrentSet for 1.000.000 object insertions " + duration);
    }


    @Test
    public void testRemove2() {
        final HashSet<Object> source = new HashSet<Object>();
        final HashSet<Object> toRemove = new HashSet<Object>();

        final IConcurrentSet testSetWeak = createSet();
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
                    testSetWeak.add(src);
                }
            }
        }, numberOfThreads);

        // remove all candidates that have previously been marked for removal from the test set
        ConcurrentExecutor.runConcurrent(new Runnable() {
            @Override
            public void run() {
                for (Object src : toRemove) {
                    testSetWeak.remove(src);
                }
            }
        }, numberOfThreads);

        // ensure that the test set does not contain any of the elements that have been removed from it
        for (Object tar : testSetWeak) {
            Assert.assertTrue(!toRemove.contains(tar));
        }
        // ensure that the test set still contains all objects from the source set that have not been marked
        // for removal
        assertEquals(source.size() - toRemove.size(), testSetWeak.size());
        for (Object src : source) {
            if (!toRemove.contains(src)) assertTrue(testSetWeak.contains(src));
        }
    }

    @Test
    public void testRemoval() {
        final HashSet<Object> source = new HashSet<Object>();
        final HashSet<Object> toRemove = new HashSet<Object>();

        final IConcurrentSet testSetWeak = createSet();
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
                    testSetWeak.add(src);
                    if (toRemove.contains(src))
                        testSetWeak.remove(src);
                }
            }
        }, numberOfThreads);

        // ensure that the test set does not contain any of the elements that have been removed from it
        for (Object tar : testSetWeak) {
            Assert.assertTrue(!toRemove.contains(tar));
        }
        // ensure that the test set still contains all objects from the source set that have not been marked
        // for removal
        assertEquals(source.size() - toRemove.size(), testSetWeak.size());
        for (Object src : source) {
            if (!toRemove.contains(src)) assertTrue(testSetWeak.contains(src));
        }
    }

    @Test
    public void testCompleteRemoval() {
        final HashSet<Object> source = new HashSet<Object>();
        final IConcurrentSet testSetWeak = createSet();

        // build set of candidates and mark subset for removal
        for (int i = 0; i < numberOfElements; i++) {
            Object candidate = new Object();
            source.add(candidate);
            testSetWeak.add(candidate);
        }

        // build test set by adding the candidates
        // and subsequently removing those marked for removal
        ConcurrentExecutor.runConcurrent(new Runnable() {
            @Override
            public void run() {
                for (Object src : source) {
                    testSetWeak.remove(src);
                }
            }
        }, numberOfThreads);


        // ensure that the test set still contains all objects from the source set that have not been marked
        // for removal
        assertEquals(0, testSetWeak.size());
        for(Object src : source){
            assertFalse(testSetWeak.contains(src));
        }
    }

    @Test
    public void testRemovalViaIterator() {
        final HashSet<Object> source = new HashSet<Object>();
        final IConcurrentSet setUnderTest = createSet();

        // build set of candidates and mark subset for removal
        for (int i = 0; i < numberOfElements; i++) {
            Object candidate = new Object();
            source.add(candidate);
            setUnderTest.add(candidate);
        }

        // build test set by adding the candidates
        // and subsequently removing those marked for removal
        ConcurrentExecutor.runConcurrent(new Runnable() {
            @Override
            public void run() {
                Iterator<Object> iterator = setUnderTest.iterator();
                while(iterator.hasNext()){
                    iterator.remove();
                }
            }
        }, numberOfThreads);


        // ensure that the test set still contains all objects from the source set that have not been marked
        // for removal
        assertEquals(0, setUnderTest.size());
        for(Object src : source){
            assertFalse(setUnderTest.contains(src));
        }
    }


    /**
     * In this test HashMap will cross capacity threshold multiple times in
     * different directions which will trigger rehashing. Because rehashing
     * requires modification of Entry class for all hash map entries some keys
     * may temporarily disappear from the map.
     * <p>
     * For more information please take a look at transfer method in HashMap.
     *
     * Thanks to Ivan Koblik (http://koblik.blogspot.com) for contributing initial code and idea
     */
    @Test
    public void testConcurrentAddRemove() {
        final IConcurrentSet set = createSet();
        final List permanentObjects = createWithRandomIntegers(80, null);
        final List volatileObjects = createWithRandomIntegers(10000, permanentObjects);
        final CopyOnWriteArraySet missing = new CopyOnWriteArraySet();
        final int mutatorThreshold = 1000;

        // Add elements that will not be touched by the constantly running mutating thread
        final int numItems = 8;
        for (Object permanent : permanentObjects) {
            set.add(permanent);
        }

        // Adds and removes items >= numItems
        // thus forcing constant rehashing of the backing hashtable
        Runnable updatingThread = new Runnable() {
            public void run() {
                Random rand = new Random();
                for(int times = 0; times < 1000 ; times++){
                    System.out.println("New mutator cycle: " + times);
                    HashSet elements = new HashSet(mutatorThreshold);

                    for (int i = numItems; i < mutatorThreshold; i++) {
                        Object volatileObject = volatileObjects.get(Math.abs(rand.nextInt()) % volatileObjects.size());
                        set.add(volatileObject);
                        elements.add(volatileObject);
                    }
                    for (Object volObj : elements) {
                        set.remove(volObj);
                    }
                }
            };
        };

        Runnable lookupThread = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10000; i++) {
                    System.out.println("New lookup cycle: " + i);
                    for (Object permanent : permanentObjects) {
                        // permanent items are never touched,
                        // --> set.contains(j) should always return true
                        if(!set.contains(permanent))
                            missing.add(permanent);
                    }
                }
            }
        };

        ConcurrentExecutor.runConcurrent(updatingThread, lookupThread, lookupThread, lookupThread);
        assertTrue("There where items temporarily unavailable: " + missing.size(), missing.size() == 0);

    }


    public List createWithRandomIntegers(int size, List<Integer> exluding){
        if(exluding == null) exluding = new ArrayList<Integer>();
        List<Integer> result = new ArrayList<Integer>(size);
        Random rand = new Random();
        for(int i = 0; i < size;i++){
            result.add(rand.nextInt());
        }
        for(Integer excluded : exluding)
            result.remove(excluded);
        return result;
    }

}
