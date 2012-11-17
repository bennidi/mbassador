package org.mbassy;

import org.mbassy.common.IPredicate;
import org.mbassy.common.ReflectionUtils;
import org.mbassy.listener.Listener;
import org.mbassy.listener.MetadataReader;
import org.mbassy.subscription.Subscription;
import org.mbassy.subscription.SubscriptionDeliveryRequest;
import org.mbassy.subscription.SubscriptionFactory;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;


public abstract class AbstractMessageBus<T, P extends IMessageBus.IPostCommand> implements IMessageBus<T, P> {


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
    }

    ;

    // executor for asynchronous listeners using unbound queuing strategy to ensure that no events get lost
    private ExecutorService executor;

    private MetadataReader metadataReader = new MetadataReader();

    // all subscriptions per message type
    // this is the primary list for dispatching a specific message
    // write access is synchronized and happens very infrequently
    private final Map<Class, Collection<Subscription>> subscriptionsPerMessage = new HashMap(50);

    // all subscriptions per messageHandler type
    // this list provides fast access for subscribing and unsubscribing
    // write access is synchronized and happens very infrequently
    private final Map<Class, Collection<Subscription>> subscriptionsPerListener = new HashMap(50);

    // remember already processed classes that do not contain any listeners
    private final Collection<Class> nonListeners = new HashSet();

    // this handler will receive all errors that occur during message dispatch or message handling
    private CopyOnWriteArrayList<IPublicationErrorHandler> errorHandlers = new CopyOnWriteArrayList<IPublicationErrorHandler>();

    // all threads that are available for asynchronous message dispatching
    private final CopyOnWriteArrayList<Thread> dispatchers = new CopyOnWriteArrayList<Thread>();

    // all pending messages scheduled for asynchronous dispatch are queued here
    private final LinkedBlockingQueue<SubscriptionDeliveryRequest<T>> pendingMessages = new LinkedBlockingQueue<SubscriptionDeliveryRequest<T>>();

    private final SubscriptionFactory subscriptionFactory;

    // initialize the dispatch workers
    private void initDispatcherThreads(int numberOfThreads) {
        for (int i = 0; i < numberOfThreads; i++) {
            // each thread will run forever and process incoming
            //dispatch requests
            Thread dispatcher = new Thread(new Runnable() {
                public void run() {
                    while (true) {
                        try {
                           pendingMessages.take().execute();
                        } catch (InterruptedException e) {
                            handlePublicationError(new PublicationError(e, "Asynchronous publication interrupted", null, null, null));
                            return;
                        }
                    }
                }
            });
            dispatchers.add(dispatcher);
            dispatcher.start();
        }
    }

    public AbstractMessageBus() {
        this(2);
    }

    public AbstractMessageBus(int dispatcherThreadCount) {
        this(2, new ThreadPoolExecutor(5, 50, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>()));
    }

    public AbstractMessageBus(int dispatcherThreadCount, ExecutorService executor) {
        this.executor = executor;
        initDispatcherThreads(dispatcherThreadCount > 0 ? dispatcherThreadCount : 2);
        addErrorHandler(new ConsoleLogger());
        subscriptionFactory = getSubscriptionFactory();
        initialize();
    }

    protected abstract SubscriptionFactory getSubscriptionFactory();

    protected void initialize(){}

    @Override
    public Collection<IPublicationErrorHandler> getRegisteredErrorHandlers() {
        return Collections.unmodifiableCollection(errorHandlers);
    }

    public void unsubscribe(Object listener) {
        if (listener == null) return;
        Collection<Subscription> subscriptions = subscriptionsPerListener.get(listener.getClass());
        if (subscriptions == null) return;
        for (Subscription subscription : subscriptions) {
            subscription.unsubscribe(listener);
        }
    }


    public void subscribe(Object listener) {
        try {
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
                            Class eventType = getMessageType(messageHandler);
                            Subscription subscription = subscriptionFactory.createSubscription(metadataReader.getHandlerMetadata(messageHandler));
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    public void addErrorHandler(IPublicationErrorHandler handler) {
        errorHandlers.add(handler);
    }

    protected void addAsynchronousDeliveryRequest(SubscriptionDeliveryRequest<T> request) {
        pendingMessages.offer(request);
    }

    // obtain the set of subscriptions for the given message type
    protected Collection<Subscription> getSubscriptionsByMessageType(Class messageType) {
        Set<Subscription> subscriptions = new TreeSet<Subscription>(Subscription.SubscriptionByPriorityDesc);

        if (subscriptionsPerMessage.get(messageType) != null) {
            subscriptions.addAll(subscriptionsPerMessage.get(messageType));
        }
        for (Class eventSuperType : getSuperclasses(messageType)) {
            if (subscriptionsPerMessage.get(eventSuperType) != null) {
                subscriptions.addAll(subscriptionsPerMessage.get(eventSuperType));
            }
        }
        // IMPROVEMENT: use tree list that sorts during insertion
        //Collections.sort(subscriptions, new SubscriptionByPriorityDesc());
        return subscriptions;
    }

    private Collection<Class> getSuperclasses(Class from) {
        Collection<Class> superclasses = new LinkedList<Class>();
        while (!from.equals(Object.class)) {
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


    public void handlePublicationError(PublicationError error) {
        for (IPublicationErrorHandler errorHandler : errorHandlers)
            errorHandler.handleError(error);
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        for (Thread dispatcher : dispatchers) {
            dispatcher.interrupt();
        }
    }

    @Override
    public Executor getExecutor() {
        return executor;
    }

}
