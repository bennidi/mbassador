package org.mbassy;

import org.mbassy.listener.MetadataReader;
import org.mbassy.subscription.SubscriptionFactory;

import java.util.concurrent.*;

/**
 *
 *
 *
 * @author bennidi
 *         Date: 12/8/12
 */
public class BusConfiguration {

    public static final BusConfiguration Default(){
        return new BusConfiguration();
    }

    private int numberOfMessageDispatchers;

    private ExecutorService executor;

    private int maximumNumberOfPendingMessages;

    private SubscriptionFactory subscriptionFactory;

    private MetadataReader metadataReader;

    public BusConfiguration() {
        this.numberOfMessageDispatchers = 2;
        this.maximumNumberOfPendingMessages = Integer.MAX_VALUE;
        this.subscriptionFactory = new SubscriptionFactory();
        this.executor = new ThreadPoolExecutor(5, 20, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());
        this.metadataReader = new MetadataReader();
    }

    public MetadataReader getMetadataReader() {
        return metadataReader;
    }

    public BusConfiguration setMetadataReader(MetadataReader metadataReader) {
        this.metadataReader = metadataReader;
        return this;
    }

    public int getNumberOfMessageDispatchers() {
        return numberOfMessageDispatchers > 0 ? numberOfMessageDispatchers : 2;
    }

    public BusConfiguration setNumberOfMessageDispatchers(int numberOfMessageDispatchers) {
        this.numberOfMessageDispatchers = numberOfMessageDispatchers;
        return this;
    }

    public ExecutorService getExecutor() {
        return executor;
    }

    public BusConfiguration setExecutor(ExecutorService executor) {
        this.executor = executor;
        return this;
    }

    public int getMaximumNumberOfPendingMessages() {
        return maximumNumberOfPendingMessages;
    }

    public BusConfiguration setMaximumNumberOfPendingMessages(int maximumNumberOfPendingMessages) {
        this.maximumNumberOfPendingMessages = maximumNumberOfPendingMessages > 0
                ? maximumNumberOfPendingMessages
                : Integer.MAX_VALUE;
        return this;
    }

    public SubscriptionFactory getSubscriptionFactory() {
        return subscriptionFactory;
    }

    public BusConfiguration setSubscriptionFactory(SubscriptionFactory subscriptionFactory) {
        this.subscriptionFactory = subscriptionFactory;
        return this;
    }
}
