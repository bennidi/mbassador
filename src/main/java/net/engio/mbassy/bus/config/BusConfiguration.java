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

    public static BusConfiguration Default() {
        BusConfiguration defaultConfig = new BusConfiguration();
        int numberOfCoreThreads = Runtime.getRuntime().availableProcessors();
        defaultConfig.setExecutorForAsynchronousHandlers(new ThreadPoolExecutor(numberOfCoreThreads, numberOfCoreThreads*2, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(), AsynchronousHandlerThreadFactory));
        defaultConfig.setMetadataReader(new MetadataReader());
        defaultConfig.setSubscriptionFactory(new SubscriptionFactory());
        defaultConfig.setNumberOfMessageDispatchers(2);
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
