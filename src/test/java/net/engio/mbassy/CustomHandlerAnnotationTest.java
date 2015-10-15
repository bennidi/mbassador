package net.engio.mbassy;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.common.MessageBusTest;
import net.engio.mbassy.listener.*;
import net.engio.mbassy.subscription.MessageEnvelope;
import net.engio.mbassy.subscription.SubscriptionContext;
import org.junit.Test;

import java.lang.annotation.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

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
	@Handler(filters = { @Filter(NamedMessageFilter.class) })
	@Synchronized
	@Target(value = { ElementType.METHOD, ElementType.ANNOTATION_TYPE })
	@interface NamedMessageHandler
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
    @interface MessageThree {}



	/**
	 * Test enveloped meta annotation.
	 */
	@Retention(value = RetentionPolicy.RUNTIME)
	@Target(value = { ElementType.METHOD, ElementType.ANNOTATION_TYPE })
	@Inherited
	@Handler(filters = { @Filter(NamedMessageFilter.class) })
	@Enveloped(messages = NamedMessage.class)
	@interface EnvelopedNamedMessageHandler
	{
		/**
		 * @return The message names supported.
		 */
		String[] value();
	}

	/**
	 * Searches for a NamedMessageHandler annotation on the handler method.
	 * The annotation specifies the supported message names.
	 */
	public static class NamedMessageFilter implements IMessageFilter<NamedMessage>
	{
		@Override
		public boolean accepts( NamedMessage message,  SubscriptionContext context ) {
            MessageHandler handler = context.getHandler();
			NamedMessageHandler namedMessageHandler = handler.getAnnotation(NamedMessageHandler.class);
			if ( namedMessageHandler != null ) {
				return Arrays.asList( namedMessageHandler.value() ).contains( message.getName() );
			}

			EnvelopedNamedMessageHandler envelopedHandler = handler.getAnnotation(EnvelopedNamedMessageHandler.class);
			return envelopedHandler != null && Arrays.asList( envelopedHandler.value() ).contains( message.getName() );

		}
	}

	static class NamedMessage
	{
		private String name;

		NamedMessage( String name ) {
			this.name = name;
		}

		public String getName() {
			return name;
		}
	}

	static class NamedMessageListener
	{
		final Set<NamedMessage> handledByOne = new HashSet<NamedMessage>();
		final Set<NamedMessage> handledByTwo = new HashSet<NamedMessage>();
		final Set<NamedMessage> handledByThree = new HashSet<NamedMessage>();

		@NamedMessageHandler({ "messageOne", "messageTwo" })
		void handlerOne( NamedMessage message ) {
			handledByOne.add( message );
		}

		@EnvelopedNamedMessageHandler({ "messageTwo", "messageThree" })
		void handlerTwo( MessageEnvelope envelope ) {
			handledByTwo.add( (NamedMessage) envelope.getMessage() );
		}

		@MessageThree
		void handlerThree( NamedMessage message ) {
			handledByThree.add( message );
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
