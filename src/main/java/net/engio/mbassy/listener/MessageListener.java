package net.engio.mbassy.listener;

import net.engio.mbassy.common.IPredicate;
import net.engio.mbassy.common.ISetEntry;
import net.engio.mbassy.common.ReflectionUtils;
import net.engio.mbassy.common.StrongConcurrentSet;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * All instances of any class that defines at least one message handler (see @MessageHandler) are message listeners. Thus,
 * a message listener is any object capable of receiving messages by means of defined message handlers.
 * There are no restrictions about the number of allowed message handlers in a message listener.
 *
 * A message listener can be configured using the @Listener annotation but is always implicitly configured by the handler
 * definition it contains.
 *
 * This class is an internal representation of a message listener used to encapsulate all relevant objects
 * and data about that message listener, especially all its handlers.
 * There will be only one instance of MessageListener per message listener class and message bus instance.
 *
 * @author bennidi
 *         Date: 12/16/12
 */
public class MessageListener<T> {


    public static IPredicate<MessageHandler> ForMessage(final Class<?> messageType) {
        return new IPredicate<MessageHandler>() {
            @Override
            public boolean apply(MessageHandler target) {
                return target.handlesMessage(messageType);
            }
        };
    }

    private StrongConcurrentSet<MessageHandler> handlers = new StrongConcurrentSet<MessageHandler>();

    private Class<T> listenerDefinition;

    private Listener listenerAnnotation;

    public MessageListener(Class<T> listenerDefinition) {
       this.listenerDefinition = listenerDefinition;
       listenerAnnotation = ReflectionUtils.getAnnotation( listenerDefinition, Listener.class );
    }


    public boolean isFromListener(Class listener){
        return listenerDefinition.equals(listener);
    }

    public boolean useStrongReferences(){
        return listenerAnnotation != null && listenerAnnotation.references().equals(References.Strong);
    }

    public MessageListener addHandlers(Collection<? extends MessageHandler> c) {
        handlers.addAll(c);
        return this;
    }

    public boolean addHandler(MessageHandler messageHandler) {
        return handlers.add(messageHandler);
    }

    public StrongConcurrentSet<MessageHandler> getHandlers(){
        return handlers;
    }

    public List<MessageHandler> getHandlers(IPredicate<MessageHandler> filter) {
        List<MessageHandler> matching = new LinkedList<MessageHandler>();

        ISetEntry<MessageHandler> current = handlers.head;
        MessageHandler handler;
        while (current != null) {
            handler = current.getValue();
            current = current.next();

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
