package net.engio.mbassy.subscription;

import net.engio.mbassy.bus.ISyncMessageBus;
import net.engio.mbassy.listener.MessageHandlerMetadata;

/**
 * The subscription context holds all (meta)data/objects that are relevant to successfully publish
 * a message within a subscription. A one-to-one relation between a subscription and
 * subscription context holds -> a subscription context is created for each distinct subscription
 * managed by the subscription manager.
 *
 * @author bennidi
 *         Date: 11/23/12
 */
public class SubscriptionContext<Bus extends ISyncMessageBus> {

    private Bus owningBus;

    private MessageHandlerMetadata handlerMetadata;

    public SubscriptionContext(Bus owningBus, MessageHandlerMetadata handlerMetadata) {
        this.owningBus = owningBus;
        this.handlerMetadata = handlerMetadata;
    }

    /**
     * Get a reference to the message bus this context belongs to
     *
     * @return
     */
    public Bus getOwningBus() {
        return owningBus;
    }


    /**
     * Get the meta data that specifies the characteristics of the message handler
     * that is associated with this context
     *
     * @return
     */
    public MessageHandlerMetadata getHandlerMetadata() {
        return handlerMetadata;
    }

}
