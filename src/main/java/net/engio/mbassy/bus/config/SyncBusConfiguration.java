package net.engio.mbassy.bus.config;

import net.engio.mbassy.bus.MessagePublication;
import net.engio.mbassy.listener.MetadataReader;
import net.engio.mbassy.subscription.SubscriptionFactory;

/**
 * Todo: Add javadoc
 *
 * @author bennidi
 *         Date: 3/29/13
 */
public class SyncBusConfiguration<Config extends SyncBusConfiguration<Config>> {

    protected SubscriptionFactory subscriptionFactory;
    protected MetadataReader metadataReader;
    protected MessagePublication.Factory messagePublicationFactory;

    public SyncBusConfiguration() {
        this.metadataReader = new MetadataReader();
        this.subscriptionFactory = new SubscriptionFactory();
        this.messagePublicationFactory = new MessagePublication.Factory();
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

    public Config setMetadataReader(MetadataReader metadataReader) {
        this.metadataReader = metadataReader;
        return (Config)this;
    }

    public SubscriptionFactory getSubscriptionFactory() {
        return subscriptionFactory;
    }

    public Config setSubscriptionFactory(SubscriptionFactory subscriptionFactory) {
        this.subscriptionFactory = subscriptionFactory;
        return (Config)this;
    }
}
