package net.engio.mbassy;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

/**
 * Test suite for running all available unit tests
 *
 * @author bennidi
 *         Date: 11/23/12
 */
@RunWith(Suite.class)
@Suite.SuiteClasses({
        AsyncFIFOBusTest.class,
        ConditionalHandlerTest.class,
        CustomHandlerAnnotationTest.class,
        DeadMessageTest.class,
        FilterTest.class,
        MetadataReaderTest.class,
        MethodDispatchTest.class,
        StrongConcurrentSetTest.class,
        SubscriptionManagerTest.class,
        SyncAsyncTest.class,
        SyncBusTest.MBassadorTest.class,
        SyncBusTest.SyncMessageBusTest.class,
        SynchronizedHandlerTest.class,
        WeakConcurrentSetTest.class
})
public class AllTests {
}
