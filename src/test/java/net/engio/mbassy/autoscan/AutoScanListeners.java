package net.engio.mbassy.autoscan;

import net.engio.mbassy.listener.Handler;
import net.engio.mbassy.listener.Listener;
import net.engio.mbassy.listener.References;

import java.util.concurrent.atomic.AtomicInteger;

public final class AutoScanListeners {

  private AutoScanListeners() {}

  public static class TestEvent {}

  @Listener(references = References.Strong)
  public static class TraditionalListener {
    public static final AtomicInteger count = new AtomicInteger(0);

    @Handler
    public void on(TestEvent event) {
      count.incrementAndGet();
    }
  }

  @Listener(references = References.Strong)
  public static class AutoScannedListener {
    public static final AtomicInteger count = new AtomicInteger(0);

    @Handler
    public void on(TestEvent event) {
      count.incrementAndGet();
    }
  }

  @Listener(references = References.Strong)
  public static class MixedAutoScannedListener {
    public static final AtomicInteger count = new AtomicInteger(0);

    @Handler
    public void on(TestEvent event) {
      count.incrementAndGet();
    }
  }

  @Listener(references = References.Strong)
  public static class MixedManualListener {
    public static final AtomicInteger count = new AtomicInteger(0);

    @Handler
    public void on(TestEvent event) {
      count.incrementAndGet();
    }
  }
}