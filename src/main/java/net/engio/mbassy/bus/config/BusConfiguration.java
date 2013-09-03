package net.engio.mbassy.bus.config;

import net.engio.mbassy.bus.MessagePublication;
import net.engio.mbassy.listener.MetadataReader;
import net.engio.mbassy.subscription.SubscriptionFactory;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * The bus configuration holds various parameters that can be used to customize the bus' runtime behaviour.
 *
 * @author bennidi
 *         Date: 12/8/12
 */
public class BusConfiguration implements IBusConfiguration {

    protected static final ThreadFactory AsynchronousHandlerThreadFactory = new ThreadFactory() {

        private final AtomicInteger threadID = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
            thread.setName("AsyncHandler-" + threadID.getAndIncrement());
            thread.setDaemon(true);
            return thread;
        }
    };

    protected static final ThreadFactory DispatcherThreadFactory = new ThreadFactory() {

        private final AtomicInteger threadID = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
            thread.setDaemon(true);// do not prevent the JVM from exiting
            thread.setName("Dispatcher-" + threadID.getAndIncrement());
            return thread;
        }
    };

    /**
     * Creates a new instance, using the Default settings of 2 dispatchers, and
     * asynchronous handlers with an initial count equal to the number of
     * available processors in the machine, with maximum count equal to
     * 2 * the number of available processors. Uses {@link Runtime#availableProcessors()} to
     * determine the number of available processors
     * 
     * @return a Default BusConfiguration
     */
    public static BusConfiguration Default() {
    	return Default(2);
    }

    /**
     * Creates a new instance, using the specified number of dispatchers, and
     * asynchronous handlers with an initial count equal to the number of
     * available processors in the machine, with maximum count equal to
     * 2 * the number of available processors. Uses {@link Runtime#availableProcessors()} to
     * determine the number of available processors
     * 
     * @return a Default BusConfiguration
     */
    public static BusConfiguration Default(int numberOfDispatchers) {
        int numberOfCoreThreads = Runtime.getRuntime().availableProcessors();
        return Default(numberOfDispatchers, numberOfCoreThreads, numberOfCoreThreads * 2);
    }
    
    /**
     * Creates a new instance, using the specified number of dispatchers, and
     * asynchronous handlers with initial threads and maximum threads specified by the calling
     * parameters.
     * 
     * @return a Default BusConfiguration
     */
    public static BusConfiguration Default(int numberOfDispatchers, int initialCoreThreads, int maximumCoreThreads) {
    	ThreadPoolExecutor executor = new ThreadPoolExecutor(initialCoreThreads, maximumCoreThreads, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(), AsynchronousHandlerThreadFactory);
    	return Default(numberOfDispatchers, executor);
    }
    
    /**
     * Creates a new instance, using the specified number of dispatchers, and
     * asynchronous handlers that use the provided ThreadPoolExecutor.
     * 
     * @return a Default BusConfiguration
     */
    public static BusConfiguration Default(int numberOfDispatchers, ThreadPoolExecutor executor) {
        BusConfiguration defaultConfig = new BusConfiguration();
        defaultConfig.setExecutorForAsynchronousHandlers(executor);
        defaultConfig.setMetadataReader(new MetadataReader());
        defaultConfig.setSubscriptionFactory(new SubscriptionFactory());
        defaultConfig.setNumberOfMessageDispatchers(numberOfDispatchers);
        defaultConfig.setMessagePublicationFactory(new MessagePublication.Factory());
        defaultConfig.setPendingMessagesQueue(new LinkedBlockingQueue<MessagePublication>(Integer.MAX_VALUE));
        defaultConfig.setThreadFactoryForAsynchronousMessageDispatch(DispatcherThreadFactory);
        return defaultConfig;
    }

    public static BusConfiguration Empty(){
        return new BusConfiguration();
    }

    protected int numberOfMessageDispatchers;
    protected ExecutorService executor;
    protected SubscriptionFactory subscriptionFactory;
    protected MetadataReader metadataReader;
    protected MessagePublication.Factory messagePublicationFactory;
    protected ThreadFactory dispatcherThreadFactory;

    public void setPendingMessagesQueue(BlockingQueue<MessagePublication> pendingMessagesQueue) {
        this.pendingMessagesQueue = pendingMessagesQueue;
    }

    protected BlockingQueue<MessagePublication> pendingMessagesQueue;

    private BusConfiguration() {
        super();
    }

    @Override
    public int getNumberOfMessageDispatchers() {
        return numberOfMessageDispatchers > 0 ? numberOfMessageDispatchers : 2;
    }

    public BusConfiguration setNumberOfMessageDispatchers(int numberOfMessageDispatchers) {
        this.numberOfMessageDispatchers = numberOfMessageDispatchers;
        return this;
    }


    @Override
    public ExecutorService getExecutorForAsynchronousHandlers() {
        return executor;
    }

    @Override
    public BlockingQueue<MessagePublication> getPendingMessagesQueue() {
        return new LinkedBlockingQueue<MessagePublication>(Integer.MAX_VALUE);
    }

    @Override
    public ThreadFactory getThreadFactoryForAsynchronousMessageDispatch() {
        return dispatcherThreadFactory;
    }

    public BusConfiguration setThreadFactoryForAsynchronousMessageDispatch(ThreadFactory factory) {
        dispatcherThreadFactory = factory;
        return this;
    }

    public BusConfiguration setExecutorForAsynchronousHandlers(ExecutorService executor) {
        this.executor = executor;
        return this;
    }

    @Override
    public MessagePublication.Factory getMessagePublicationFactory() {
        return messagePublicationFactory;
    }

    public BusConfiguration setMessagePublicationFactory(MessagePublication.Factory messagePublicationFactory) {
        this.messagePublicationFactory = messagePublicationFactory;
        return this;
    }

    @Override
    public MetadataReader getMetadataReader() {
        return metadataReader;
    }

    public void setMetadataReader(MetadataReader metadataReader) {
        this.metadataReader = metadataReader;
    }

    @Override
    public SubscriptionFactory getSubscriptionFactory() {
        return subscriptionFactory;
    }

    public BusConfiguration setSubscriptionFactory(SubscriptionFactory subscriptionFactory) {
        this.subscriptionFactory = subscriptionFactory;
        return this;
    }

}
