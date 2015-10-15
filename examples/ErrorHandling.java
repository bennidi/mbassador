import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.bus.error.IPublicationErrorHandler;
import net.engio.mbassy.bus.error.PublicationError;

/**
 *
 * @author bennidi
 *         Date: 07.10.15
 */
public class ErrorHandling {

    static final IPublicationErrorHandler illustrativeHandler =  new IPublicationErrorHandler() {
        @Override
        public void handleError(PublicationError error) {
            error.getMessage(); // An error message to describe what went wrong
            error.getCause(); // The underlying exception
            error.getPublishedMessage(); // The message that was published (can be null)
            error.getListener(); // The listener that was invoked when the execption was thrown (can be null)
            error.getHandler(); // The message handler (Method) that was invoked when the execption was thrown (can be null)
        }
    };


    public static void main(String[] args){

        // An error handler can be passed as constructor argument
        MBassador bus =  new MBassador<String>(illustrativeHandler);


        // ...or as part of a configuration object
        MBassador featureDrivenBus = new MBassador(new BusConfiguration()
                .addFeature(Feature.SyncPubSub.Default())
                .addFeature(Feature.AsynchronousHandlerInvocation.Default())
                .addFeature(Feature.AsynchronousMessageDispatch.Default())
                .addPublicationErrorHandler(new IPublicationErrorHandler.ConsoleLogger()) // <-- This is a default handlers
                .addPublicationErrorHandler(illustrativeHandler) // <-- It is possible to add multiple handlers
                .setProperty(IBusConfiguration.Properties.BusId, "global bus")); // this is used for identification in #toString
    }
}
