package org.mbassy;

import org.mbassy.filter.Filter;
import org.mbassy.filter.MessageFilter;
import org.mbassy.common.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;

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
public class MBassador<T>{


	//  This predicate is used to find all message listeners (methods annotated with @Listener)
	private static final IPredicate<Method> AllMessageListeners = new IPredicate<Method>() {
		@Override
		public boolean apply(Method target) {
			return target.getAnnotation(Listener.class) != null;
		}
	};

    // This is the default error handler it will simply log to standard out and
    // print stack trace if available
	protected static final class ConsoleLogger implements IPublicationErrorHandler {
		@Override
		public void handleError(PublicationError error) {
            System.out.println(error);
            if (error.getCause() != null) error.getCause().printStackTrace();
		}
	};

    // executor for asynchronous listeners using unbound queuing strategy to ensure that no events get lost
    private ExecutorService executor = new ThreadPoolExecutor(5, 50, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());

	// cache already created filter instances
	private final Map<Class<? extends MessageFilter>, MessageFilter> filterCache = new HashMap<Class<? extends MessageFilter>, MessageFilter>();

	// all subscriptions per message type
	// this is the primary list for dispatching a specific message
	private final Map<Class, Collection<Subscription>> subscriptionsPerMessage = new HashMap(50);

	// all subscriptions per messageHandler type
	// this list provides access for subscribing and unsubsribing
	private final Map<Class, Collection<Subscription>> subscriptionsPerListener = new HashMap(50);

	// remember already processed classes that do not contain any listeners
	private final Collection<Class> nonListeners = new HashSet();

    // this handler will receive all errors that occur during message dispatch or message handling
	private IPublicationErrorHandler errorHandler = new ConsoleLogger();


    // all threads that are available for asynchronous message dispatching
    private final CopyOnWriteArrayList<Thread> dispatchers = new CopyOnWriteArrayList<Thread>();

    // all pending messages scheduled for asynchronous dispatch are queued here
    private final LinkedBlockingQueue<T> pendingMessages = new LinkedBlockingQueue<T>();

    // initialize the dispatch workers
    private void initDispatcherThreads(int numberOfThreads) {
        for (int i = 0; i < numberOfThreads; i++) {
            // each thread will run forever and process incoming
            //dispatch requests
            Thread dispatcher = new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        try {
                            publish(pendingMessages.take());
                        } catch (InterruptedException e) {
                            errorHandler.handleError(new PublicationError(e, "Asnchronous publication interupted", null, null, null));
                            return;
                        }
                    }
                }
            });
            dispatchers.add(dispatcher);
            dispatcher.start();
        }
    }

    public MBassador(){
        initDispatcherThreads(2);
    }

    public MBassador(int dispatcherThreadCount){
        initDispatcherThreads(dispatcherThreadCount > 0 ? dispatcherThreadCount : 2);
    }


    public void publishAsync(T message){
        pendingMessages.offer(message);
    }


    /**
     * Synchronously publish a message to all registered listeners (this includes listeners defined for super types)
     * The call blocks until every messageHandler has processed the message.
     *
     * @param message
     */
	public void publish(T message){
		try {
			final Collection<Subscription> subscriptions = getSubscriptionsByMessageType(message.getClass());
			for (Subscription subscription : subscriptions) subscription.publish(message);
		} catch (Throwable e) {
			handlePublicationError(new PublicationError()
					.setMessage("Error during publication of message")
					.setCause(e)
					.setPublishedObject(message));
		}

	}

    /**
     * Immediately unsubscribe all registered message handlers (if any) of the given listener. When this call returns
     * have effectively been removed and will not receive any message publications (including asynchronously scheduled
     * publications that have been published when the messageHandler was still subscribed).
     * A call to this method passing null, an already subscribed message or any message that does not define any listeners
     * will not have any effect.
     *
     * @param listener
     */
	public void unsubscribe(Object listener){
		if (listener == null) return;
		Collection<Subscription> subscriptions = subscriptionsPerListener.get(listener.getClass());
		for (Subscription subscription : subscriptions) {
			subscription.unsubscribe(listener);
		}
	}


    /**
     * Subscribe all listeners of the given message to receive message publications.
     * Any message may only be subscribed once (subsequent subscriptions of an already subscribed
     * message will be silently ignored)
     *
     * @param listener
     */
	public void subscribe(Object listener){
		Class listeningClass = listener.getClass();
		if (nonListeners.contains(listeningClass))
			return; // early reject of known classes that do not participate in eventing
		Collection<Subscription> subscriptionsByListener = subscriptionsPerListener.get(listeningClass);
		if (subscriptionsByListener == null) { // if the type is registered for the first time
			synchronized (this) { // new subscriptions must be processed sequentially for each class
				subscriptionsByListener = subscriptionsPerListener.get(listeningClass);
				if (subscriptionsByListener == null) {  // double check (a bit ugly but works here)
					List<Method> messageHandlers = getListeners(listeningClass);  // get all methods with subscriptions
					subscriptionsByListener = new ArrayList<Subscription>(messageHandlers.size()); // it's safe to use non-concurrent collection here (read only)
					if (messageHandlers.isEmpty()) {  // remember the class as non listening class
						nonListeners.add(listeningClass);
						return;
					}
					// create subscriptions for all detected listeners
					for (Method messageHandler : messageHandlers) {
						if (!isValidMessageHandler(messageHandler)) continue; // ignore invalid listeners
						MessageFilter[] filter = getFilter(messageHandler.getAnnotation(Listener.class));
						Class eventType = getMessageType(messageHandler);
						Subscription subscription = new Subscription(messageHandler, filter);
						subscription.subscribe(listener);
						addMessageTypeSubscription(eventType, subscription);
						subscriptionsByListener.add(subscription);
						//updateMessageTypeHierarchy(eventType);
					}
					subscriptionsPerListener.put(listeningClass, subscriptionsByListener);
				}
			}
		}
		// register the message to the existing subscriptions
		for (Subscription sub : subscriptionsByListener) sub.subscribe(listener);
	}


	public void setErrorHandler(IPublicationErrorHandler handler){
		this.errorHandler = handler;
	}



	// obtain the set of subscriptions for the given message type
	private Collection<Subscription> getSubscriptionsByMessageType(Class messageType) {
		// TODO improve with cache
		Collection<Subscription> subscriptions = new LinkedList<Subscription>();

		if(subscriptionsPerMessage.get(messageType) != null) {
			subscriptions.addAll(subscriptionsPerMessage.get(messageType));
		}
		for (Class eventSuperType : getSuperclasses(messageType)){
           if(subscriptionsPerMessage.get(eventSuperType) != null){
               subscriptions.addAll(subscriptionsPerMessage.get(eventSuperType));
           }
        }

		return subscriptions;
	}

    private Collection<Class> getSuperclasses(Class from){
        Collection<Class> superclasses = new LinkedList<Class>();
        while(!from.equals(Object.class)){
            superclasses.add(from.getSuperclass());
            from = from.getSuperclass();
        }
        return superclasses;
    }

	// associate a suscription with a message type
	private void addMessageTypeSubscription(Class messageType, Subscription subscription) {
		Collection<Subscription> subscriptions = subscriptionsPerMessage.get(messageType);
		if (subscriptions == null) {
			subscriptions = new CopyOnWriteArraySet<Subscription>();
			subscriptionsPerMessage.put(messageType, subscriptions);
		}
		subscriptions.add(subscription);
	}

    /*
	private void updateMessageTypeHierarchy(Class messageType) {
		for (Class existingEventType : subscriptionsPerMessage.keySet()) {
			if (existingEventType.equals(messageType)) continue;
			if (messageType.isAssignableFrom(existingEventType)) //message is super type of existing
				messageTypeHierarchy.put(existingEventType, messageType);
			else if (existingEventType.isAssignableFrom(messageType)) { // message is sub type of existing
				messageTypeHierarchy.put(messageType, existingEventType); // add direct super type
				messageTypeHierarchy.putAll(messageType, messageTypeHierarchy.get(existingEventType)); // add all super types of super type
			}
		}
	}*/


	private boolean isValidMessageHandler(Method handler) {
		if (handler.getParameterTypes().length != 1) {
			// a messageHandler only defines one parameter (the message)
			System.out.println("Found nono or more than one parameter in messageHandler [" + handler.getName()
					+ "]. A messageHandler must define exactly one parameter");
			return false;
		}
		return true;
	}

	private static Class getMessageType(Method listener) {
		return listener.getParameterTypes()[0];
	}

	// get all listeners defined by the given class (includes
	// listeners defined in super classes)
	private static List<Method> getListeners(Class<?> target) {
		return ReflectionUtils.getMethods(AllMessageListeners, target);
	}

	// retrieve all instances of filters associated with the given subscription
	private MessageFilter[] getFilter(Listener subscription) {
		if (subscription.value().length == 0) return null;
		MessageFilter[] filters = new MessageFilter[subscription.value().length];
		int i = 0;
		for (Filter filterDef : subscription.value()) {
			MessageFilter filter = filterCache.get(filterDef.value());
			if (filter == null) {
				try {
					filter = filterDef.value().newInstance();
					filterCache.put(filterDef.value(), filter);
				} catch (Throwable e) {
					handlePublicationError(new PublicationError()
							.setMessage("Error retrieving filter"));
				}

			}
			filters[i] = filter;
			i++;
		}
		return filters;
	}



	private void handlePublicationError(PublicationError error) {
		errorHandler.handleError(error);
	}

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        for(Thread dispatcher : dispatchers){
            dispatcher.interrupt();
        }
    }

    /**
     * Subscription is a thread safe container for objects that contain message handlers
     *
     */
	private class Subscription {

		private final MessageFilter[] filter;

		private final Method messageHandler;

		private ConcurrentLinkedBag<Object> listeners = new ConcurrentLinkedBag<Object>();

        private boolean isAynchronous;

		private Subscription(Method messageHandler, MessageFilter[] filter) {
			this.messageHandler = messageHandler;
			this.filter = filter;
            this.messageHandler.setAccessible(true);
            this.isAynchronous = messageHandler.getAnnotation(Listener.class).mode().equals(Listener.Dispatch.Asynchronous);
		}


		public void subscribe(Object o) {
			listeners.add(o);

		}

        private void dispatch(final Object message, final Object listener){
            if(isAynchronous){
                 MBassador.this.executor.execute(new Runnable() {
                     @Override
                     public void run() {
                         invokeHandler(message, listener);
                     }
                 });
            }
            else{
                invokeHandler(message, listener);
            }
        }

        private void invokeHandler(final Object message, final Object listener){
            try {
                messageHandler.invoke(listener, message);
            }catch(IllegalAccessException e){
                MBassador.this.handlePublicationError(
                        new PublicationError(e, "Error during messageHandler notification. " +
                                "The class or method is not accessible",
                                messageHandler, listener, message));
            }
            catch(IllegalArgumentException e){
                MBassador.this.handlePublicationError(
                        new PublicationError(e, "Error during messageHandler notification. " +
                                "Wrong arguments passed to method. Was: " + message.getClass()
                                + "Expected: " + messageHandler.getParameterTypes()[0],
                                messageHandler, listener, message));
            }
            catch (InvocationTargetException e) {
                MBassador.this.handlePublicationError(
                        new PublicationError(e, "Error during messageHandler notification. " +
                                "Message handler threw exception",
                                messageHandler, listener, message));
            }
            catch (Throwable e) {
                MBassador.this.handlePublicationError(
                        new PublicationError(e, "Error during messageHandler notification. " +
                                "Unexpected exception",
                                messageHandler, listener, message));
            }
        }

		public void publish(Object message) {

			Iterator<Object> iterator = listeners.iterator();
			Object listener = null;
			while ((listener = iterator.next()) != null) {
					if(passesFilter(message, listener)) {
						dispatch(message, listener);
					}
			}
		}

		private boolean passesFilter(Object message, Object listener) {

			if (filter == null) {
				return true;
			}
			else {
				for (int i = 0; i < filter.length; i++) {
					if (!filter[i].accepts(message, listener)) return false;
				}
				return true;
			}
		}

		public void unsubscribe(Object existingListener) {
			listeners.remove(existingListener);
		}
	}

}
