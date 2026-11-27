/*
 * Copyright and licensing information for this source code can be found in
 * the NOTICE and LICENSE files distributed with this project.
 */

package io.spring.start.site.stats;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.DisposableBean;

/**
 * Samples a few health figures of this JVM at a fixed rate and keeps a short history of
 * them for the live "Server" section of the statistics page. One sampler serves every
 * viewer. Only load figures are exposed (CPU, heap, threads, uptime), nothing that
 * identifies the host, because the statistics page is public.
 *
 * @author Sami Ekblad
 */
public final class JvmMetrics implements DisposableBean {

	/**
	 * How often a sample is taken.
	 */
	public static final Duration INTERVAL = Duration.ofSeconds(5);

	/**
	 * How much history is kept.
	 */
	public static final Duration WINDOW = Duration.ofMinutes(10);

	private static final int CAPACITY = (int) (WINDOW.toMillis() / INTERVAL.toMillis());

	private final Deque<Sample> history = new ArrayDeque<>(CAPACITY + 1);

	private final ScheduledExecutorService sampler;

	public JvmMetrics() {
		this.sampler = Executors.newSingleThreadScheduledExecutor((task) -> {
			Thread thread = new Thread(task, "jvm-metrics");
			thread.setDaemon(true);
			return thread;
		});
		sample();
		this.sampler.scheduleAtFixedRate(this::sample, INTERVAL.toMillis(), INTERVAL.toMillis(), TimeUnit.MILLISECONDS);
	}

	/**
	 * Takes a sample now and adds it to the history. Called by the scheduler.
	 */
	void sample() {
		MemoryUsage heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		long heapMax = (heap.getMax() > 0) ? heap.getMax() : heap.getCommitted();
		Sample sample = new Sample(Instant.now(), cpuLoad(), heap.getUsed(), heapMax,
				ManagementFactory.getThreadMXBean().getThreadCount());
		synchronized (this.history) {
			this.history.addLast(sample);
			while (this.history.size() > CAPACITY) {
				this.history.removeFirst();
			}
		}
	}

	/**
	 * The samples of the last {@link #WINDOW}, oldest first.
	 * @return the history, never empty
	 */
	public List<Sample> history() {
		synchronized (this.history) {
			return List.copyOf(this.history);
		}
	}

	/**
	 * The most recent sample.
	 * @return the latest sample
	 */
	public Sample latest() {
		synchronized (this.history) {
			return this.history.getLast();
		}
	}

	/**
	 * How long this JVM has been running.
	 * @return the uptime
	 */
	public Duration uptime() {
		return Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime());
	}

	private static double cpuLoad() {
		// The com.sun variant has the system-wide load; the first reading can be
		// negative or NaN while the JVM has no baseline yet, which reads as 0 here.
		if (ManagementFactory.getOperatingSystemMXBean() instanceof com.sun.management.OperatingSystemMXBean os) {
			double load = os.getCpuLoad();
			return (load >= 0 && load <= 1) ? load : 0;
		}
		return 0;
	}

	@Override
	public void destroy() {
		this.sampler.shutdownNow();
	}

	/**
	 * One reading.
	 *
	 * @param time when it was taken
	 * @param cpuLoad system CPU load between 0 and 1
	 * @param heapUsed heap in use, in bytes
	 * @param heapMax the most heap the JVM may use, in bytes
	 * @param threads live threads
	 */
	public record Sample(Instant time, double cpuLoad, long heapUsed, long heapMax, int threads) {

		/**
		 * Heap in use as a share of the maximum.
		 * @return a value between 0 and 1
		 */
		public double heapShare() {
			return (this.heapMax > 0) ? Math.min(1.0, (double) this.heapUsed / this.heapMax) : 0;
		}

	}

}
