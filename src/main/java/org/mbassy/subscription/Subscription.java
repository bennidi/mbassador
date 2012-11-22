package org.mbassy.subscription;

import org.mbassy.IMessageBus;
import org.mbassy.IPublicationErrorHandler;
import org.mbassy.common.ConcurrentSet;
import org.mbassy.PublicationError;
import org.mbassy.listener.MessageHandlerMetadata;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Comparator;
import java.util.UUID;

/**
 * Subscription is a thread safe container for objects that contain message handlers
 */
public abstract class Subscription {

    private UUID id = UUID.randomUUID();

    private final Method handler;

    protected ConcurrentSet<Object> listeners = new ConcurrentSet<Object>();

    private int priority = 0;

    private IMessageBus owningBus ;

    public Subscription(IMessageBus owningBus, MessageHandlerMetadata messageHandler) {
        this.owningBus = owningBus;
        this.priority = messageHandler.getPriority();
        this.handler = messageHandler.getHandler();
        this.handler.setAccessible(true);
    }

    public abstract void publish(Object message);

    protected abstract void dispatch(final Object message, final Object listener);


    protected IMessageBus getMessageBus(){
        return owningBus;
    }

    public int getPriority(){
        return priority;
    }


    public void subscribe(Object o) {
        listeners.add(o);

    }

    protected void handlePublicationError(PublicationError error){
        Collection<IPublicationErrorHandler> handlers = owningBus.getRegisteredErrorHandlers();
        for(IPublicationErrorHandler handler : handlers){
            handler.handleError(error);
        }
    }

    protected void invokeHandler(final Object message, final Object listener){
        try {
            handler.invoke(listener, message);
        }catch(IllegalAccessException e){
            handlePublicationError(
                    new PublicationError(e, "Error during messageHandler notification. " +
                            "The class or method is not accessible",
                            handler, listener, message));
        }
        catch(IllegalArgumentException e){
            handlePublicationError(
                    new PublicationError(e, "Error during messageHandler notification. " +
                            "Wrong arguments passed to method. Was: " + message.getClass()
                            + "Expected: " + handler.getParameterTypes()[0],
                            handler, listener, message));
        }
        catch (InvocationTargetException e) {
            handlePublicationError(
                    new PublicationError(e, "Error during messageHandler notification. " +
                            "Message handler threw exception",
                            handler, listener, message));
        }
        catch (Throwable e) {
            handlePublicationError(
                    new PublicationError(e, "Error during messageHandler notification. " +
                            "Unexpected exception",
                            handler, listener, message));
        }
    }


    public boolean unsubscribe(Object existingListener) {
        return listeners.remove(existingListener);
    }


    public static final Comparator<Subscription> SubscriptionByPriorityDesc = new Comparator<Subscription>() {
        @Override
        public int compare(Subscription o1, Subscription o2) {
            int result =  o1.getPriority() - o2.getPriority();
            return result == 0 ? o1.id.compareTo(o2.id): result;
        }
    };

}
