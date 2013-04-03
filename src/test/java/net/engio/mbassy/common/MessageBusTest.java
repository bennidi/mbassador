package net.engio.mbassy.common;

import junit.framework.Assert;
import net.engio.mbassy.IPublicationErrorHandler;
import net.engio.mbassy.PublicationError;
import net.engio.mbassy.bus.BusConfiguration;
import net.engio.mbassy.bus.ISyncMessageBus;
import net.engio.mbassy.bus.MBassador;

/**
 * A base test that provides a factory for message bus that makes tests fail if any
 * publication error occurs
 *
 * @author bennidi
 *         Date: 3/2/13
 */
public class MessageBusTest<Bus extends ISyncMessageBus> extends UnitTest {

    protected static final IPublicationErrorHandler TestFailingHandler = new IPublicationErrorHandler() {
        @Override
        public void handleError(PublicationError error) {
            Assert.fail();
        }
    };

    public MBassador getBus(BusConfiguration configuration) {
        MBassador bus = new MBassador(configuration);
        bus.addErrorHandler(TestFailingHandler);
        return bus;
    }

}
