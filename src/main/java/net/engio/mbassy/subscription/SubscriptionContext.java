package net.engio.mbassy.subscription;

import net.engio.mbassy.bus.IMessageBus;
import net.engio.mbassy.listener.MessageHandlerMetadata;

/**
 * The subscription context holds all (meta)data/objects that are relevant to successfully publish
 * a message within a subscription. A one-to-one relation between a subscription and
 * subscription context holds -> a subscription context is created for each distinct subscription
 * that lives inside a message bus.
 *
 * @author bennidi
 *         Date: 11/23/12
 */
public class SubscriptionContext {

    private IMessageBus owningBus;

    private MessageHandlerMetadata handlerMetadata;

    public SubscriptionContext(IMessageBus owningBus, MessageHandlerMetadata handlerMetadata) {
        this.owningBus = owningBus;
        this.handlerMetadata = handlerMetadata;
    }

    /**
     * Get a reference to the message bus this context belongs to
     * @return
     */
    public IMessageBus getOwningBus() {
        return owningBus;
    }


    /**
     * Get the meta data that specifies the characteristics of the message handler
     * that is associated with this context
     * @return
     */
    public MessageHandlerMetadata getHandlerMetadata() {
        return handlerMetadata;
    }

}
