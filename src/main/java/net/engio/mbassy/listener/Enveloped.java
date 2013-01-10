package net.engio.mbassy.listener;

import java.lang.annotation.*;

/**
 * Configure a handler to receive an enveloped message as a wrapper around the source
 * message. An enveloped message can be
 *
 * @author bennidi
 * Date: 2/8/12
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Inherited
@Target(value = {ElementType.METHOD})
public @interface Enveloped {

    /**
     * The set of messages that should be dispatched to the message handler
     */
	Class[] messages();


}
