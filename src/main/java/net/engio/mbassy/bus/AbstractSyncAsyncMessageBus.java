package net.engio.mbassy.bus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import net.engio.mbassy.IMessageBus;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.bus.error.PublicationError;

/**
 * The base class for all message bus implementations with support for asynchronous message dispatch
 */
public abstract class AbstractSyncAsyncMessageBus<T>
        extends AbstractPubSubSupport<T> implements IMessageBus<T> {

    // executor for asynchronous message handlers
    private final ExecutorService executor;

    // all threads that are available for asynchronous message dispatching
    private final List<Thread> dispatchers;

    // all pending messages scheduled for asynchronous dispatch are queued here
    private final BlockingQueue<T> pendingMessages = new LinkedBlockingQueue<T>(Integer.MAX_VALUE/16);

    protected AbstractSyncAsyncMessageBus(IBusConfiguration configuration) {
        super(configuration);

        // configure asynchronous message dispatch
        Feature.AsynchronousMessageDispatch asyncDispatch = configuration.getFeature(Feature.AsynchronousMessageDispatch.class);
        this.dispatchers = new ArrayList<Thread>(asyncDispatch.getNumberOfMessageDispatchers());
        initDispatcherThreads(asyncDispatch);

        // configure asynchronous handler invocation
        Feature.AsynchronousHandlerInvocation asyncInvocation = configuration.getFeature(Feature.AsynchronousHandlerInvocation.class);
        this.executor = asyncInvocation.getExecutor();
    }

    // initialize the dispatch workers
    private void initDispatcherThreads(Feature.AsynchronousMessageDispatch configuration) {
        for (int i = 0; i < configuration.getNumberOfMessageDispatchers(); i++) {
            // each thread will run forever and process incoming
            // message publication requests
            Thread dispatcher = configuration.getDispatcherThreadFactory().newThread(new Runnable() {
                @Override
                public void run() {
                    T message = null;
                    while (true) {
                        try {
                            message = AbstractSyncAsyncMessageBus.this.pendingMessages.take();
                            publishMessage(message);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        } catch(Throwable t){
                            handlePublicationError(new PublicationError(t, "Error in asynchronous dispatch", message));
                        }
                    }
                }
            });
            dispatcher.setName("Message dispatcher");
            this.dispatchers.add(dispatcher);
            dispatcher.start();
        }
    }


    // this method queues a message delivery request
    protected void addAsynchronousPublication(T message) {
        try {
            this.pendingMessages.put(message);
        } catch (InterruptedException e) {
            handlePublicationError(new PublicationError(e, "Error while adding an asynchronous message publication", message));
        }
    }

    // this method queues a message delivery request
    protected void addAsynchronousPublication(T message, long timeout, TimeUnit unit) {
        try {
            this.pendingMessages.offer(message, timeout, unit);
        } catch (InterruptedException e) {
            handlePublicationError(new PublicationError(e, "Error while adding an asynchronous message publication", message));
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        shutdown();
    }

    @Override
    public void shutdown() {
        for (Thread dispatcher : this.dispatchers) {
            dispatcher.interrupt();
        }
        if(this.executor != null) {
            this.executor.shutdown();
        }
    }

    @Override
    public boolean hasPendingMessages() {
        return this.pendingMessages.size() > 0;
    }

    @Override
    public Executor getExecutor() {
        return this.executor;
    }

}
