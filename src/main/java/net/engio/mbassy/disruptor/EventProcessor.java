package net.engio.mbassy.disruptor;

import net.engio.mbassy.PubSubSupport;

import com.lmax.disruptor.WorkHandler;

/**
 * @author dorkbox, llc
 *         Date: 2/2/15
 */
public class EventProcessor implements WorkHandler<MessageHolder> {
    private final PubSubSupport publisher;

    public EventProcessor(PubSubSupport publisher) {
        this.publisher = publisher;
    }

    @Override
    public void onEvent(MessageHolder event) throws Exception {
        MessageType messageType = event.messageType;
        switch (messageType) {
            case ONE: {
                this.publisher.publish(event.message1);
                event.message1 = null; // cleanup
                return;
            }
            case TWO: {
                this.publisher.publish(event.message1, event.message2);
                event.message1 = null; // cleanup
                event.message2 = null; // cleanup
                return;
            }
            case THREE: {
                this.publisher.publish(event.message1, event.message2, event.message3);
                event.message1 = null; // cleanup
                event.message2 = null; // cleanup
                event.message3 = null; // cleanup
                return;
            }
            case ARRAY: {
                this.publisher.publish(event.messages);
                event.messages = null; // cleanup
                return;
            }
        }
    }
}