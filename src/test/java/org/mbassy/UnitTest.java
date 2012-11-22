package org.mbassy;

import org.junit.Assert;

import java.lang.ref.WeakReference;

/**
 * Created with IntelliJ IDEA.
 * @author bennidi
 * Date: 11/12/12
 * Time: 3:16 PM
 * To change this template use File | Settings | File Templates.
 */
public class UnitTest {


    public void pause(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void pause() {
        pause(10);
    }


    public void runGC() {
        WeakReference ref = new WeakReference<Object>(new Object());
        while(ref.get() != null) {
            System.gc();
        }
    }

    public void fail(String message) {
        Assert.fail(message);
    }

    public void fail() {
        Assert.fail();
    }

    public void assertTrue(Boolean condition) {
        Assert.assertTrue(condition);
    }

    public void assertTrue(String message, Boolean condition) {
        Assert.assertTrue(message, condition);
    }

    public void assertFalse(Boolean condition) {
        Assert.assertFalse(condition);
    }

    public void assertNull(Object object) {
        Assert.assertNull(object);
    }

    public void assertNotNull(Object object) {
        Assert.assertNotNull(object);
    }

    public void assertFalse(String message, Boolean condition) {
        Assert.assertFalse(message, condition);
    }

    public void assertEquals(Object expected, Object actual) {
        Assert.assertEquals(expected, actual);
    }
}
