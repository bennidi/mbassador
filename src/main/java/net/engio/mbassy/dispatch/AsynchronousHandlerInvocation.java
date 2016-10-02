package net.engio.mbassy.dispatch;

import net.engio.mbassy.bus.MessagePublication;
import net.engio.mbassy.bus.config.IBusConfiguration;
import net.engio.mbassy.subscription.AbstractSubscriptionContextAware;

import java.util.concurrent.ExecutorService;

/**
 * This invocation will schedule the wrapped (decorated) invocation to be executed asynchronously
 *
 * @author bennidi
 *         Date: 11/23/12
 */
public class AsynchronousHandlerInvocation extends AbstractSubscriptionContextAware implements IHandlerInvocation {

    private final IHandlerInvocation delegate;

    private final ExecutorService executor;

    public AsynchronousHandlerInvocation(IHandlerInvocation delegate) {
        super(delegate.getContext());
        this.delegate = delegate;
        this.executor = delegate.getContext().getRuntime().get(IBusConfiguration.Properties.AsynchronousHandlerExecutor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void invoke(final Object listener, final Object message, final MessagePublication publication){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                    delegate.invoke(listener, message, publication);
            }
        });
    }
}
