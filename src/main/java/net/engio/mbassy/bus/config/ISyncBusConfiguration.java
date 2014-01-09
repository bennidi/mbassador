package net.engio.mbassy.bus.config;

import net.engio.mbassy.bus.MessagePublication;
import net.engio.mbassy.listener.MetadataReader;
import net.engio.mbassy.subscription.ISubscriptionManagerProvider;
import net.engio.mbassy.subscription.SubscriptionFactory;

public interface ISyncBusConfiguration {

	MessagePublication.Factory getMessagePublicationFactory();

	MetadataReader getMetadataReader();

	SubscriptionFactory getSubscriptionFactory();
	
	ISubscriptionManagerProvider getSubscriptionManagerProvider();

}