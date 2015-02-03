package net.engio.mbassy.common;

import java.util.concurrent.ConcurrentHashMap;

import junit.framework.Assert;
import net.engio.mbassy.bus.IMessagePublication;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;
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
    private ConcurrentHashMap<IMessagePublication, Object> issuedPublications = new ConcurrentHashMap<IMessagePublication, Object>();

    @Before
    public void setUp(){
        this.issuedPublications = new ConcurrentHashMap<IMessagePublication, Object>();
        for(MessageTypes mes : MessageTypes.values()) {
            mes.reset();
        }
    }

    public static IBusConfiguration SyncAsync() {
        return new BusConfiguration()
            .addFeature(Feature.SyncPubSub.Default())
            .addFeature(Feature.AsynchronousHandlerInvocation.Default())
            .addFeature(Feature.AsynchronousMessageDispatch.Default());
    }

    public MBassador createBus(IBusConfiguration configuration) {
        MBassador bus = new MBassador(configuration);
        bus.addErrorHandler(TestFailingHandler);
        return bus;
    }

    public MBassador createBus(IBusConfiguration configuration, ListenerFactory listeners) {
        MBassador bus = new MBassador(configuration);
        bus.addErrorHandler(TestFailingHandler);
        ConcurrentExecutor.runConcurrent(TestUtil.subscriber(bus, listeners), ConcurrentUnits);
        return bus;
    }

    protected void track(IMessagePublication asynchronously) {
        this.issuedPublications.put(asynchronously, mapObject);
    }

    public void waitForPublications(long timeOutInMs){
        long start = System.currentTimeMillis();
        while(this.issuedPublications.size() > 0 && System.currentTimeMillis() - start < timeOutInMs){
            for(IMessagePublication pub : this.issuedPublications.keySet()){
                if(pub.isFinished()) {
                    this.issuedPublications.remove(pub);
                }
            }
        }
        if(this.issuedPublications.size() > 0) {
            fail("Issued publications did not finish within specified timeout of " + timeOutInMs + " ms");
        }
    }

    public void addPublication(IMessagePublication publication){
        this.issuedPublications.put(publication, mapObject);
    }

}
