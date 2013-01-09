package net.engio.mbassy.listener;

import net.engio.mbassy.common.IPredicate;

import java.util.LinkedList;
import java.util.List;

/**
 * Provides information about the message listeners of a specific class. Each message handler
 * defined by the target class is represented as a single entity.
 *
 *
 * @author bennidi
 *         Date: 12/16/12
 */
public class MessageListenerMetadata<T> {


    public static final IPredicate<MessageHandlerMetadata> ForMessage(final Class<?> messageType){
        return new IPredicate<MessageHandlerMetadata>() {
            @Override
            public boolean apply(MessageHandlerMetadata target) {
                return target.handlesMessage(messageType);
            }
        };
    }

    private List<MessageHandlerMetadata> handlers;

    private Class<T> listenerDefinition;

    public MessageListenerMetadata(List<MessageHandlerMetadata> handlers, Class<T> listenerDefinition) {
        this.handlers = handlers;
        this.listenerDefinition = listenerDefinition;
    }


    public List<MessageHandlerMetadata> getHandlers(IPredicate<MessageHandlerMetadata> filter){
        List<MessageHandlerMetadata> matching = new LinkedList<MessageHandlerMetadata>();
        for(MessageHandlerMetadata handler : handlers){
            if(filter.apply(handler))matching.add(handler);
        }
        return matching;
    }

    public boolean handles(Class<?> messageType){
        return !getHandlers(ForMessage(messageType)).isEmpty();
    }

    public Class<T> getListerDefinition(){
        return listenerDefinition;
    }
}
