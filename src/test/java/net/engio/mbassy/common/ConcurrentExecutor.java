package net.engio.mbassy.common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

/**
 * Run various tests concurrently. A given instance of runnable will be used to spawn and start
 * as many threads as specified by an additional parameter or (if multiple runnables have been
 * passed to the method) one thread for each runnable.
 * <p/>
 * Date: 2/14/12
 *
 * @Author bennidi
 */
public class ConcurrentExecutor {


	public static void runConcurrent(final Runnable unit, int numberOfConcurrentExecutions) {
		Runnable[] units = new Runnable[numberOfConcurrentExecutions];
		// create the tasks and schedule for execution
		for (int i = 0; i < numberOfConcurrentExecutions; i++) {
			units[i] = unit;
		}
		runConcurrent(units);

	}

	public static void runConcurrent(final Runnable... units) {
		ExecutorService executor = Executors.newCachedThreadPool();
		List<Future<Long>> returnValues = new ArrayList<Future<Long>>();

		// create the tasks and schedule for execution
		for (final Runnable unit : units) {
			Callable<Long> wrapper = new Callable<Long>() {
				@Override
				public Long call() throws Exception {
					long start = System.currentTimeMillis();
					unit.run();
					return System.currentTimeMillis() - start;
				}
			};
			returnValues.add(executor.submit(wrapper));
		}

		// wait until all tasks have been executed
		try {
			executor.shutdown();// tells the thread pool to execute all waiting tasks
			executor.awaitTermination(5, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			// unlikely that this will happen
			e.printStackTrace();
		}
	}


}
