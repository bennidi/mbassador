package net.engio.mbassy.disruptor;

import net.engio.mbassy.bus.AbstractPubSubSupport;
import net.engio.mbassy.bus.error.PublicationError;

import com.lmax.disruptor.EventHandler;

/**
 * @author dorkbox, llc
 *         Date: 2/2/15
 */
public class EventProcessor implements EventHandler<MessageHolder> {
    private final AbstractPubSubSupport publisher;

    private final long ordinal;
    private final long numberOfConsumers;

    public EventProcessor(AbstractPubSubSupport publisher, final long ordinal, final long numberOfConsumers) {
        this.publisher = publisher;
        this.ordinal = ordinal;
        this.numberOfConsumers = numberOfConsumers;
    }

    @Override
    public void onEvent(MessageHolder event, long sequence, boolean endOfBatch) throws Exception {
        if (sequence % this.numberOfConsumers == this.ordinal) {
            try {
                this.publisher.publishMessage(event.message);
            } catch (Throwable t) {
                this.publisher.handlePublicationError(new PublicationError()
                                                            .setMessage("Error in asynchronous dispatch")
                                                            .setCause(t)
                                                            .setPublishedObject(new Object[] {event.message}));
            }
            event.message = null; // cleanup
        }
    }
}