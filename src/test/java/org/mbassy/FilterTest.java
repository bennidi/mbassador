package org.mbassy;

import org.junit.Test;
import org.mbassy.events.SubTestEvent;
import org.mbassy.events.TestEvent;
import org.mbassy.listener.Filter;
import org.mbassy.listener.IMessageFilter;
import org.mbassy.listener.Listener;
import org.mbassy.listeners.*;

import java.util.List;

/**
 * Testing of filter functionality
 *
 * @author bennidi
 *         Date: 11/26/12
 */
public class FilterTest extends UnitTest{

    @Test
    public void testSubclassFilter() throws Exception {

        MBassador bus = new MBassador(new BusConfiguration());
        ListenerFactory listenerFactory = new ListenerFactory()
                .create(100, FilteredMessageListener.class)
                .create(100, Object.class)
                .create(100, NonListeningBean.class);

        List<Object> listeners = listenerFactory.build();

        // this will subscribe the listeners concurrently to the bus
        TestUtil.setup(bus, listeners, 10);

        TestEvent event = new TestEvent();
        TestEvent subTestEvent = new SubTestEvent();

        bus.post(event).now();
        bus.post(subTestEvent).now();

        assertEquals(100, event.counter.get());
        assertEquals(0, subTestEvent.counter.get());

    }


    public static class FilteredMessageListener{

        @Listener(filters = {@Filter(IMessageFilter.DontAllowSubtypes.class)})
        public void handleTestEvent(TestEvent event){
            event.counter.incrementAndGet();
        }


    }

}
