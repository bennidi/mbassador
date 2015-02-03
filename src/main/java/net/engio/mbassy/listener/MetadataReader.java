package net.engio.mbassy.listener;

import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

import net.engio.mbassy.annotations.Handler;
import net.engio.mbassy.common.ReflectionUtils;

/**
 * The meta data reader is responsible for parsing and validating message handler configurations.
 *
 * @author bennidi
 *         Date: 11/16/12
 */
public class MetadataReader {

    // get all listeners defined by the given class (includes
    // listeners defined in super classes)
    public MessageListener getMessageListener(Class<?> target) {

        // get all handlers (this will include all (inherited) methods directly annotated using @Handler)
        List<Method> allHandlers = ReflectionUtils.getMethods(target);

        // retain only those that are at the bottom of their respective class hierarchy (deepest overriding method)
        List<Method> bottomMostHandlers = new LinkedList<Method>();
        for (Method handler : allHandlers) {
            if (!ReflectionUtils.containsOverridingMethod(allHandlers, handler)) {
                bottomMostHandlers.add(handler);
            }
        }

        MessageListener listenerMetadata = new MessageListener(target);

        // for each handler there will be no overriding method that specifies @Handler annotation
        // but an overriding method does inherit the listener configuration of the overwritten method
        for (Method handler : bottomMostHandlers) {
            Handler handlerConfig = ReflectionUtils.getAnnotation( handler, Handler.class);
            if (!handlerConfig.enabled() || !isValidMessageHandler(handler)) {
                continue; // disabled or invalid listeners are ignored
            }

            Method overriddenHandler = ReflectionUtils.getOverridingMethod(handler, target);
            if (overriddenHandler == null) {
                overriddenHandler = handler;
            }

            // if a handler is overwritten it inherits the configuration of its parent method
            MessageHandler handlerMetadata = new MessageHandler(overriddenHandler, handlerConfig, listenerMetadata);
            listenerMetadata.addHandler(handlerMetadata);

        }
        return listenerMetadata;
    }



//TODO: change this to support MORE THAN ONE object in the signature
    private boolean isValidMessageHandler(Method handler) {
        if (handler == null || ReflectionUtils.getAnnotation( handler, Handler.class) == null) {
            return false;
        }
        if (handler.getParameterTypes().length != 1) {
            // a messageHandler only defines one parameter (the message)
            System.out.println("Found no or more than one parameter in messageHandler [" + handler.getName()
                    + "]. A messageHandler must define exactly one parameter");
            return false;
        }

        return true;
    }

}
