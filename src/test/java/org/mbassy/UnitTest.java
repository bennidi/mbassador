package org.mbassy;

import java.lang.ref.WeakReference;

/**
 * Created with IntelliJ IDEA.
 * User: benni
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
}
