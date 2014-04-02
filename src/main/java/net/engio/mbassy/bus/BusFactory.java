package net.engio.mbassy.bus;

import net.engio.mbassy.bus.common.IMessageBus;
import net.engio.mbassy.bus.common.ISyncMessageBus;
import net.engio.mbassy.bus.config.BusConfiguration;
import net.engio.mbassy.bus.config.SyncBusConfiguration;

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
     *
     * @return
     */
    public static ISyncMessageBus SynchronousOnly(){
        return new SyncMessageBus(new SyncBusConfiguration());
    }

    /**
     * Create a message bus supporting synchronous and asynchronous message publication.
     * Asynchronous message publication will be handled by a single thread such that FIFO
     * order of message processing is guaranteed.
     *
     *
     * @return
     */
    public static IMessageBus AsynchronousSequentialFIFO(){
        return new MBassador(BusConfiguration.Default(1,1,1));
    }
}
