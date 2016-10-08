package net.engio.mbassy.bus;

import junit.framework.Assert;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;
import net.engio.mbassy.common.MessageBusTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.concurrent.atomic.AtomicInteger;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Added for changes proposed under https://github.com/bennidi/mbassador/issues/106
 * <p/>
 * Created by David Sowerby on 13/04/15.
 */
@RunWith(MockitoJUnitRunner.class)
public class AbstractPubSubSupportTest {

    IBusConfiguration configuration;

    @Mock
    IPublicationErrorHandler handler1;

    @Mock
    IPublicationErrorHandler handler2;

    @Mock
    IPublicationErrorHandler handler3;

    @Mock
    PublicationError publicationError;


    @Before
    public void setup() {
        configuration = MessageBusTest.SyncAsync(false);
    }


    @Test
    public void testHandlePublicationError_handlers_present_sync() {
        //given

        configuration.addPublicationErrorHandler(handler1);
        configuration.addPublicationErrorHandler(handler2);
        configuration.addPublicationErrorHandler(handler3);
        //when
        SyncMessageBus<String> bus = new SyncMessageBus<String>(configuration);
        bus.handlePublicationError(publicationError);
        //then
        verify(handler1).handleError(publicationError);
        verify(handler2).handleError(publicationError);
        verify(handler3).handleError(publicationError);
    }

    @Test
    public void testHandlePublicationError_handlers_present_async() {
        //given

        configuration.addPublicationErrorHandler(handler1);
        configuration.addPublicationErrorHandler(handler2);
        configuration.addPublicationErrorHandler(handler3);
        //when
        MBassador<String> bus = new MBassador<String>(configuration);
        bus.handlePublicationError(publicationError);
        //then
        verify(handler1).handleError(publicationError);
        verify(handler2).handleError(publicationError);
        verify(handler3).handleError(publicationError);
    }


    @Test
    public void testHandlePublicationError_construct_with_handler_sync() {
        //given

        //when
        SyncMessageBus<String> bus = new SyncMessageBus<String>(handler1);
        bus.handlePublicationError(publicationError);
        //then
        verify(handler1).handleError(publicationError);
    }

    @Test
    public void testHandlePublicationError_constrcut_with_handler_async() {
        //given

        configuration.addPublicationErrorHandler(handler1);
        //when
        MBassador<String> bus = new MBassador<String>(handler1);
        bus.handlePublicationError(publicationError);
        //then
        verify(handler1).handleError(publicationError);
    }

    /**
     * Test configuration that does not provide a publication error handler.
     * This should print a warning message and fallback to STDOUT handler
     */
    @Test
    public void testHandlePublicationError_no_handlers_present_construct_with_config_async() {
        //given
        PrintStream old = null;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            old = System.out;
            System.setOut(ps);
            //when no publication error handler is provided
            MBassador<String> bus = new MBassador<String>(configuration);
            // then we see the warning on the console
            Assert.assertTrue(baos.toString().contains(AbstractPubSubSupport.ERROR_HANDLER_MSG));
        } finally {
            System.out.flush();
            if (old != null) {
                System.setOut(old);
            }
        }

    }

    @Test
    public void testHandlePublicationError_default_construct_sync() {
        //given
        final String errorMsg = "Test error";
        when(publicationError.toString()).thenReturn(errorMsg);
        PrintStream old = null;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            old = System.out;
            System.setOut(ps);
            //when
            SyncMessageBus<String> bus = new SyncMessageBus<String>();
            Assert.assertTrue(baos.toString().contains(AbstractPubSubSupport.ERROR_HANDLER_MSG));
            bus.handlePublicationError(publicationError);
            //then
            Assert.assertTrue(baos.toString().contains(errorMsg));

        } finally {
            System.out.flush();
            if (old != null) {
                System.setOut(old);
            }
        }
    }

    @Test
    public void testHandlePublicationError_default_construct_async() {
        //given
        final String errorMsg = "Test error";
        when(publicationError.toString()).thenReturn(errorMsg);
        PrintStream old = null;

        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            PrintStream ps = new PrintStream(baos);
            old = System.out;
            System.setOut(ps);
            //when
            MBassador<String> bus = new MBassador<String>();
            Assert.assertTrue(baos.toString().contains(AbstractPubSubSupport.ERROR_HANDLER_MSG));
            bus.handlePublicationError(publicationError);
            //then
            Assert.assertTrue(baos.toString().contains(errorMsg));

        } finally {
            System.out.flush();
            if (old != null) {
                System.setOut(old);
            }
        }

    }

    @Test
    public void testHandlePublicationError_raises_exception() {
        final AtomicInteger invocationCounter = new AtomicInteger(0);
        SyncMessageBus<String> bus = new SyncMessageBus<String>(new IPublicationErrorHandler() {
            @Override
            public void handleError(PublicationError error) {
                invocationCounter.incrementAndGet();
                throw new RuntimeException("exception encountered in error handler");
            }
        });
        bus.handlePublicationError(publicationError);
        Assert.assertEquals(1, invocationCounter.get());
    }
}