package {{package}};

import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.nio.file.FileStore;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.stream.Stream;

import com.sun.management.OperatingSystemMXBean;
import com.vaadin.flow.signals.Signal;
import com.vaadin.flow.signals.shared.SharedValueSignal;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tools.jackson.core.type.TypeReference;

/**
 * Samples the JVM every two seconds, once for all users, and publishes the last two
 * minutes through a shared signal. Set {@code dashboard.sampling=-} to switch the
 * scheduled sampling off, as the tests do.
 */
@Service
@EnableScheduling
public class SystemMetrics {

	public record Sample(Instant time, double systemCpu, double processCpu, long heapUsed, long heapCommitted,
			long heapMax, long memoryUsed, long memoryTotal, long diskUsed, long diskTotal, int threads,
			Duration uptime, long gcCollections) {
	}

	static final int HISTORY_SIZE = 60;

	private final SharedValueSignal<List<Sample>> history;

	public SystemMetrics() throws IOException {
		// Starts with one sample, so that the dashboard never shows an empty history
		this.history = new SharedValueSignal<>(List.of(read()), new TypeReference<List<Sample>>() {
		});
	}

	public Signal<List<Sample>> history() {
		return this.history.asReadonly();
	}

	@Scheduled(cron = "${dashboard.sampling:*/2 * * * * *}")
	void sample() throws IOException {
		publish(read());
	}

	void publish(Sample sample) {
		this.history.update((samples) -> Stream.concat(samples.stream(), Stream.of(sample))
			.skip(Math.max(0, samples.size() + 1 - HISTORY_SIZE))
			.toList());
	}

	private static Sample read() throws IOException {
		OperatingSystemMXBean os = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
		MemoryUsage heap = ManagementFactory.getMemoryMXBean().getHeapMemoryUsage();
		FileStore disk = Files.getFileStore(Path.of(""));
		long gcCollections = ManagementFactory.getGarbageCollectorMXBeans()
			.stream()
			.mapToLong(GarbageCollectorMXBean::getCollectionCount)
			.filter((count) -> count > 0)
			.sum();
		return new Sample(Instant.now(), load(os.getCpuLoad()), load(os.getProcessCpuLoad()), heap.getUsed(),
				heap.getCommitted(), heap.getMax() > 0 ? heap.getMax() : heap.getCommitted(),
				os.getTotalMemorySize() - os.getFreeMemorySize(), os.getTotalMemorySize(),
				disk.getTotalSpace() - disk.getUsableSpace(), disk.getTotalSpace(),
				ManagementFactory.getThreadMXBean().getThreadCount(),
				Duration.ofMillis(ManagementFactory.getRuntimeMXBean().getUptime()), gcCollections);
	}

	// The first reading, or one the platform can't make, is negative or NaN
	private static double load(double value) {
		return value >= 0 ? value : 0;
	}

}
