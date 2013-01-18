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
        ConcurrentSetTest.class,
        MessagePublicationTest.class,
        FilterTest.class,
        MetadataReaderTest.class,
        ListenerSubscriptionTest.class,
        MethodDispatchTest.class,
        DeadEventTest.class
})
public class AllTests {
}
