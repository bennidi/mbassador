package net.engio.mbassy.bus;

import java.util.concurrent.*;

/**
 * The bus configuration holds various parameters that can be used to customize the bus' runtime behaviour.
 *
 * @author bennidi
 *         Date: 12/8/12
 */
public class BusConfiguration extends SyncBusConfiguration<BusConfiguration> {

    private static final ThreadFactory DaemonThreadFactory = new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Thread thread = Executors.defaultThreadFactory().newThread(r);
            thread.setDaemon(true);
            return thread;
        }
    };

    public static BusConfiguration Default() {
        return new BusConfiguration();
    }

    private int numberOfMessageDispatchers;

    private ExecutorService executor;

    private int maximumNumberOfPendingMessages;

    public BusConfiguration() {
        super();
        this.numberOfMessageDispatchers = 2;
        this.maximumNumberOfPendingMessages = Integer.MAX_VALUE;
        this.executor = new ThreadPoolExecutor(10, 10, 1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>(), DaemonThreadFactory);
    }

    public int getNumberOfMessageDispatchers() {
        return numberOfMessageDispatchers > 0 ? numberOfMessageDispatchers : 2;
    }

    public BusConfiguration setNumberOfMessageDispatchers(int numberOfMessageDispatchers) {
        this.numberOfMessageDispatchers = numberOfMessageDispatchers;
        return this;
    }

    /**
     * By default an unbound queuing strategy is used to ensure that no events get lost
     * @return
     */
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

}
