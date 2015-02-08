package net.engio.mbassy.common;

import java.lang.ref.WeakReference;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bennidi
 */
public class AssertSupport {

    private Runtime runtime = Runtime.getRuntime();
    protected Logger logger = LoggerFactory.getLogger(getClass().getSimpleName());
    private volatile long testExecutionStart;

    @Rule
    public TestName name = new TestName();


    @Before
    public void beforeTest(){
        this.logger.info("Running test " + getTestName());
        this.testExecutionStart = System.currentTimeMillis();
    }

    @After
    public void afterTest(){
        this.logger.info(String.format("Finished " + getTestName() + ": " + (System.currentTimeMillis() - this.testExecutionStart) + " ms"));
    }


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

    public String getTestName(){
        return getClass().getSimpleName() + "." + this.name.getMethodName();
    }

    public void runGC() {
        WeakReference ref = new WeakReference<Object>(new Object());
        while(ref.get() != null) {
            this.runtime.gc();
            pause();
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
