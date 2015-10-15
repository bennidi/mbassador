import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.SyncMessageBus;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;

/**
 * These examples show how to create instances of a message bus using its different constructors.
 *
 */
public class BusConstruction {



    public static void main(String[] args){

        // Create a bus instance configured with reasonable defaults
        // NOTE: Since there is no publication error handler provided, the bus will fall back to
        // ConsoleLogger and print a hint about how to add publication error handlers
        MBassador unboundBus = new MBassador();

        // Create a bus bound to handle messages of type String.class only
        // with a custom publication error handler
        MBassador<String> stringOnlyBus = new MBassador<String>(new IPublicationErrorHandler() {
            @Override
            public void handleError(PublicationError error) {
                // custom error handling logic here
            }
        });


        // Use feature driven configuration to have more control over the configuration details
        MBassador featureDrivenBus = new MBassador(new BusConfiguration()
                .addFeature(Feature.SyncPubSub.Default())
                .addFeature(Feature.AsynchronousHandlerInvocation.Default())
                .addFeature(Feature.AsynchronousMessageDispatch.Default())
                .addPublicationErrorHandler(new IPublicationErrorHandler.ConsoleLogger())
                .setProperty(IBusConfiguration.Properties.BusId, "global bus")); // this is used for identification in #toString

        // The same construction patterns work for the synchronous message bus
        SyncMessageBus synchronousOnly = new SyncMessageBus();
    }
}
