package net.engio.mbassy.common;

import java.lang.management.RuntimeMXBean;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author dorkbox, llc
 *         Date: 2/2/15
 */
public class DisruptorThreadFactory implements ThreadFactory {
    /**
     * The stack size is arbitrary based on JVM implementation. Default is 0
     * 8k is the size of the android stack. Depending on the version of android, this can either change, or will always be 8k
     *<p>
     * To be honest, 8k is pretty reasonable for an asynchronous/event based system (32bit) or 16k (64bit)
     * Setting the size MAY or MAY NOT have any effect!!!
     * <p>
     * Stack size must be specified in bytes. Default is 8k
     */
    private final long stackSizeForThreads;

    private final AtomicInteger threadID = new AtomicInteger(0);

    public DisruptorThreadFactory() {
        String stackSize = null;

        {
            RuntimeMXBean runtimeMX = java.lang.management.ManagementFactory.getRuntimeMXBean();
            List<String> inputArguments = runtimeMX.getInputArguments();

            Locale english = Locale.ENGLISH;
            for (String xss : inputArguments) {
                String xssLower = xss.toLowerCase(english);
                if (xssLower.startsWith("-xss")) {
                    stackSize = xssLower;
                    break;
                }
            }
        }

        if (stackSize != null) {
            int value = 0;
            if (stackSize.endsWith("k")) {
                stackSize = stackSize.substring(4, stackSize.length()-1);
                value = Integer.parseInt(stackSize) * 1024;
            } else if (stackSize.endsWith("m")) {
                stackSize = stackSize.substring(4, stackSize.length()-1);
                value = Integer.parseInt(stackSize) * 1024 * 1024;
            } else {
                try {
                    value = Integer.parseInt(stackSize.substring(4));
                } catch (Exception e) {
                }
            }

            this.stackSizeForThreads = value;
        } else {
            this.stackSizeForThreads = 8192;
        }
    }

    @Override
    public Thread newThread(Runnable r) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("MessageBus-");
        stringBuilder.append(this.threadID.getAndIncrement());

        // stack size is arbitrary based on JVM implementation. Default is 0
        // 8k is the size of the android stack. Depending on the version of android, this can either change, or will always be 8k
        // To be honest, 8k is pretty reasonable for an asynchronous/event based system (32bit) or 16k (64bit)
        // Setting the size MAY or MAY NOT have any effect!!!
        Thread t = new Thread(Thread.currentThread().getThreadGroup(), r, stringBuilder.toString(), this.stackSizeForThreads);
        t.setDaemon(false);// FORCE these threads to finish before allowing the JVM to exit
        t.setPriority(Thread.NORM_PRIORITY);
        return t;
    }
}

