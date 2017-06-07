package net.engio.mbassy.bus;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import net.engio.mbassy.bus.common.IMessageBus;
import net.engio.mbassy.bus.config.ConfigurationError;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.bus.error.InternalPublicationError;
import net.engio.mbassy.bus.publication.ISyncAsyncPublicationCommand;

/**
 * The base class for all message bus implementations with support for asynchronous message dispatch
 *
 * @param <T> The type of message this bus consumes
 * @param <P> The publication commands this bus supports depend on P
 */
public abstract class AbstractSyncAsyncMessageBus<T, P extends ISyncAsyncPublicationCommand>
        extends AbstractPubSubPauseSupport<T> implements IMessageBus<T, P> {

    // executor for asynchronous message handlers
    private final ExecutorService executor;

    // all threads that are available for asynchronous message dispatching
    private final List<Thread> dispatchers;

    // all pending messages scheduled for asynchronous dispatch are queued here
    private final BlockingQueue<IMessagePublication> pendingMessages;

    protected AbstractSyncAsyncMessageBus(final IBusConfiguration configuration) {
        super(configuration);

        // configure asynchronous message dispatch
        final Feature.AsynchronousMessageDispatch asyncDispatch = configuration.getFeature(Feature.AsynchronousMessageDispatch.class);
        if(asyncDispatch == null){
            throw ConfigurationError.MissingFeature(Feature.AsynchronousMessageDispatch.class);
        }
        pendingMessages = asyncDispatch.getMessageQueue();
        dispatchers = new ArrayList<Thread>(asyncDispatch.getNumberOfMessageDispatchers());
        initDispatcherThreads(asyncDispatch);

        // configure asynchronous handler invocation
        final Feature.AsynchronousHandlerInvocation asyncInvocation = configuration.getFeature(Feature.AsynchronousHandlerInvocation.class);
        if(asyncInvocation == null){
            throw ConfigurationError.MissingFeature(Feature.AsynchronousHandlerInvocation.class);
        }
        this.executor = asyncInvocation.getExecutor();
        getRuntime().add(IBusConfiguration.Properties.AsynchronousHandlerExecutor, executor);

    }

    // initialize the dispatch workers
    private void initDispatcherThreads(final Feature.AsynchronousMessageDispatch configuration) {
        for (int i = 0; i < configuration.getNumberOfMessageDispatchers(); i++) {
            // each thread will run forever and process incoming
            // message publication requests
            final Thread dispatcher = configuration.getDispatcherThreadFactory().newThread(new Runnable() {
                @Override
		public void run() {
                    while (true) {
                        IMessagePublication publication = null;
                        try {
                            publication = pendingMessages.take();
                            publication.execute();
                        } catch (final InterruptedException e) {
                            Thread.currentThread().interrupt();
                            return;
                        } catch(Throwable t){
                            handlePublicationError(new InternalPublicationError(t, "Error in asynchronous dispatch",publication));
                        }
                    }
                }
            });
            dispatcher.setName("MsgDispatcher-"+i);
            dispatchers.add(dispatcher);
            dispatcher.start();
        }
    }


    // this method queues a message delivery request
    protected IMessagePublication addAsynchronousPublication(MessagePublication publication) {
        try {
            pendingMessages.put(publication);
            return publication.markScheduled();
        } catch (InterruptedException e) {
            handlePublicationError(new InternalPublicationError(e, "Error while adding an asynchronous message publication", publication));
            return publication;
        }
    }

    // this method queues a message delivery request
    protected IMessagePublication addAsynchronousPublication(MessagePublication publication, long timeout, TimeUnit unit) {
        try {
            return pendingMessages.offer(publication, timeout, unit)
                    ? publication.markScheduled()
                    : publication;
        } catch (InterruptedException e) {
            handlePublicationError(new InternalPublicationError(e, "Error while adding an asynchronous message publication", publication));
            return publication;
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
        shutdown();
    }

    @Override
    public void shutdown() {
        for (final Thread dispatcher : dispatchers) {
            dispatcher.interrupt();
        }
        if(executor != null) executor.shutdown();
    }

    @Override
    public boolean hasPendingMessages() {
        return pendingMessages.size() > 0;
    }

}
