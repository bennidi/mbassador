package net.engio.mbassy.dispatch;

import com.esotericsoftware.reflectasm.MethodAccess;

/**
 * Uses reflection to invoke a message handler for a given message.
 *
 * @author bennidi
 *         Date: 11/23/12
 * @author dorkbox, llc
 *         Date: 2/2/15
 */
public class ReflectiveHandlerInvocation implements IHandlerInvocation {

    public ReflectiveHandlerInvocation() {
        super();
    }

    @Override
    public void invoke(final Object listener, final MethodAccess handler, final int methodIndex, final Object message) throws Throwable {
        handler.invoke(listener, methodIndex, message);
    }

    @Override
    public void invoke(final Object listener, MethodAccess handler, int methodIndex, final Object message1, final Object message2) throws Throwable {
        handler.invoke(listener, methodIndex, message1, message2);
    }

    @Override
    public void invoke(final Object listener, MethodAccess handler, int methodIndex, final Object message1, final Object message2, final Object message3) throws Throwable {
        handler.invoke(listener, methodIndex, message1, message2, message3);
    }

    @Override
    public void invoke(final Object listener, MethodAccess handler, int methodIndex, final Object... messages) throws Throwable {
        handler.invoke(listener, methodIndex, messages);
    }
}
