package net.engio.mbassy.disruptor;

import net.engio.mbassy.bus.AbstractPubSubSupport;

import com.lmax.disruptor.EventFactory;

public class EventBusFactory implements EventFactory<MessageHolder> {

    public EventBusFactory() {
    }

    @Override
    public MessageHolder newInstance() {
        return new MessageHolder();
    }
}