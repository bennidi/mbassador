package net.engio.mbassy.bus;

import net.engio.mbassy.PublicationError;
import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Invoke;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @author Ståle Undheim <su@ums.no>
 */
public class DisruptorPerformance {

    private static final int JOB_COUNT = 2 << 16;

    public static class Event {
        private final int id;

        public Event(final int id) {
            this.id = id;
        }
    }

    public static class EventHandler {

        private CountDownLatch countDownLatch = new CountDownLatch(0);

        @Handler(delivery = Invoke.Asynchronously)
        public void handle(Event event) throws InterruptedException {
            Thread.yield();
            countDownLatch.countDown();
        }
    }

    private static final ExecutorService EXECUTOR_SERVICE = Executors.newFixedThreadPool(64);
    private static final DisruptorMBassador<Event> EVENT_DISRUPTOR_M_BASSADOR = new DisruptorMBassador<Event>(BusConfiguration.Default().setExecutor(EXECUTOR_SERVICE));
    private static final MBassador<Event> EVENT_M_BASSADOR = new MBassador<Event>(BusConfiguration.Default().setExecutor(EXECUTOR_SERVICE));

    public static void main(String[] args) throws ExecutionException, InterruptedException {
        EventHandler listener = new EventHandler();
        EVENT_DISRUPTOR_M_BASSADOR.subscribe(listener);
        EVENT_M_BASSADOR.subscribe(listener);

        long start;

        Collection<? extends Callable<Object>> disruptorJobs = createJobs(EVENT_DISRUPTOR_M_BASSADOR, JOB_COUNT);
        Collection<? extends Callable<Object>> regularJobs = createJobs(EVENT_M_BASSADOR, JOB_COUNT);

        long disruptorSum = 0;
        long regularSum = 0;
        for (int j=0; j<10; j++) {
            listener.countDownLatch = new CountDownLatch(JOB_COUNT);
            start = System.currentTimeMillis();
            for (Future<Object> future : EXECUTOR_SERVICE.invokeAll(disruptorJobs)) {
                future.get();
            }
            listener.countDownLatch.await();
            if (j > 0) {
                disruptorSum += System.currentTimeMillis() - start;
            }
            System.out.printf("Disruptor: %d (total: %d)%n", System.currentTimeMillis() - start, disruptorSum);

            listener.countDownLatch = new CountDownLatch(JOB_COUNT);
            start = System.currentTimeMillis();
            for (Future<Object> future : EXECUTOR_SERVICE.invokeAll(regularJobs)) {
                future.get();
            }
            listener.countDownLatch.await();
            if (j > 0) {
                regularSum += System.currentTimeMillis() - start;
            }
            System.out.printf("Regular: %d (total: %d)%n", System.currentTimeMillis() - start, regularSum);
        }

        EVENT_M_BASSADOR.shutdown();
        EVENT_DISRUPTOR_M_BASSADOR.shutdown();
        EXECUTOR_SERVICE.shutdown();
    }

    private static Collection<? extends Callable<Object>> createJobs(final IMessageBus<Event, ? extends IMessageBus.IPostCommand> mBassador, final int size) {
        final List<Callable<Object>> tasks = new ArrayList<Callable<Object>>(size);
        for (int i=0; i<size; i++) {
            final int index = i;
            tasks.add(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    mBassador.post(new Event(index)).asynchronously();
                    return null;
                }
            });
        }
        return tasks;
    }
}
