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
        StrongConcurrentSetTest.class,
        WeakConcurrentSetTest.class,
        SyncAsyncTest.class,
        SyncBusTest.MBassadorTest.class,
        SyncBusTest.SyncMessageBusTest.class,
        FilterTest.class,
        MetadataReaderTest.class,
        MethodDispatchTest.class,
        DeadMessageTest.class,
        SynchronizedHandlerTest.class,
        SubscriptionManagerTest.class,
        AsyncFIFOBusTest.class,
        ConditionalHandlerTest.class
})
public class AllTests {
}
