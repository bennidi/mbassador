package net.engio.mbassy.bus;

import net.engio.mbassy.bus.publication.SyncAsyncPostCommand;

import java.util.concurrent.TimeUnit;

/**
 * Created with IntelliJ IDEA.
 * User: benjamin
 * Date: 8/21/13
 * Time: 11:05 AM
 * To change this template use File | Settings | File Templates.
 */
public interface IMBassador<T> extends IMessageBus<T, SyncAsyncPostCommand<T>> {

    MessagePublication publishAsync(T message);

    MessagePublication publishAsync(T message, long timeout, TimeUnit unit);
}
