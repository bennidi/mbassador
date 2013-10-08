package net.engio.mbassy.bus.config;

import net.engio.mbassy.bus.MessagePublication;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadFactory;

/**
 * Created with IntelliJ IDEA.
 * User: benjamin
 * Date: 8/16/13
 * Time: 9:56 AM
 * To change this template use File | Settings | File Templates.
 */
public interface IBusConfiguration extends ISyncBusConfiguration {

    int getNumberOfMessageDispatchers();

    ExecutorService getExecutorForAsynchronousHandlers();

    BlockingQueue<MessagePublication> getPendingMessagesQueue();

    ThreadFactory getThreadFactoryForAsynchronousMessageDispatch();

}
