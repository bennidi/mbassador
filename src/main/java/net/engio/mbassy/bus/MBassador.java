package net.engio.mbassy.bus;

import net.engio.mbassy.bus.common.IMessageBus;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;
import net.engio.mbassy.bus.publication.SyncAsyncPostCommand;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class MBassador<T> extends AbstractSyncAsyncMessageBus<T, SyncAsyncPostCommand<T>> implements IMessageBus<T,
        SyncAsyncPostCommand<T>> {

    private HashMap<Object, IMessagePublication> publications = new HashMap<Object, IMessagePublication>();


    /**
     * Default constructor using default setup. super() will also add a default publication error logger
     */
    public MBassador() {
        this(new BusConfiguration().addFeature(Feature.SyncPubSub.Default())
                                   .addFeature(Feature.AsynchronousHandlerInvocation.Default())
                                   .addFeature(Feature.AsynchronousMessageDispatch.Default()));
    }

    /**
     * Construct with default settings and specified publication error handler
     *
     * @param errorHandler
     */
    public MBassador(IPublicationErrorHandler errorHandler) {
        super(new BusConfiguration().addFeature(Feature.SyncPubSub.Default())
                                    .addFeature(Feature.AsynchronousHandlerInvocation.Default())
                                    .addFeature(Feature.AsynchronousMessageDispatch.Default())
                                    .addPublicationErrorHandler(errorHandler));
    }

    /**
     * Construct with fully specified configuration
     *
     * @param configuration
     */
    public MBassador(IBusConfiguration configuration) {
        super(configuration);
    }


    public IMessagePublication publishAsync(T message) {
        updatePublications();
        IMessagePublication publication = addAsynchronousPublication(createMessagePublication(message));
        publications.put(message, publication);
        return publication;
    }

    public IMessagePublication publishAsync(T message, long timeout, TimeUnit unit) {
        updatePublications();
        IMessagePublication publication = addAsynchronousPublication(createMessagePublication(message), timeout, unit);
        publications.put(message, publication);
        return publication;
    }


    /**
     * Synchronously publish a message to all registered listeners (this includes listeners defined for super types)
     * The call blocks until every messageHandler has processed the message.
     *
     * @param message
     */
    public void publish(T message) {
        updatePublications();
        try {
            IMessagePublication publication = createMessagePublication(message);
            publications.put(message, publication);
            publication.execute();
        } catch (Throwable e) {
            handlePublicationError(new PublicationError().setMessage("Error during publication of message")
                                                         .setCause(e)
                                                         .setPublishedMessage(message));
        }

    }


    @Override
    public SyncAsyncPostCommand<T> post(T message) {
        updatePublications();
        return new SyncAsyncPostCommand<T>(this, message);
    }

    public void cancel(T message) {
        IMessagePublication publication = publications.get(message);

        if (publication != null) {
            publication.markCancelled();
        }
    }

    private void updatePublications() {
        Iterator iter = publications.entrySet().iterator();
        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();
            if (((IMessagePublication) entry.getValue()).isFinished()) {
                iter.remove();
            }
        }
    }
}
