package org.mbassy;

import org.junit.Assert;
import org.junit.Test;
import org.mbassy.filter.Filter;
import org.mbassy.filter.MessageFilter;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Test synchronous and asynchronous dispatch in single and multi-threaded scenario.
 *
 * @author bennidi
 * Date: 2/8/12
 */
public class MBassadorTest {



    @Test
    public void testSubscribe() throws InterruptedException {

        MBassador bus = new MBassador();
        int listenerCount = 1000;

        for (int i = 1; i <= listenerCount; i++) {
            EventingTestBean bean = new EventingTestBean();
            bus.subscribe(bean);
            bus.unsubscribe(new EventingTestBean());

        }
    }

    @Test
    public void testUnSubscribe() throws InterruptedException {

        MBassador bus = new MBassador();
        int listenerCount = 1000;

        for (int i = 1; i <= listenerCount; i++) {
            bus.unsubscribe(new EventingTestBean());

        }
    }


	@Test
	public void testAsynchronous() throws InterruptedException {

		MBassador bus = new MBassador();
		int listenerCount = 1000;
		List<EventingTestBean> persistentReferences = new ArrayList();

        for (int i = 1; i <= listenerCount; i++) {
			EventingTestBean bean = new EventingTestBean();
			persistentReferences.add(bean);
			bus.subscribe(bean);
		}

		TestEvent event = new TestEvent();
		TestEvent subEvent = new SubTestEvent();

		bus.publishAsync(event);
		bus.publishAsync(subEvent);

		Thread.sleep(2000);

		Assert.assertTrue(event.counter.get() == 1000);
		Assert.assertTrue(subEvent.counter.get() == 1000 * 2);

	}

    @Test
	public void testSynchronous() throws InterruptedException {

		MBassador bus = new MBassador();
		int listenerCount = 100;
		List<EventingTestBean> persistentReferences = new ArrayList();
		for (int i = 1; i <= listenerCount; i++) {


			EventingTestBean bean = new EventingTestBean();
			persistentReferences.add(bean);
			bus.subscribe(bean);

			TestEvent event = new TestEvent();
			TestEvent subEvent = new SubTestEvent();

			bus.publish(event);
			bus.publish(subEvent);

            Assert.assertEquals(i, event.counter.get());

            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }

			Assert.assertEquals(i * 2, subEvent.counter.get());

		}

	}

    @Test
	public void testConcurrentPublication() {
        final MBassador bus = new MBassador();
        final int listenerCount = 100;
        final int concurenny = 20;
        final CopyOnWriteArrayList<TestEvent> testEvents = new CopyOnWriteArrayList<TestEvent>();
        final CopyOnWriteArrayList<SubTestEvent> subtestEvents = new CopyOnWriteArrayList<SubTestEvent>();
        final CopyOnWriteArrayList<EventingTestBean> persistentReferences = new CopyOnWriteArrayList<EventingTestBean>();

		ConcurrentExecutor.runConcurrent(new Runnable() {
			@Override
			public void run() {
				long start = System.currentTimeMillis();
				for (int i = 0; i < listenerCount; i++) {
					EventingTestBean bean = new EventingTestBean();
					persistentReferences.add(bean);
                    bus.subscribe(bean);
				}

				long end = System.currentTimeMillis();
				System.out.println("MBassador: Creating " + listenerCount + " listeners took " + (end - start) + " ms");
			}
		}, concurenny);

        ConcurrentExecutor.runConcurrent(new Runnable() {
            @Override
            public void run() {
                long start = System.currentTimeMillis();
                for (int i = 0; i < listenerCount; i++) {
                    TestEvent event = new TestEvent();
                    SubTestEvent subEvent = new SubTestEvent();
                    testEvents.add(event);
                    subtestEvents.add(subEvent);

                    bus.publishAsync(event);
                    bus.publish(subEvent);
                }

                long end = System.currentTimeMillis();
                System.out.println("MBassador: Publishing " + 2 * listenerCount + " events took " + (end - start) + " ms");
            }
        }, concurenny);

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

        for(TestEvent event : testEvents){
            Assert.assertEquals(listenerCount * concurenny, event.counter.get());
        }

        for(SubTestEvent event : subtestEvents){
            Assert.assertEquals(listenerCount * concurenny * 2, event.counter.get());
        }

	}


	public static class TestEvent {

		public AtomicInteger counter = new AtomicInteger();

	}

	public static class SubTestEvent extends TestEvent {

	}


	public class EventingTestBean {

        // every event of type TestEvent or any subtype will be delivered
        // to this listener
		@Listener
		public void handleTestEvent(TestEvent event) {
			event.counter.incrementAndGet();
		}

        // this handler will be invoked asynchronously
		@Listener(mode = Listener.Dispatch.Asynchronous)
		public void handleSubTestEvent(SubTestEvent event) {
            event.counter.incrementAndGet();
		}

        // this handler will receive events of type SubTestEvent
        // or any subtabe and that passes the given filter
        @Listener({@Filter(MessageFilter.None.class),@Filter(MessageFilter.All.class)})
        public void handleFiltered(SubTestEvent event) {
            event.counter.incrementAndGet();
        }


	}


}
