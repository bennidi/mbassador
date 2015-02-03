package net.engio.mbassy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.HashSet;
import java.util.Set;

import net.engio.mbassy.annotations.Handler;
import net.engio.mbassy.annotations.Synchronized;
import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.common.MessageBusTest;

import org.junit.Test;

/**
 * Tests a custom handler annotation with a @Handler meta annotation and a default filter.
 */
public class CustomHandlerAnnotationTest extends MessageBusTest
{
    /**
    * Handler annotation that adds a default filter on the NamedMessage.
    * Enveloped is in no way required, but simply added to test a meta enveloped annotation.
    */
    @Retention(value = RetentionPolicy.RUNTIME)
    @Inherited
    @Handler()
    @Synchronized
    @Target(value = { ElementType.METHOD, ElementType.ANNOTATION_TYPE })
    static @interface NamedMessageHandler
    {
        /**
        * @return The message names supported.
        */
        String[] value();
    }

    /**
     * Handler annotation that adds a default filter on the NamedMessage.
     * Enveloped is in no way required, but simply added to test a meta enveloped annotation.
     */
    @Retention(value = RetentionPolicy.RUNTIME)
    @Inherited
    @NamedMessageHandler("messageThree")
    static @interface MessageThree {}



    /**
    * Test enveloped meta annotation.
    */
    @Retention(value = RetentionPolicy.RUNTIME)
    @Target(value = { ElementType.METHOD, ElementType.ANNOTATION_TYPE })
    @Inherited
    @Handler()
    static @interface EnvelopedNamedMessageHandler
    {
        /**
        * @return The message names supported.
        */
        String[] value();
    }

    static class NamedMessage
    {
        private String name;

        NamedMessage( String name ) {
            this.name = name;
        }

        public String getName() {
            return this.name;
        }
    }

    static class NamedMessageListener
    {
        final Set<NamedMessage> handledByOne = new HashSet<NamedMessage>();
        final Set<NamedMessage> handledByTwo = new HashSet<NamedMessage>();
        final Set<NamedMessage> handledByThree = new HashSet<NamedMessage>();

        @NamedMessageHandler({ "messageOne", "messageTwo" })
        void handlerOne( NamedMessage message ) {
            this.handledByOne.add( message );
        }

        @MessageThree
        void handlerThree( NamedMessage message ) {
            this.handledByThree.add( message );
        }
    }

    @Test
    public void testMetaHandlerFiltering() {
        MBassador bus = createBus(SyncAsync());

        NamedMessageListener listener = new NamedMessageListener();
        bus.subscribe( listener );

        NamedMessage messageOne = new NamedMessage( "messageOne" );
        NamedMessage messageTwo = new NamedMessage( "messageTwo" );
        NamedMessage messageThree = new NamedMessage( "messageThree" );

        bus.publish( messageOne );
        bus.publish( messageTwo );
        bus.publish( messageThree );

        assertEquals(2, listener.handledByOne.size());
        assertTrue( listener.handledByOne.contains( messageOne ) );
        assertTrue(listener.handledByOne.contains(messageTwo));

        assertEquals(2, listener.handledByTwo.size());
        assertTrue( listener.handledByTwo.contains( messageTwo ) );
        assertTrue( listener.handledByTwo.contains( messageThree ) );

        assertEquals(1, listener.handledByThree.size());
        assertTrue( listener.handledByThree.contains( messageThree ) );
    }
}
