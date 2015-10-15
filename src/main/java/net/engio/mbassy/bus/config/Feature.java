package net.engio.mbassy.bus.config;

import net.engio.mbassy.bus.IMessagePublication;
import net.engio.mbassy.bus.MessagePublication;
import net.engio.mbassy.listener.MetadataReader;
import net.engio.mbassy.subscription.ISubscriptionManagerProvider;
import net.engio.mbassy.subscription.SubscriptionFactory;
import net.engio.mbassy.subscription.SubscriptionManagerProvider;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A feature defines the configuration of a specific functionality of a message bus.
 *
 * @author bennidi
 *         Date: 8/29/14
 */
public interface Feature {


    class SyncPubSub implements Feature{

        public static final SyncPubSub Default(){
            return new SyncPubSub()
                    .setMetadataReader(new MetadataReader())
                    .setPublicationFactory(new MessagePublication.Factory())
                    .setSubscriptionFactory(new SubscriptionFactory())
                    .setSubscriptionManagerProvider(new SubscriptionManagerProvider());
        }

        private MessagePublication.Factory publicationFactory;
        private MetadataReader metadataReader;
        private SubscriptionFactory subscriptionFactory;
        private ISubscriptionManagerProvider subscriptionManagerProvider;

        public ISubscriptionManagerProvider getSubscriptionManagerProvider() {
            return subscriptionManagerProvider;
        }

        public SyncPubSub setSubscriptionManagerProvider(ISubscriptionManagerProvider subscriptionManagerProvider) {
            this.subscriptionManagerProvider = subscriptionManagerProvider;
            return this;
        }

        public SubscriptionFactory getSubscriptionFactory() {
            return subscriptionFactory;
        }

        public SyncPubSub setSubscriptionFactory(SubscriptionFactory subscriptionFactory) {
            this.subscriptionFactory = subscriptionFactory;
            return this;
        }

        public MetadataReader getMetadataReader() {
            return metadataReader;
        }

        public SyncPubSub setMetadataReader(MetadataReader metadataReader) {
            this.metadataReader = metadataReader;
            return this;
        }

        /**
         * The message publication factory is used to wrap a published message
         * in a {@link MessagePublication} for processing.
         * @return The factory to be used by the bus to create the publications
         */
        public MessagePublication.Factory getPublicationFactory() {
            return publicationFactory;
        }

        public SyncPubSub setPublicationFactory(MessagePublication.Factory publicationFactory) {
            this.publicationFactory = publicationFactory;
            return this;
        }
    }

    class AsynchronousHandlerInvocation implements Feature{

        protected static final ThreadFactory MessageHandlerThreadFactory = new ThreadFactory() {

            private final AtomicInteger threadID = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = Executors.defaultThreadFactory().newThread(r);
                thread.setName("AsyncHandler-" + threadID.getAndIncrement());
                thread.setDaemon(true);
                return thread;
            }
        };

        public static final AsynchronousHandlerInvocation Default(){
            int numberOfCores = Runtime.getRuntime().availableProcessors();
            return Default(numberOfCores, numberOfCores * 2);
        }

        public static final AsynchronousHandlerInvocation Default(int minThreadCount, int maxThreadCount){
            return new AsynchronousHandlerInvocation().setExecutor(new ThreadPoolExecutor(minThreadCount, maxThreadCount, 1,
                    TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(), MessageHandlerThreadFactory));
        }

        private ExecutorService executor;

        public ExecutorService getExecutor() {
            return executor;
        }

        public AsynchronousHandlerInvocation setExecutor(ExecutorService executor) {
            this.executor = executor;
            return this;
        }
    }

    class AsynchronousMessageDispatch implements Feature{

        protected static final ThreadFactory MessageDispatchThreadFactory = new ThreadFactory() {

            private final AtomicInteger threadID = new AtomicInteger(0);

            @Override
            public Thread newThread(Runnable r) {
                Thread thread = Executors.defaultThreadFactory().newThread(r);
                thread.setDaemon(true);// do not prevent the JVM from exiting
                thread.setName("Dispatcher-" + threadID.getAndIncrement());
                return thread;
            }
        };

        public static final AsynchronousMessageDispatch Default(){
            return new AsynchronousMessageDispatch()
                .setNumberOfMessageDispatchers(2)
                .setDispatcherThreadFactory(MessageDispatchThreadFactory)
                .setMessageQueue(new LinkedBlockingQueue<IMessagePublication>(Integer.MAX_VALUE));
        }


        private int numberOfMessageDispatchers;
        private BlockingQueue<IMessagePublication> messageQueue;
        private ThreadFactory dispatcherThreadFactory;

        public int getNumberOfMessageDispatchers() {
            return numberOfMessageDispatchers;
        }

        public AsynchronousMessageDispatch setNumberOfMessageDispatchers(int numberOfMessageDispatchers) {
            this.numberOfMessageDispatchers = numberOfMessageDispatchers;
            return this;
        }

        public BlockingQueue<IMessagePublication> getMessageQueue() {
            return messageQueue;
        }

        public AsynchronousMessageDispatch setMessageQueue(BlockingQueue<IMessagePublication> pendingMessages) {
            this.messageQueue = pendingMessages;
            return this;
        }

        public ThreadFactory getDispatcherThreadFactory() {
            return dispatcherThreadFactory;
        }

        public AsynchronousMessageDispatch setDispatcherThreadFactory(ThreadFactory dispatcherThreadFactory) {
            this.dispatcherThreadFactory = dispatcherThreadFactory;
            return this;
        }
    }


}
