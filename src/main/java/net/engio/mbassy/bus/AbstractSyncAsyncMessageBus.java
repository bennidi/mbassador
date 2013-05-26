package net.engio.mbassy.bus;

import net.engio.mbassy.PublicationError;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * The base class for all async message bus implementations.
 *
 * @param <T>
 * @param <P>
 */
public abstract class AbstractSyncAsyncMessageBus<T, P extends IMessageBus.IPostCommand> extends AbstractSyncMessageBus<T, P> implements IMessageBus<T, P> {

    // executor for asynchronous message handlers
    private final ExecutorService executor;

    // all threads that are available for asynchronous message dispatching
    private final List<Thread> dispatchers;

    // all pending messages scheduled for asynchronous dispatch are queued here
    private final BlockingQueue<MessagePublication> pendingMessages;

    public AbstractSyncAsyncMessageBus(BusConfiguration configuration) {
        super(configuration);
        this.executor = configuration.getExecutor();
        pendingMessages = new LinkedBlockingQueue<MessagePublication>(configuration.getMaximumNumberOfPendingMessages());
         dispatchers = new ArrayList<Thread>(configuration.getNumberOfMessageDispatchers());
        initDispatcherThreads(configuration.getNumberOfMessageDispatchers());
    }


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
                            Thread.currentThread().interrupt();
                            return;
                        } catch(Throwable t){
                            handlePublicationError(new PublicationError(t, "Error in asynchronous dispatch", null, null, null));
                        }
                    }
                }
            });
            dispatcher.setDaemon(true); // do not prevent the JVM from exiting
            dispatchers.add(dispatcher);
            dispatcher.start();
        }
    }


    // this method enqueues a message delivery request
    protected MessagePublication addAsynchronousDeliveryRequest(MessagePublication request) {
        try {
            pendingMessages.put(request);
            return request.markScheduled();
        } catch (InterruptedException e) {
            return request.setError();
        }
    }

    // this method queues a message delivery request
    protected MessagePublication addAsynchronousDeliveryRequest(MessagePublication request, long timeout, TimeUnit unit) {
        try {
            return pendingMessages.offer(request, timeout, unit)
                    ? request.markScheduled()
                    : request.setError();
        } catch (InterruptedException e) {
            return request.setError();
        }
    }

    @Override
    protected void finalize() throws Throwable {
        shutdown();
        super.finalize();
    }

    public void shutdown() {
        for (Thread dispatcher : dispatchers) {
            dispatcher.interrupt();
        }
        executor.shutdown();
    }

    public boolean hasPendingMessages() {
        return pendingMessages.size() > 0;
    }

    @Override
    public Executor getExecutor() {
        return executor;
    }

}
