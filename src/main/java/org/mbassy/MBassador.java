package org.mbassy;

import org.mbassy.filter.Filter;
import org.mbassy.filter.MessageFilter;
import org.mbassy.common.*;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;


public class MBassador<T> implements IMessageBus<T, SimplePostCommand>{


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
    private ExecutorService executor;

	// cache already created filter instances
	private final Map<Class<? extends MessageFilter>, MessageFilter> filterCache = new HashMap<Class<? extends MessageFilter>, MessageFilter>();

	// all subscriptions per message type
	// this is the primary list for dispatching a specific message
    // write access is synchronized and happens very infrequently
	private final Map<Class, Collection<Subscription>> subscriptionsPerMessage = new HashMap(50);

	// all subscriptions per messageHandler type
	// this list provides fast access for subscribing and unsubscribing
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
                            errorHandler.handleError(new PublicationError(e, "Asynchronous publication interrupted", null, null, null));
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
        this(2);
    }

    public MBassador(int dispatcherThreadCount){
        this(2, new ThreadPoolExecutor(5, 50, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>()));
    }

    public MBassador(int dispatcherThreadCount, ExecutorService executor){
        this.executor = executor;
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
			if(subscriptions == null){
                return; // TODO: Dead Event?
            }
            for (Subscription subscription : subscriptions){
                subscription.publish(message);
            }
		} catch (Throwable e) {
			handlePublicationError(new PublicationError()
					.setMessage("Error during publication of message")
					.setCause(e)
					.setPublishedObject(message));
		}

	}


	public void unsubscribe(Object listener){
		if (listener == null) return;
		Collection<Subscription> subscriptions = subscriptionsPerListener.get(listener.getClass());
		if(subscriptions == null)return;
        for (Subscription subscription : subscriptions) {
			subscription.unsubscribe(listener);
		}
	}

    @Override
    public SimplePostCommand post(T message) {
        return new SimplePostCommand(this, message);
    }

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
						Subscription subscription = createSubscription(messageHandler, filter);
						subscription.subscribe(listener);
						addMessageTypeSubscription(eventType, subscription);
						subscriptionsByListener.add(subscription);
						//updateMessageTypeHierarchy(eventType);
					}
					subscriptionsPerListener.put(listeningClass, subscriptionsByListener);
				}
			}
		}
		// register the listener to the existing subscriptions
		for (Subscription sub : subscriptionsByListener) sub.subscribe(listener);
	}


	public void setErrorHandler(IPublicationErrorHandler handler){
		this.errorHandler = handler;
	}



	// obtain the set of subscriptions for the given message type
	private Collection<Subscription> getSubscriptionsByMessageType(Class messageType) {
		List<Subscription> subscriptions = new LinkedList<Subscription>();

		if(subscriptionsPerMessage.get(messageType) != null) {
			subscriptions.addAll(subscriptionsPerMessage.get(messageType));
		}
		for (Class eventSuperType : getSuperclasses(messageType)){
           if(subscriptionsPerMessage.get(eventSuperType) != null){
               subscriptions.addAll(subscriptionsPerMessage.get(eventSuperType));
           }
        }
        // IMPROVEMENT: use tree list that sorts during insertion
		//Collections.sort(subscriptions, new SubscriptionByPriorityDesc());
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


	private boolean isValidMessageHandler(Method handler) {
		if (handler.getParameterTypes().length != 1) {
			// a messageHandler only defines one parameter (the message)
			System.out.println("Found no or more than one parameter in messageHandler [" + handler.getName()
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


    private Subscription createSubscription(Method messageHandler, MessageFilter[] filter){
        if(filter == null || filter.length == 0){
            if(isAsynchronous(messageHandler)){
                return new UnfilteredAsynchronousSubscription(messageHandler);
            }
            else{
                return new UnfilteredSynchronousSubscription(messageHandler);
            }
        }
        else{
            if(isAsynchronous(messageHandler)){
                return new FilteredAsynchronousSubscription(messageHandler, filter);
            }
            else{
                return new FilteredSynchronousSubscription(messageHandler, filter);
            }
        }
    }

    private boolean isAsynchronous(Method messageHandler){
         return messageHandler.getAnnotation(Listener.class).mode().equals(Listener.Dispatch.Asynchronous);
    }


    /**
     * Subscription is a thread safe container for objects that contain message handlers
     */
	private abstract class Subscription {

		private final Method messageHandler;

		protected ConcurrentSet<Object> listeners = new ConcurrentSet<Object>();

        private int priority = 0;

		private Subscription(Method messageHandler) {
            // TODO: init priority
			this.messageHandler = messageHandler;
            this.messageHandler.setAccessible(true);
		}

        protected abstract void publish(Object message);

        protected abstract void dispatch(final Object message, final Object listener);


        public int getPriority(){
            return priority;
        }


		public void subscribe(Object o) {
			listeners.add(o);

		}

        protected void invokeHandler(final Object message, final Object listener){
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


		public void unsubscribe(Object existingListener) {
			listeners.remove(existingListener);
		}




	}

    private abstract class UnfilteredSubscription extends Subscription{


        private UnfilteredSubscription(Method messageHandler) {
            super(messageHandler);
        }

        public void publish(Object message) {

            Iterator<Object> iterator = listeners.iterator();
            Object listener = null;
            while ((listener = iterator.next()) != null) {
                dispatch(message, listener);
            }
        }
    }

    private class UnfilteredAsynchronousSubscription extends UnfilteredSubscription{


        private UnfilteredAsynchronousSubscription(Method messageHandler) {
            super(messageHandler);
        }

        protected void dispatch(final Object message, final Object listener){
                MBassador.this.executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        invokeHandler(message, listener);
                    }
                });

        }
    }

    private class UnfilteredSynchronousSubscription extends UnfilteredSubscription{


        private UnfilteredSynchronousSubscription(Method messageHandler) {
            super(messageHandler);
        }

        protected void dispatch(final Object message, final Object listener){
            invokeHandler(message, listener);
        }
    }

    private abstract class FilteredSubscription extends Subscription{

        private final MessageFilter[] filter;


        private FilteredSubscription(Method messageHandler, MessageFilter[] filter) {
            super(messageHandler);
            this.filter = filter;
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

        protected void publish(Object message) {

            Iterator<Object> iterator = listeners.iterator();
            Object listener = null;
            while ((listener = iterator.next()) != null) {
                if(passesFilter(message, listener)) {
                    dispatch(message, listener);
                }
            }
        }
    }

    private class FilteredSynchronousSubscription extends FilteredSubscription{


        private FilteredSynchronousSubscription(Method messageHandler, MessageFilter[] filter) {
            super(messageHandler, filter);
        }

        protected void dispatch(final Object message, final Object listener){
            MBassador.this.executor.execute(new Runnable() {
                @Override
                public void run() {
                    invokeHandler(message, listener);
                }
            });

        }
    }

    private class FilteredAsynchronousSubscription extends FilteredSubscription{


        private FilteredAsynchronousSubscription(Method messageHandler, MessageFilter[] filter) {
            super(messageHandler, filter);
        }

        protected void dispatch(final Object message, final Object listener){
            invokeHandler(message, listener);
        }
    }


    private final class SubscriptionByPriorityDesc implements Comparator<Subscription> {
        @Override
        public int compare(Subscription o1, Subscription o2) {
            return o1.getPriority() - o2.getPriority();
        }
    };

}
