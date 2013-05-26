package net.engio.mbassy.common;

import org.junit.Assert;

import java.lang.ref.WeakReference;

/**
 * @author bennidi
 */
public class AssertSupport {

    // Internal state
    private Runtime runtime = Runtime.getRuntime();

    public void pause(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void pause() {
        pause(10);
    }


    public void runGC() {
        WeakReference ref = new WeakReference<Object>(new Object());
        pause(100);
        while(ref.get() != null) {
            pause(10);
            runtime.gc();
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
