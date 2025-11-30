package net.engio.mbassy.bus;

import java.util.List;
import net.engio.mbassy.bus.common.IMessageBus;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;
import net.engio.mbassy.bus.publication.SyncAsyncPostCommand;

import java.util.concurrent.TimeUnit;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.scan.ClassFileHandlerScanner;


public class MBassador<T> extends AbstractSyncAsyncMessageBus<T, SyncAsyncPostCommand<T>> implements IMessageBus<T, SyncAsyncPostCommand<T>> {


    /**
     * Default constructor using default setup. super() will also add a default publication error logger
     */
    public MBassador(){
        this(new BusConfiguration()
                .addFeature(Feature.SyncPubSub.Default())
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

    /**
     * Auto-scans default package for listeners and subscribes them automatically.
     * This provides zero-configuration capability.
     */
    public void autoScan() {
        autoScan("");
    }

    /**
     * Auto-scans specified packages for listeners and subscribes them automatically.
     *
     * @param packages Packages to scan (e.g., "com.example.listeners", "org.myapp.handlers")
     */
    public void autoScan(String... packages) {
        try {
            ClassFileHandlerScanner scanner = new ClassFileHandlerScanner();
            for (String pkg : packages) {
                List<Class<?>> listenerClasses = scanner.scanPackage(pkg);
                for (Class<?> listenerClass : listenerClasses) {
                    try {
                        Listener listenerAnn = listenerClass.getAnnotation(Listener.class);
                        if (listenerAnn != null && !listenerAnn.autoScan()) {
                            // Explicitly opted out of auto-scan
                            continue;
                        }

                        // Check for default constructor before attempting to use it
                        listenerClass.getDeclaredConstructor(); // throws if none

                        Object listener = listenerClass.getDeclaredConstructor().newInstance();
                        subscribe(listener);
                        System.out.println("Auto-subscribed listener: " + listenerClass.getName());
                    } catch (NoSuchMethodException e) {
                        // No default constructor - skip silently or log once
                        System.err.println("Skipping listener without default constructor: " +
                                               listenerClass.getName());
                    } catch (Exception e) {
                        System.err.println("Failed to instantiate and subscribe listener: " +
                                               listenerClass.getName() + " - " + e.getMessage());
                    }
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Auto-scan failed", e);
        }
    }

    public IMessagePublication publishAsync(T message) {
        return addAsynchronousPublication(createMessagePublication(message));
    }

    public IMessagePublication publishAsync(T message, long timeout, TimeUnit unit) {
        return addAsynchronousPublication(createMessagePublication(message), timeout, unit);
    }


    /**
     * Synchronously publish a message to all registered listeners (this includes listeners defined for super types)
     * The call blocks until every messageHandler has processed the message.
     *
     * @param message
     */
    public IMessagePublication publish(T message) {
        IMessagePublication publication = createMessagePublication(message);
        try {
            publication.execute();
        } catch (Throwable e) {
            handlePublicationError(new PublicationError()
                    .setMessage("Error during publication of message")
                    .setCause(e)
                    .setPublication(publication));
        }
        finally{
            return publication;
        }
    }


    @Override
    public SyncAsyncPostCommand<T> post(T message) {
        return new SyncAsyncPostCommand<T>(this, message);
    }

}
