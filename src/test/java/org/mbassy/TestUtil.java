package org.mbassy;

import java.util.List;

/**
 * Todo: Add javadoc
 *
 * @author bennidi
 *         Date: 11/22/12
 */
public class TestUtil {


    public static void setup(final IMessageBus bus, final List<Object> listeners, int numberOfThreads) {
        Runnable[] setupUnits = new Runnable[numberOfThreads];
        int partitionSize;
        if(listeners.size() >= numberOfThreads){
          partitionSize =  (int)Math.floor(listeners.size() / numberOfThreads);
        }
        else{
            partitionSize = 1;
            numberOfThreads = listeners.size();
        }

        for(int i = 0; i < numberOfThreads; i++){
            final int partitionStart = i * partitionSize;
            final int partitionEnd = (i+1 < numberOfThreads)
                    ? partitionStart + partitionSize + 1
                    : listeners.size();
            setupUnits[i] = new Runnable() {

                private List<Object> listenerSubset = listeners.subList(partitionStart, partitionEnd);

                public void run() {
                   for(Object listener : listenerSubset){
                       bus.subscribe(listener);
                   }
                }
            };

        }

        ConcurrentExecutor.runConcurrent(setupUnits);

    }

}
