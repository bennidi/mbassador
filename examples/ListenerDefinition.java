import net.engio.mbassy.dispatch.HandlerInvocation;
import net.engio.mbassy.listener.*;
import net.engio.mbassy.subscription.MessageEnvelope;
import net.engio.mbassy.subscription.SubscriptionContext;

import java.io.File;
import java.lang.annotation.*;

/**
 * These examples show how to configure listeners and handlers based on the available configuration options.
 *
 * NOTE: The presented handler configurations compose very well because they are implemented using decorator pattern.
 *
 */
public class ListenerDefinition {


    /**
     * By default any listener will be stored using weak references {@link java.lang.ref.WeakReference}.
     * This implies that at any give point in time, listeners might be GC'ed if no other alive object holds a reference to it.
     *
     * In managed environments where object lifecycle is controlled
     * by a framework  (Spring, Guice etc.) this is a very handy feature because no attentions needs
     * to be paid to proper unsubscription of listeners that have ended their life (session scoped beans etc.)
     *
     * NOTE: There is no dedicated maintenance task running to take care of GC'ed listeners.
     * Automatic cleanup of orphaned weak references is an embedded process done during message publication.
     *
     */
    static class WeaklyReferencedListener{
        // Handler definitions go here
    }


    /**
     * In case that there is no other mechanism managing references to the listeners and they should
     * just stick around until explicitly unsubscribed, listener classes need to be annotated accordingly.
     */
    @Listener(references = References.Strong)
    static class StronglyReferencedListener{
        // This listener will stay subscribed until explicitly unsubscribed
    }


    /**
     * This listeners demonstrates the very basic use cases of synchronous and asynchronous handler definitions.
     *
     */
    static class SyncAsyncListener{

        /**
         * Any published message will be delivered to this handler (as it consumes any object of type Object.class)
         * Delivery is done using synchronous invocation, i.e. the handler is called from the thread running the message
         * publication.
         *
         */
        @Handler
        public void synchronousHandler(Object message) {
            // do something
        }

        /**
         * According to the handler configuration, this handler is invoked asynchronously, meaning that each handler
         * invocation runs in a thread different from the one that runs the initial message publication.
         *
         * This feature is useful for computationally expensive or IO-bound tasks.
         *
         */
        @Handler(delivery = Invoke.Asynchronously)
        public void asynchronousHandler(File message) {
            // do something more expensive here
        }

    }

    static class FilteringListener{


        /**
         * This handler consumes only strings (as there are no subtypes of final String.class).
         * Furthermore, each string is passed through the list of defined filters and the handler is
         * invoked only if all filters pass. In this case, only strings starting with 'http' will be handled.
         *
         */
        @Handler(delivery = Invoke.Synchronously,
                filters = {@Filter(Urlfilter.class)})
        public void httpUrlsOnly(String message) {

        }

        /**
         * Another way of controlling which messages are delivered to handlers is by using JUEL expressions.
         * These can be specified as conditions (no type checking etc.) and will be evaluated on the msg.
         * This particular condition will filter out all empty strings
         */
        @Handler(condition = "!msg.isEmpty()")
        public void handleNonEmptyStrings(String msg) {
        }

        /**
         *
         */
        @Handler(delivery = Invoke.Synchronously, rejectSubtypes = true)
        @Enveloped(messages = {Object.class, String.class})
        public void handleUnrelatedMessageTypes(MessageEnvelope envelope) {
            // the envelope will contain either an instance of Object.class or String.class
            // if rejectSubtypes were set to 'false' (default) also subtypes of TestMessage or TestMessage2 would be allowed
        }

        static class Urlfilter implements IMessageFilter<String>{
            public boolean accepts(String message, SubscriptionContext context){
                return message.startsWith("http");
            }
        }

    }



    /**
     *  Listeners can use custom code to invoke their handlers. Custom invocation logic is defined on a per-handler
     *  basis (as the signature requires knowledge about concrete handler and message type).
     */
    @Listener(references = References.Strong)
    static class CustomInvocationListener {

        @Handler(invocation = TimingInvocation.class)
        public void handle(File message) {
            // do timed operation here
        }

        public static class TimingInvocation extends HandlerInvocation<CustomInvocationListener, File> {

            public TimingInvocation(SubscriptionContext context) {
                super(context);
            }

            @Override
            public void invoke(CustomInvocationListener listener, File message) {
                long start = System.currentTimeMillis();
                listener.handle(message);
                long duration = System.currentTimeMillis() - start;
                System.out.println("Time takes for handler invocation: " + duration + " ms");
            }
        }

    }


    /**
     * Handler annotation that adds a condition checking for positive integers only
     */
    @Retention(value = RetentionPolicy.RUNTIME)
    @Inherited
    @Handler(condition = "msg.getClass() == Integer.class && msg > 0")
    @Synchronized
    @Target(value = { ElementType.METHOD, ElementType.ANNOTATION_TYPE })
    @interface SynchronizedPositiveIntegers{}

    static class ListenerWithCustomAnnotation{

        @SynchronizedPositiveIntegers
        public void handlePositiveIntegers(Integer msg){

        }

    }

}
