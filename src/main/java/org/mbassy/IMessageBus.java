package org.mbassy;

import java.util.Collection;
import java.util.concurrent.Executor;

/**
 *
 * A message bus offers facilities for publishing messages to registered listeners. Messages can be dispatched
 * synchronously or asynchronously and may be of any type that is a valid sub type of the type parameter T.
 * The dispatch mechanism can by controlled for each concrete message publication.
 * A message publication is the publication of any message using one of the bus' publish(..) methods.
 * <p/>
 * Each message publication is isolated from all other running publications such that it does not interfere with them.
 * Hence, the bus expects message handlers to be stateless as it may invoke them concurrently if multiple
 * messages get published asynchronously.
 * <p/>
 * A listener is any object that defines at least one message handler and that has been subscribed to at least
 * one message bus. A message handler can be any method that accepts exactly one parameter (the message) and is marked
 * as a message handler using the @Listener annotation.
 * <p/>
 * The bus uses weak references to all listeners such that registered listeners do not need to
 * be explicitly unregistered to be eligible for garbage collection. Dead (garbage collected) listeners are
 * removed on-the-fly as messages get dispatched.
 * <p/>
 * Generally message handlers will be invoked in inverse sequence of insertion (subscription) but any
 * class using this bus should not rely on this assumption. The basic contract of the bus is that it will deliver
 * a specific message exactly once to each of the subscribed message handlers.
 * <p/>
 * Messages are dispatched to all listeners that accept the type or supertype of the dispatched message. Additionally
 * a message handler may define filters to narrow the set of messages that it accepts.
 * <p/>
 * Subscribed message handlers are available to all pending message publications that have not yet started processing.
 * Any messageHandler may only be subscribed once (subsequent subscriptions of an already subscribed messageHandler will be silently ignored)
 * <p/>
 * Removing a listener means removing all subscribed message handlers of that object. This remove operation
 * immediately takes effect and on all running dispatch processes. A removed listener (a listener
 * is considered removed after the remove(Object) call returned) will under no circumstances receive any message publications.
 *
 * NOTE: Generic type parameters of messages will not be taken into account, e.g. a List<Long> will
 * get dispatched to all message handlers that take an instance of List as their parameter
 *
 * @Author bennidi
 * Date: 2/8/12
 */
public interface IMessageBus<T, P extends IMessageBus.IPostCommand> {

    /**
     * Subscribe all listeners of the given message to receive message publications.
     * Any message may only be subscribed once (subsequent subscriptions of an already subscribed
     * message will be silently ignored)
     *
     * @param listener
     */
    public void subscribe(Object listener);


    /**
     * Immediately unsubscribe all registered message handlers (if any) of the given listener. When this call returns
     * have effectively been removed and will not receive any message publications (including asynchronously scheduled
     * publications that have been published when the messageHandler was still subscribed).
     * A call to this method passing null, an already subscribed message or any message that does not define any listeners
     * will not have any effect.
     *
     * @param listener
     */
    public void unsubscribe(Object listener);

    /**
     *
     * @param message
     * @return
     */
    public P post(T message);

    /**
     * Publication errors may occur at various points of time during message delivery. A handler may throw an exception,
     * may not be accessible due to security constraints or is not annotated properly.
     * In any of all possible cases a publication error is created and passed to each of the registered error handlers.
     * A call to this method will add the given error handler to the chain
     *
     * @param errorHandler
     */
    public void addErrorHandler(IPublicationErrorHandler errorHandler);

    /**
     * Returns an immutable collection containing all the registered error handlers
     *
     * @return
     */
    public Collection<IPublicationErrorHandler> getRegisteredErrorHandlers();

    public Executor getExecutor();



    public static interface IPostCommand{

        public void now();

        public void asynchronously();

    }

}
