package net.engio.mbassy.bus.publication;

import net.engio.mbassy.bus.IMessagePublication;

import java.util.concurrent.TimeUnit;

/**
 *
 *
*/
public interface ISyncAsyncPublicationCommand extends IPublicationCommand {

    /**
     * Execute the message publication asynchronously. The behaviour of this method depends on the
     * configured queuing strategy:
     * <p/>
     * If an unbound queuing strategy is used the call returns immediately.
     * If a bounded queue is used the call might block until the message can be placed in the queue.
     *
     * @return A message publication that can be used to access information about the state of
     */
    IMessagePublication asynchronously();

    /**
     * Execute the message publication asynchronously. The behaviour of this method depends on the
     * configured queuing strategy:
     * <p/>
     * If an unbound queuing strategy is used the call returns immediately.
     * If a bounded queue is used the call will block until the message can be placed in the queue
     * or the timeout is reached.
     *
     * @return A message publication that wraps up the publication request
     */
    IMessagePublication asynchronously(long timeout, TimeUnit unit);
}
