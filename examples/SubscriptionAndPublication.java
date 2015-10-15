import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;

import java.io.File;

/**
 *
 * @author bennidi
 *         Date: 07.10.15
 */
public class SubscriptionAndPublication {

    static MBassador bus = new MBassador(new BusConfiguration()
            .addFeature(Feature.SyncPubSub.Default())
            .addFeature(Feature.AsynchronousHandlerInvocation.Default())
            .addFeature(Feature.AsynchronousMessageDispatch.Default())
            .addPublicationErrorHandler(new IPublicationErrorHandler.ConsoleLogger())
            .setProperty(IBusConfiguration.Properties.BusId, "global bus")); // this is used for identification in #toString

    public static void main(String[] args){

        // Listeners are subscribed by passing them to the #subscribe() method
        bus.subscribe(new ListenerDefinition.SyncAsyncListener());

        // #subscribe() is idem-potent => Multiple calls to subscribe do NOT add the listener more than once (set semantics)
        Object listener = new ListenerDefinition.SyncAsyncListener();
        bus.subscribe(listener);
        bus.subscribe(listener);

        // Classes without handlers will be silently ignored
        bus.subscribe(new Object());
        bus.subscribe(new String());

        bus.publishAsync(new File("/tmp/random.csv")); //returns immediately, publication will continue asynchronously
        bus.post(new File("/tmp/random.csv")).asynchronously(); // same as above

        bus.publish("some message");   // will return after each handler has been invoked
        bus.post("some message").now(); // same as above


    }
}
