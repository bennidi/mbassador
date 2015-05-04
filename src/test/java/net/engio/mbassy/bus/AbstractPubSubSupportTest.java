package net.engio.mbassy.bus;

import com.mycila.testing.junit.MycilaJunitRunner;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;
import net.engio.mbassy.common.MessageBusTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Added for changes proposed under https://github.com/bennidi/mbassador/issues/106
 * <p/>
 * Created by David Sowerby on 13/04/15.
 */
@RunWith(MycilaJunitRunner.class)
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
        configuration = MessageBusTest.SyncAsync();
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

    @Test
    public void testHandlePublicationError_no_handlers_present_construct_with_config_async() {
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
            MBassador<String> bus = new MBassador<String>(configuration);
            assertThat(baos.toString()).contains(AbstractPubSubSupport.ERROR_HANDLER_MSG);
            bus.handlePublicationError(publicationError);
            //then
            assertThat(baos.toString()).contains(errorMsg);

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
            assertThat(baos.toString()).contains(AbstractPubSubSupport.ERROR_HANDLER_MSG);
            bus.handlePublicationError(publicationError);
            //then
            assertThat(baos.toString()).contains(errorMsg);

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
            assertThat(baos.toString()).contains(AbstractPubSubSupport.ERROR_HANDLER_MSG);
            bus.handlePublicationError(publicationError);
            //then
            assertThat(baos.toString()).contains(errorMsg);

        } finally {
            System.out.flush();
            if (old != null) {
                System.setOut(old);
            }
        }

    }


}