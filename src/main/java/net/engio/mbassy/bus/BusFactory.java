package net.engio.mbassy.bus;

import net.engio.mbassy.bus.common.IMessageBus;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.Feature;

/**
 * The bus factory provides convenient factory methods for the most common bus use cases.
 *
 * @author bennidi
 *         Date: 3/30/14
 */
public class BusFactory {

    /**
     * Create a message bus supporting only synchronous message publication.
     * All message publications will run in the calling thread, no bus internal
     * multi-threading will occur.
     */
    public static SyncMessageBus SynchronousOnly(){
        BusConfiguration syncPubSubCfg = new BusConfiguration();
        syncPubSubCfg.addFeature(Feature.SyncPubSub.Default());
        return new SyncMessageBus(syncPubSubCfg);
    }

    /**
     * Create a message bus with support for synchronous and asynchronous message publication.
     * Asynchronous message publication will be handled by a single thread such that FIFO
     * order of message processing is guaranteed.
     */
    public static IMessageBus AsynchronousSequentialFIFO(){
        BusConfiguration asyncFIFOConfig = new BusConfiguration();
        asyncFIFOConfig.addFeature(Feature.SyncPubSub.Default());
        asyncFIFOConfig.addFeature(Feature.AsynchronousHandlerInvocation.Default(1, 1));
        asyncFIFOConfig.addFeature(Feature.AsynchronousMessageDispatch.Default().setNumberOfMessageDispatchers(1));
        return new MBassador(asyncFIFOConfig);
    }
}
