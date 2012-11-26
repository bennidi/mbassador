package org.mbassy.dispatch;

import org.mbassy.IMessageBus;
import org.mbassy.listener.MessageHandlerMetadata;

/**
 * The messaging context holds all data/objects that is relevant to successfully publish
 * a message within a subscription. A one-to-one relation between a subscription and
 * MessagingContext holds -> a messaging context is created for each distinct subscription
 * that lives inside a message bus.
 *
 * @author bennidi
 *         Date: 11/23/12
 */
public class MessagingContext {

    private IMessageBus owningBus;

    private MessageHandlerMetadata handlerMetadata;

    public MessagingContext(IMessageBus owningBus, MessageHandlerMetadata handlerMetadata) {
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
