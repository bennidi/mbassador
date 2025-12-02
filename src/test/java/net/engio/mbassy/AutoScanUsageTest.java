package net.engio.mbassy;

import net.engio.mbassy.bus.MBassador;
import net.engio.mbassy.common.MessageBusTest;
import net.engio.mbassy.listener.Handler;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicInteger;

import static net.engio.mbassy.autoscan.AutoScanListeners.*;

/**
 * Tests for the different usage styles of MBassador with and without auto-scanning.
 */
public class AutoScanUsageTest extends MessageBusTest {

  // Manual-only listener (not in autoscan package, so not auto-scanned)
  public static class ManualOnlyListener {
    public static final AtomicInteger count = new AtomicInteger(0);

    @Handler
    public void on(TestEvent event) {
      count.incrementAndGet();
    }
  }

  @Test
  public void testTraditionalManualSubscription() {
    TraditionalListener.count.set(0);
    MBassador<TestEvent> bus = new MBassador<>();

    bus.subscribe(new TraditionalListener());
    bus.post(new TestEvent()).now();

    assertEquals(1, TraditionalListener.count.get());
  }

  @Test
  public void testModernAutoScanningConstructorUsage() {
    AutoScannedListener.count.set(0);

    String pkg = "net.engio.mbassy.autoscan";
    MBassador<TestEvent> bus = new MBassador<>();
    bus.autoScan(pkg);

    bus.post(new TestEvent()).now();

    assertEquals(1, AutoScannedListener.count.get());
  }

  @Test
  public void testModernAutoScanningManualCall() {
    AutoScannedListener.count.set(0);

    String pkg = "net.engio.mbassy.autoscan";
    MBassador<TestEvent> bus = new MBassador<>();

    bus.autoScan(pkg);
    bus.post(new TestEvent()).now();

    assertEquals(1, AutoScannedListener.count.get());
  }

  @Test
  public void testMixedAutoScanAndManualSubscription() {
    MixedAutoScannedListener.count.set(0);
    ManualOnlyListener.count.set(0);

    String pkg = "net.engio.mbassy.autoscan";
    MBassador<TestEvent> bus = new MBassador<>();

    // Auto-discover MixedAutoScannedListener (from autoscan package)
    bus.autoScan(pkg);

    // Manually add a specific listener that is not auto-scanned
    bus.subscribe(new ManualOnlyListener());

    bus.post(new TestEvent()).now();

    // Auto-scanned listener should have been invoked once
    assertEquals(
        "Auto-scanned listener must receive the event",
        1,
        MixedAutoScannedListener.count.get()
    );

    // Manually subscribed listener should also have been invoked exactly once
    assertEquals(
        "Manually subscribed listener must also receive the event",
        1,
        ManualOnlyListener.count.get()
    );
  }
}