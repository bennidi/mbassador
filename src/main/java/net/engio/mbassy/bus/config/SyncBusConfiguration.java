package net.engio.mbassy.bus.config;

import net.engio.mbassy.bus.MessagePublication;
import net.engio.mbassy.listener.MetadataReader;
import net.engio.mbassy.subscription.ISubscriptionManagerProvider;
import net.engio.mbassy.subscription.SubscriptionFactory;
import net.engio.mbassy.subscription.SubscriptionManagerProvider;

/**
 * Todo: Add javadoc
 *
 * @author bennidi
 *         Date: 3/29/13
 */
public class SyncBusConfiguration<C extends SyncBusConfiguration<C>> implements ISyncBusConfiguration {

    protected SubscriptionFactory subscriptionFactory;
    protected MetadataReader metadataReader;
    protected MessagePublication.Factory messagePublicationFactory;
    protected ISubscriptionManagerProvider subscriptionManagerProvider;

    public SyncBusConfiguration() {
        this.metadataReader = new MetadataReader();
        this.subscriptionFactory = new SubscriptionFactory();
        this.messagePublicationFactory = new MessagePublication.Factory();
        this.subscriptionManagerProvider = new SubscriptionManagerProvider();
    }

    public MessagePublication.Factory getMessagePublicationFactory() {
        return messagePublicationFactory;
    }

    public void setMessagePublicationFactory(MessagePublication.Factory messagePublicationFactory) {
        this.messagePublicationFactory = messagePublicationFactory;
    }

    public MetadataReader getMetadataReader() {
        return metadataReader;
    }

    public C setMetadataReader(MetadataReader metadataReader) {
        this.metadataReader = metadataReader;
        return (C) this;
    }

    public SubscriptionFactory getSubscriptionFactory() {
        return subscriptionFactory;
    }

    public C setSubscriptionFactory(SubscriptionFactory subscriptionFactory) {
        this.subscriptionFactory = subscriptionFactory;
        return (C) this;
    }
    
    public ISubscriptionManagerProvider getSubscriptionManagerProvider() {
    	return subscriptionManagerProvider;
    }
    
    public C setSubscriptionManagerProvider(ISubscriptionManagerProvider subscriptionManagerProvider) {
    	this.subscriptionManagerProvider = subscriptionManagerProvider;
    	return (C) this;
    }
}
