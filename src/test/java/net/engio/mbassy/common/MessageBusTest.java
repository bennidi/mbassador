package net.engio.mbassy.common;

import junit.framework.Assert;
import net.engio.mbassy.MBassador;
import net.engio.mbassy.error.IPublicationErrorHandler;
import net.engio.mbassy.error.PublicationError;
import net.engio.mbassy.messages.MessageTypes;

import org.junit.Before;

/**
 * A base test that provides a factory for message bus that makes tests fail if any
 * publication error occurs
 *
 * @author bennidi
 *         Date: 3/2/13
 */
public abstract class MessageBusTest extends AssertSupport {

    // this value probably needs to be adjusted depending on the performance of the underlying plattform
    // otherwise the tests will fail since asynchronous processing might not have finished when
    // evaluation is run
    protected static final int processingTimeInMS = 6000;
    protected static final int InstancesPerListener = 5000;
    protected static final int ConcurrentUnits = 10;
    protected static final int IterationsPerThread = 100;

    protected static final IPublicationErrorHandler TestFailingHandler = new IPublicationErrorHandler() {
        @Override
        public void handleError(PublicationError error) {
            error.getCause().printStackTrace();
            Assert.fail();
        }
    };

    private static final Object mapObject = new Object();

    @Before
    public void setUp(){
        for(MessageTypes mes : MessageTypes.values()) {
            mes.reset();
        }
    }


    public MBassador createBus() {
        MBassador bus = new MBassador().start();
        bus.addErrorHandler(TestFailingHandler);
        return bus;
    }

    public MBassador createBus(ListenerFactory listeners) {
        MBassador bus = new MBassador().start();
        bus.addErrorHandler(TestFailingHandler);
        ConcurrentExecutor.runConcurrent(TestUtil.subscriber(bus, listeners), ConcurrentUnits);
        return bus;
    }
}
