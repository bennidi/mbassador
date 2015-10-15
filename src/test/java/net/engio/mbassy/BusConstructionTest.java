package net.engio.mbassy;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.ConfigurationError;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.common.AssertSupport;
import org.junit.Test;

/**
 * Testing different ways of construction
 *
 * @author bennidi
 *         Date: 13.09.15
 */
public class BusConstructionTest extends AssertSupport {

    @Test
    public void testMBassadorDefaultConstructor(){
        assertNotNull(new MBassador());
    }

    @Test(expected = ConfigurationError.class)
    public void testEmptyBusConfiguration(){
        new MBassador(new BusConfiguration());
    }


    @Test(expected = ConfigurationError.class)
    public void testMissingMessageDispatch(){
        assertNotNull(new MBassador(new BusConfiguration()
                .addFeature(Feature.SyncPubSub.Default())
                .addFeature(Feature.AsynchronousHandlerInvocation.Default())
                .addPublicationErrorHandler(new IPublicationErrorHandler.ConsoleLogger())
                .setProperty(IBusConfiguration.Properties.BusId, "global bus")));
    }

    @Test(expected = ConfigurationError.class)
    public void testMissingPubSub(){
        assertNotNull(new MBassador(new BusConfiguration()
                .addFeature(Feature.AsynchronousHandlerInvocation.Default())
                .addFeature(Feature.AsynchronousMessageDispatch.Default())
                .addPublicationErrorHandler(new IPublicationErrorHandler.ConsoleLogger())
                .setProperty(IBusConfiguration.Properties.BusId, "global bus")));
    }

    @Test(expected = ConfigurationError.class)
    public void testMissingAsyncInvocation(){
        assertNotNull(new MBassador(new BusConfiguration()
                .addFeature(Feature.SyncPubSub.Default())
                .addFeature(Feature.AsynchronousMessageDispatch.Default())
                .addPublicationErrorHandler(new IPublicationErrorHandler.ConsoleLogger())
                .setProperty(IBusConfiguration.Properties.BusId, "global bus")));
    }


    @Test
    public void testValidBusConfiguration(){
        MBassador bus = new MBassador(new BusConfiguration()
                .addFeature(Feature.SyncPubSub.Default())
                .addFeature(Feature.AsynchronousHandlerInvocation.Default())
                .addFeature(Feature.AsynchronousMessageDispatch.Default())
                .addPublicationErrorHandler(new IPublicationErrorHandler.ConsoleLogger())
                .setProperty(IBusConfiguration.Properties.BusId, "global bus"));
        assertNotNull(bus);
        assertEquals(1, bus.getRegisteredErrorHandlers().size());
        System.out.println(bus.toString());
        assertTrue(bus.toString().contains("global bus"));
    }





}
