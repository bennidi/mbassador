package net.engio.mbassy.bus.config;

import net.engio.mbassy.bus.MessagePublication;
import net.engio.mbassy.listener.MetadataReader;
import net.engio.mbassy.subscription.SubscriptionFactory;

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
public interface IBusConfiguration {

    int getNumberOfMessageDispatchers();

    ExecutorService getExecutorForAsynchronousHandlers();

    BlockingQueue<MessagePublication> getPendingMessagesQueue();

    MessagePublication.Factory getMessagePublicationFactory();

    MetadataReader getMetadataReader();

    SubscriptionFactory getSubscriptionFactory();

    ThreadFactory getThreadFactoryForAsynchronousMessageDispatch();

}
