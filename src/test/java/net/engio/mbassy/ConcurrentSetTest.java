package net.engio.mbassy;

import junit.framework.Assert;
import net.engio.mbassy.common.AssertSupport;
import net.engio.mbassy.common.ConcurrentExecutor;
import org.junit.Before;
import org.junit.Test;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicInteger;

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
public abstract class ConcurrentSetTest extends AssertSupport {

    // Shared state
    protected final int numberOfElements = 100000;
    protected final int numberOfThreads = 50;

    // needed to avoid premature garbage collection for weakly referenced listeners
    protected Set gcProtector = new HashSet();

    @Before
    public void beforeTest(){
        super.beforeTest();
        gcProtector = new HashSet();
    }

    protected abstract Collection createSet();


    @Test
    public void testAddAll() {
        final Collection testSet = createSet();
        final List<Number> notFound = new CopyOnWriteArrayList<Number>();
        // insert all elements (containing duplicates) into the set
        final Random rand = new Random();
        ConcurrentExecutor.runConcurrent(new Runnable() {
            @Override
            public void run() {
                final List<Number> source = new LinkedList<Number>();
                for (int i = 0; i < numberOfElements; i++) {
                    source.add(rand.nextDouble());
                }
                testSet.addAll(source);
                for (Number src : source) {
                    if(!testSet.contains(src)){
                        notFound.add(src);
                    }
                }
            }
        }, numberOfThreads);

        // check that the control set and the test set contain the exact same elements
        assertEquals(notFound.size(), 0);
    }

    @Test
    public void testAdd() {
        final Collection testSet = createSet();
        final List<Number> notFound = new CopyOnWriteArrayList<Number>();
        final Random rand = new Random();
        // insert all elements (containing duplicates) into the set
        ConcurrentExecutor.runConcurrent(new Runnable() {
            @Override
            public void run() {
                final List<Number> source = new LinkedList<Number>();
                for (int i = 0; i < numberOfElements; i++) {
                    source.add(rand.nextDouble());
                }
                for (Number src : source) {
                    testSet.add(src);
                    if(!testSet.contains(src)){
                        notFound.add(src);
                    }
                }
            }
        }, numberOfThreads);

        // check that the control set and the test set contain the exact same elements
        assertEquals(notFound.size(), 0);
    }


    @Test
    public void testUniqueness() {
        final LinkedList<Object> duplicates = new LinkedList<Object>();
        final HashSet<Object> distinct = new HashSet<Object>();

        final Collection testSet = createSet();
        Random rand = new Random();

        // getAll set of distinct objects and list of duplicates
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

    @Test()
    public void testIterationWithConcurrentRemoval() {
        final Collection<AtomicInteger> testSet = createSet();
        final Random rand = new Random();

        for (int i = 0; i < numberOfElements; i++) {
            AtomicInteger element = new AtomicInteger();
            testSet.add(element);
            gcProtector.add(element);
        }

        Runnable incrementer = new Runnable() {
            @Override
            public void run() {
                while(testSet.size() > 100){
                    for(AtomicInteger element : testSet)
                        element.incrementAndGet();
                }

            }
        };

        Runnable remover = new Runnable() {
            @Override
            public void run() {
                while(testSet.size() > 100){
                    for(AtomicInteger element : testSet)
                        if(rand.nextInt() % 3 == 0 && testSet.size() > 100)
                            testSet.remove(element);
                }
            }
        };

        ConcurrentExecutor.runConcurrent(20, incrementer, incrementer, remover);

        Set<Integer> counts = new HashSet<Integer>();
        for (AtomicInteger count : testSet) {
            counts.add(count.get());
        }
        // all atomic integers should have been visited by the the incrementer
        // the same number of times
        // in other words: they have either been removed at some point or incremented in each
        // iteration such that all remaining atomic integers must share the same value
        assertEquals(1, counts.size());
    }



    @Test
    public void testRandomRemoval() {
        final HashSet<Object> source = new HashSet<Object>();
        final HashSet<Object> toRemove = new HashSet<Object>();

        final Collection testSet = createSet();
        // getAll set of distinct objects and mark a subset of those for removal
        for (int i = 0; i < numberOfElements; i++) {
            Object candidate = new Object();
            source.add(candidate);
            if (i % 3 == 0) {
                toRemove.add(candidate);
            }
        }

        // getAll the test set from the set of candidates
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
            assertTrue(!toRemove.contains(tar));
        }
        // ensure that the test set still contains all objects from the source set that have not been marked
        // for removal
        assertEquals(source.size() - toRemove.size(), testSet.size());
        for (Object src : source) {
            if (!toRemove.contains(src)) assertTrue(testSet.contains(src));
        }
    }

    @Test
    public void testRemovalOfHead() {
        final HashSet<Object> source = new HashSet<Object>();
        final HashSet<Object> toRemove = new HashSet<Object>();

        final Collection testSet = createSet();
        // getAll set of candidates and mark subset for removal
        for (int i = 0; i < numberOfElements; i++) {
            Object candidate = new Object();
            source.add(candidate);
            if (i % 3 == 0) {
                toRemove.add(candidate);
            }
        }

        // getAll test set by adding the candidates
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
        final Collection testSet = createSet();

        // getAll set of candidates and mark subset for removal
        for (int i = 0; i < numberOfElements; i++) {
            Object candidate = new Object();
            source.add(candidate);
            testSet.add(candidate);
        }

        // getAll test set by adding the candidates
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
        final Collection setUnderTest = createSet();

        // getAll set of candidates and mark subset for removal
        for (int i = 0; i < numberOfElements; i++) {
            Object candidate = new Object();
            source.add(candidate);
            setUnderTest.add(candidate);
        }

        // getAll test set by adding the candidates
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
        final Collection testSet = createSet();
        // a set of unique integers that will stay permanently in the test set
        final List permanentObjects = new ArrayList();
        // a set of objects that will be added and removed at random to the test set to force rehashing
        final List volatileObjects = new ArrayList();
        permanentObjects.addAll(createWithRandomIntegers(80, null));
        volatileObjects.addAll(createWithRandomIntegers(10000, permanentObjects));
        final CopyOnWriteArraySet missing = new CopyOnWriteArraySet();
        final int mutatorThreshold = 1000;

        // Add elements that will not be touched by the constantly running mutating thread
        for (Object permanent : permanentObjects) {
            testSet.add(permanent);
        }

        // Adds and removes items
        // thus forcing constant rehashing of the backing hashtable
        Runnable rehasher = new Runnable() {
            public void run() {
                Random rand = new Random();
                for(int times = 0; times < 1000 ; times++){
                    HashSet elements = new HashSet(mutatorThreshold);

                    for (int i = 0; i < mutatorThreshold; i++) {
                        Object volatileObject = volatileObjects.get(Math.abs(rand.nextInt()) % volatileObjects.size());
                        testSet.add(volatileObject);
                        elements.add(volatileObject);
                    }
                    for (Object volObj : elements) {
                        testSet.remove(volObj);
                    }
                }
            };
        };

        Runnable lookup = new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < 10000; i++) {
                    for (Object permanent : permanentObjects) {
                        // permanent items are never touched,
                        // --> set.contains(j) should always return true
                        if(!testSet.contains(permanent))
                            missing.add(permanent);
                    }
                }
            }
        };

        ConcurrentExecutor.runConcurrent(rehasher, lookup, lookup, lookup);
        assertTrue("There where items temporarily unavailable: " + missing.size(), missing.size() == 0);

    }


    public Set createWithRandomIntegers(int size, List<Integer> excluding){
        if(excluding == null) excluding = new ArrayList<Integer>();
        Set<Integer> result = new HashSet<Integer>(size);
        Random rand = new Random();
        while(result.size() < size){
            result.add(rand.nextInt());
        }
        for(Integer excluded : excluding)
            result.remove(excluded);
        return result;
    }
}
