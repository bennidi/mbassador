package net.engio.mbassy.bus.config;

import net.engio.mbassy.bus.MessagePublication;
import net.engio.mbassy.listener.MetadataReader;
import net.engio.mbassy.subscription.ISubscriptionManagerProvider;
import net.engio.mbassy.subscription.SubscriptionFactory;

/**
 * The configuration options for the synchronous message bus {@link net.engio.mbassy.bus.SyncMessageBus}
 */
public interface ISyncBusConfiguration {

    /**
     * The message publication factory is used to wrap a published message
     * and while it is being processed
     * @return The factory to be used by the bus to create the publications
     */
	MessagePublication.Factory getMessagePublicationFactory();

	MetadataReader getMetadataReader();

	SubscriptionFactory getSubscriptionFactory();
	
	ISubscriptionManagerProvider getSubscriptionManagerProvider();

}