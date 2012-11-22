package org.mbassy;

import junit.framework.Assert;
import org.junit.Test;
import org.mbassy.common.ConcurrentSet;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

/**
 * Created with IntelliJ IDEA.
 * @author bennidi
 * Date: 11/12/12
 * Time: 3:02 PM
 * To change this template use File | Settings | File Templates.
 */
public class ConcurrentSetTest extends  UnitTest{

    private int numberOfElements = 100000;

    private int numberOfThreads = 50;


    @Test
    public void testIterator(){
        final HashSet<Object> distinct = new HashSet<Object>();

        final ConcurrentSet<Object> target = new ConcurrentSet<Object>();
        Random rand = new Random();

        for(int i=0;i < numberOfElements ; i++){
            Object candidate = new Object();

            if(rand.nextInt() % 3 == 0){
                distinct.add(candidate);
            }
            target.add(candidate);
        }

        runGC();

        ConcurrentExecutor.runConcurrent(new Runnable() {
            @Override
            public void run() {
                for(Object src : target){
                    // do nothing
                    // just iterate to trigger automatic clean up
                    System.currentTimeMillis();
                }
            }
        }, numberOfThreads);




        for(Object tar : target){
            Assert.assertTrue(distinct.contains(tar));
        }



    }


        @Test
    public void testInsert(){
        final LinkedList<Object> duplicates = new LinkedList<Object>();
        final HashSet<Object> distinct = new HashSet<Object>();

        final ConcurrentSet<Object> target = new ConcurrentSet<Object>();
        Random rand = new Random();

        Object candidate = new Object();
        for(int i=0;i < numberOfElements ; i++){
            if(rand.nextInt() % 3 == 0){
                candidate = new Object();
            }
            duplicates.add(candidate);
            distinct.add(candidate);
        }


        ConcurrentExecutor.runConcurrent(new Runnable() {
            @Override
            public void run() {
                for(Object src : duplicates){
                    target.add(src);
                }
            }
        }, numberOfThreads);

        pause(3000);


        for(Object tar : target){
            Assert.assertTrue(distinct.contains(tar));
        }

        for(Object src : distinct){
            Assert.assertTrue(target.contains(src));
        }

        Assert.assertEquals(distinct.size(), target.size());
    }



    @Test
    public void testRemove1(){
        final HashSet<Object> source = new HashSet<Object>();
        final HashSet<Object> toRemove = new HashSet<Object>();

        final ConcurrentSet<Object> target = new ConcurrentSet<Object>();
        for(int i=0;i < numberOfElements ; i++){
            Object candidate = new Object();
            source.add(candidate);
            if(i % 3 == 0){
                toRemove.add(candidate);
            }
        }


        ConcurrentExecutor.runConcurrent(new Runnable() {
            @Override
            public void run() {
                for(Object src : source){
                    target.add(src);
                }
            }
        }, numberOfThreads);

        ConcurrentExecutor.runConcurrent(new Runnable() {
            @Override
            public void run() {
                for(Object src : toRemove){
                    target.remove(src);
                }
            }
        }, numberOfThreads);

        pause(3000);

        for(Object tar : target){
            Assert.assertTrue(!toRemove.contains(tar));
        }

        for(Object src : source){
            if(!toRemove.contains(src))Assert.assertTrue(target.contains(src));
        }
    }

    @Test
    public void testRemove2(){
        final HashSet<Object> source = new HashSet<Object>();
        final HashSet<Object> toRemove = new HashSet<Object>();

        final ConcurrentSet<Object> target = new ConcurrentSet<Object>();
        for(int i=0;i < numberOfElements ; i++){
            Object candidate = new Object();
            source.add(candidate);
            if(i % 3 == 0){
                toRemove.add(candidate);
            }
        }


        ConcurrentExecutor.runConcurrent(new Runnable() {
            @Override
            public void run() {
                for(Object src : source){
                    target.add(src);
                    if(toRemove.contains(src))
                        target.remove(src);
                }
            }
        }, numberOfThreads);

        pause(3000);

        for(Object tar : target){
            Assert.assertTrue(!toRemove.contains(tar));
        }

        for(Object src : source){
            if(!toRemove.contains(src))Assert.assertTrue(target.contains(src));
        }
    }

}
