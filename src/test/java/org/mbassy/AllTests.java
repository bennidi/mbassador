package org.mbassy;

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
        MBassadorTest.class,
        FilterTest.class,
        MetadataReaderTest.class
})
public class AllTests {
}
