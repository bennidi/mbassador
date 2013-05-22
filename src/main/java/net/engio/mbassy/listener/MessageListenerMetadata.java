package net.engio.mbassy.listener;

import net.engio.mbassy.common.IPredicate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Provides information about the message listeners of a specific class. Each message handler
 * defined by the target class is represented as a single entity.
 *
 * @author bennidi
 *         Date: 12/16/12
 */
public class MessageListenerMetadata<T> {


    public static IPredicate<MessageHandlerMetadata> ForMessage(final Class<?> messageType) {
        return new IPredicate<MessageHandlerMetadata>() {
            @Override
            public boolean apply(MessageHandlerMetadata target) {
                return target.handlesMessage(messageType);
            }
        };
    }

    private List<MessageHandlerMetadata> handlers = new ArrayList<MessageHandlerMetadata>();

    private Class<T> listenerDefinition;

    private Listener listenerAnnotation;

    public MessageListenerMetadata(Class<T> listenerDefinition) {
        this.listenerDefinition = listenerDefinition;
        Listener listenerAnnotation = listenerDefinition.getAnnotation(Listener.class);
    }


    public boolean isFromListener(Class listener){
        return listenerDefinition.equals(listener);
    }

    public boolean useStrongReferences(){
        return listenerAnnotation != null && listenerAnnotation.references().equals(References.Strong);
    }

    public MessageListenerMetadata addHandlers(Collection<? extends MessageHandlerMetadata> c) {
        handlers.addAll(c);
        return this;
    }

    public boolean addHandler(MessageHandlerMetadata messageHandlerMetadata) {
        return handlers.add(messageHandlerMetadata);
    }

    public List<MessageHandlerMetadata> getHandlers(){
        return handlers;
    }

    public List<MessageHandlerMetadata> getHandlers(IPredicate<MessageHandlerMetadata> filter) {
        List<MessageHandlerMetadata> matching = new LinkedList<MessageHandlerMetadata>();
        for (MessageHandlerMetadata handler : handlers) {
            if (filter.apply(handler)) {
                matching.add(handler);
            }
        }
        return matching;
    }

    public boolean handles(Class<?> messageType) {
        return !getHandlers(ForMessage(messageType)).isEmpty();
    }

    public Class<T> getListerDefinition() {
        return listenerDefinition;
    }
}
