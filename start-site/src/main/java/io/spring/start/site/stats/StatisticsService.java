/*
 * Copyright 2012 - present the original author or authors.
 */

package io.spring.start.site.stats;

import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import io.spring.initializr.web.project.ProjectGeneratedEvent;
import io.spring.initializr.web.project.ProjectRequest;
import io.spring.initializr.web.project.WebProjectRequest;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.event.EventListener;
import org.springframework.util.StringUtils;

/**
 * In-process tally of generated projects, fed by {@link ProjectGeneratedEvent} (published
 * for <em>both</em> the Vaadin UI and the CLI {@code /starter.zip} path, since
 * {@code InitializrUiService} and the web controller share the same
 * {@code ApplicationContext} as event publisher). Counters live in memory and are
 * periodically merged into a JSON file so they survive restarts. Each save adds only this
 * instance's unsaved increments to what is on disk, so two instances overlapping during a
 * restart do not overwrite each other's counts. Numbers are meant to be interesting, not
 * authoritative.
 * <p>
 * Note: the UI's "Explore" (preview) action also generates a project and therefore also
 * emits an event, so counts include previews, not only downloads. That is intentional
 * here (the page need not be exhaustive) and noted so it is not mistaken for a bug.
 *
 * @author Sami Ekblad
 */
public final class StatisticsService implements DisposableBean {

	/**
	 * Request parameter the Vaadin UI sets on its in-process generation requests to
	 * record which UI action triggered it ("generate" or "explore"). Absent for CLI
	 * {@code /starter.zip} requests, which are tallied as {@value #CLI_ACTION}.
	 */
	public static final String UI_ACTION_PARAMETER = "x-start-ui-action";

	private static final String CLI_ACTION = "cli";

	private static final String DIRECT_REFERRER = "direct";

	private static final Duration DEFAULT_SAVE_INTERVAL = Duration.ofMinutes(1);

	private static final long MILLIS_PER_DAY = 86_400_000L;

	private static final Log logger = LogFactory.getLog(StatisticsService.class);

	private final JsonMapper jsonMapper;

	private final Path file;

	private final Object saveLock = new Object();

	private final AtomicBoolean dirty = new AtomicBoolean();

	private final ScheduledExecutorService saver;

	// What this instance last read from or wrote to the file (guarded by saveLock): the
	// live counters minus this are the increments not yet saved.
	private StatisticsSnapshot baseline = emptySnapshot();

	private boolean savedOnce;

	private final AtomicLong total = new AtomicLong();

	private final Map<String, LongAdder> dependencies = new ConcurrentHashMap<>();

	private final Map<String, LongAdder> javaVersions = new ConcurrentHashMap<>();

	private final Map<String, LongAdder> bootVersions = new ConcurrentHashMap<>();

	private final Map<String, LongAdder> languages = new ConcurrentHashMap<>();

	private final Map<String, LongAdder> buildSystems = new ConcurrentHashMap<>();

	private final Map<String, LongAdder> packaging = new ConcurrentHashMap<>();

	private final Map<String, LongAdder> uiActions = new ConcurrentHashMap<>();

	private final Map<String, LongAdder> referrers = new ConcurrentHashMap<>();

	private volatile long countingSinceEpochMs = System.currentTimeMillis();

	private volatile long lastGeneratedEpochMs;

	public StatisticsService(JsonMapper jsonMapper, String filePath) {
		this(jsonMapper, filePath, DEFAULT_SAVE_INTERVAL);
	}

	StatisticsService(JsonMapper jsonMapper, String filePath, Duration saveInterval) {
		this.jsonMapper = jsonMapper;
		this.file = StringUtils.hasText(filePath) ? Path.of(filePath).toAbsolutePath() : null;
		load();
		if (this.file != null) {
			this.saver = Executors.newSingleThreadScheduledExecutor((task) -> {
				Thread thread = new Thread(task, "statistics-saver");
				thread.setDaemon(true);
				return thread;
			});
			long millis = saveInterval.toMillis();
			this.saver.scheduleWithFixedDelay(this::saveIfDirty, millis, millis, TimeUnit.MILLISECONDS);
		}
		else {
			logger.info("No statistics file configured; statistics are kept in memory only");
			this.saver = null;
		}
	}

	@EventListener
	public void onProjectGenerated(ProjectGeneratedEvent event) {
		ProjectRequest request = event.getProjectRequest();
		this.total.incrementAndGet();
		this.lastGeneratedEpochMs = System.currentTimeMillis();
		increment(this.javaVersions, request.getJavaVersion());
		increment(this.bootVersions, request.getBootVersion());
		increment(this.languages, request.getLanguage());
		increment(this.buildSystems, buildSystem(request.getType()));
		increment(this.packaging, request.getPackaging());
		increment(this.uiActions, uiAction(request));
		List<String> deps = request.getDependencies();
		if (deps != null) {
			for (String dep : deps) {
				increment(this.dependencies, dep);
			}
		}
		this.dirty.set(true);
	}

	/**
	 * Record a page arrival by referrer. Called by the arrival listener on each full page
	 * load (genuine arrivals, not in-app navigations).
	 * @param referer the browser-supplied {@code Referer} header (may be null/blank)
	 * @param selfHost this deployment's own host, so self/reload referrals collapse to
	 * {@value #DIRECT_REFERRER} rather than inflating the referrer list (may be null)
	 */
	public void recordArrival(String referer, String selfHost) {
		increment(this.referrers, referrerHost(referer, selfHost));
		this.dirty.set(true);
	}

	public long total() {
		return this.total.get();
	}

	public Instant countingSince() {
		return Instant.ofEpochMilli(this.countingSinceEpochMs);
	}

	public Instant lastGenerated() {
		return (this.lastGeneratedEpochMs > 0) ? Instant.ofEpochMilli(this.lastGeneratedEpochMs) : null;
	}

	public double perDayAverage() {
		double days = Math.max(1.0, (System.currentTimeMillis() - this.countingSinceEpochMs) / (double) MILLIS_PER_DAY);
		return this.total.get() / days;
	}

	public List<Count> topDependencies(int limit) {
		return toCounts(this.dependencies, limit);
	}

	public List<Count> javaVersionDistribution() {
		return toCounts(this.javaVersions, 0);
	}

	public List<Count> languageDistribution() {
		return toCounts(this.languages, 0);
	}

	public List<Count> buildSystemDistribution() {
		return toCounts(this.buildSystems, 0);
	}

	public List<Count> bootVersionDistribution() {
		return toCounts(this.bootVersions, 0);
	}

	public List<Count> packagingDistribution() {
		return toCounts(this.packaging, 0);
	}

	public List<Count> uiActionDistribution() {
		return toCounts(this.uiActions, 0);
	}

	public List<Count> referrerDistribution() {
		return toCounts(this.referrers, 0);
	}

	private List<Count> toCounts(Map<String, LongAdder> map, int limit) {
		long grand = this.total.get();
		return map.entrySet()
			.stream()
			.map((entry) -> toCount(entry.getKey(), entry.getValue().sum(), grand))
			.sorted(Comparator.comparingLong(Count::count).reversed().thenComparing(Count::id))
			.limit((limit <= 0) ? Long.MAX_VALUE : limit)
			.toList();
	}

	private static Count toCount(String id, long count, long grand) {
		return new Count(id, count, (grand == 0) ? 0.0 : (double) count / grand);
	}

	private static void increment(Map<String, LongAdder> map, String key) {
		if (!StringUtils.hasText(key)) {
			return;
		}
		map.computeIfAbsent(key, (k) -> new LongAdder()).increment();
	}

	private static String buildSystem(String type) {
		if (type == null) {
			return "unknown";
		}
		if (type.contains("gradle")) {
			return "gradle";
		}
		if (type.contains("maven")) {
			return "maven";
		}
		return "unknown";
	}

	private static String uiAction(ProjectRequest request) {
		if (request instanceof WebProjectRequest web) {
			Object action = web.getParameters().get(UI_ACTION_PARAMETER);
			if (action instanceof String text && StringUtils.hasText(text)) {
				return text;
			}
		}
		return CLI_ACTION;
	}

	static String referrerHost(String referer, String selfHost) {
		if (!StringUtils.hasText(referer)) {
			return DIRECT_REFERRER;
		}
		try {
			String host = URI.create(referer.trim()).getHost();
			if (!StringUtils.hasText(host)) {
				return DIRECT_REFERRER;
			}
			host = host.toLowerCase(Locale.ROOT);
			if (StringUtils.hasText(selfHost) && host.equalsIgnoreCase(selfHost)) {
				return DIRECT_REFERRER;
			}
			return host;
		}
		catch (IllegalArgumentException ex) {
			// A malformed Referer must never break a request; treat it as direct.
			return DIRECT_REFERRER;
		}
	}

	private void load() {
		if (this.file == null) {
			return;
		}
		try {
			StatisticsSnapshot snapshot = read();
			if (snapshot == null) {
				logger.info("No statistics file at " + this.file + " yet, starting fresh");
				return;
			}
			this.total.set(snapshot.total());
			if (snapshot.countingSinceEpochMs() > 0) {
				this.countingSinceEpochMs = snapshot.countingSinceEpochMs();
			}
			this.lastGeneratedEpochMs = snapshot.lastGeneratedEpochMs();
			add(this.dependencies, snapshot.dependencies(), Map.of());
			add(this.javaVersions, snapshot.javaVersions(), Map.of());
			add(this.bootVersions, snapshot.bootVersions(), Map.of());
			add(this.languages, snapshot.languages(), Map.of());
			add(this.buildSystems, snapshot.buildSystems(), Map.of());
			add(this.packaging, snapshot.packaging(), Map.of());
			add(this.uiActions, snapshot.uiActions(), Map.of());
			add(this.referrers, snapshot.referrers(), Map.of());
			this.baseline = snapshot;
			logger.info("Loaded statistics (" + snapshot.total() + " projects) from " + this.file);
		}
		catch (IOException | RuntimeException ex) {
			// A missing/corrupt file must never stop the app; start fresh instead.
			logger.warn("Could not read statistics from " + this.file + ", starting fresh", ex);
		}
	}

	private StatisticsSnapshot read() throws IOException {
		if (!Files.exists(this.file)) {
			return null;
		}
		return this.jsonMapper.readValue(Files.readString(this.file), StatisticsSnapshot.class);
	}

	private void saveIfDirty() {
		if (this.dirty.getAndSet(false)) {
			save();
		}
	}

	private void save() {
		if (this.file == null) {
			return;
		}
		synchronized (this.saveLock) {
			try {
				StatisticsSnapshot current = snapshot();
				StatisticsSnapshot onDisk = readForMerge();
				StatisticsSnapshot merged = merge(onDisk, current, this.baseline);
				String json = this.jsonMapper.writeValueAsString(merged);
				Path parent = this.file.getParent();
				if (parent != null) {
					Files.createDirectories(parent);
				}
				// Write to a sibling temp file then swap, so a crash mid-write cannot
				// leave a half-written stats file behind.
				Path tmp = this.file.resolveSibling(this.file.getFileName() + ".tmp");
				Files.writeString(tmp, json);
				Files.move(tmp, this.file, StandardCopyOption.REPLACE_EXISTING);
				absorb(onDisk, this.baseline, merged);
				this.baseline = merged;
				if (!this.savedOnce) {
					this.savedOnce = true;
					logger.info("Saving statistics to " + this.file);
				}
			}
			catch (IOException | RuntimeException ex) {
				this.dirty.set(true);
				logger.warn("Could not persist statistics to " + this.file, ex);
			}
		}
	}

	private StatisticsSnapshot readForMerge() {
		try {
			StatisticsSnapshot onDisk = read();
			return (onDisk != null) ? onDisk : this.baseline;
		}
		catch (IOException | RuntimeException ex) {
			logger.warn("Could not read statistics from " + this.file + ", overwriting it", ex);
			return this.baseline;
		}
	}

	// Counts other instances saved since our last sync (onDisk - baseline) are added to
	// the live counters so this instance shows, and later saves, the combined numbers.
	private void absorb(StatisticsSnapshot onDisk, StatisticsSnapshot base, StatisticsSnapshot merged) {
		this.total.addAndGet(onDisk.total() - base.total());
		this.countingSinceEpochMs = merged.countingSinceEpochMs();
		this.lastGeneratedEpochMs = Math.max(this.lastGeneratedEpochMs, merged.lastGeneratedEpochMs());
		add(this.dependencies, onDisk.dependencies(), base.dependencies());
		add(this.javaVersions, onDisk.javaVersions(), base.javaVersions());
		add(this.bootVersions, onDisk.bootVersions(), base.bootVersions());
		add(this.languages, onDisk.languages(), base.languages());
		add(this.buildSystems, onDisk.buildSystems(), base.buildSystems());
		add(this.packaging, onDisk.packaging(), base.packaging());
		add(this.uiActions, onDisk.uiActions(), base.uiActions());
		add(this.referrers, onDisk.referrers(), base.referrers());
	}

	private static void add(Map<String, LongAdder> map, Map<String, Long> plus, Map<String, Long> minus) {
		for (String key : keys(plus, minus)) {
			long delta = plus.getOrDefault(key, 0L) - minus.getOrDefault(key, 0L);
			if (delta != 0) {
				map.computeIfAbsent(key, (k) -> new LongAdder()).add(delta);
			}
		}
	}

	static StatisticsSnapshot merge(StatisticsSnapshot onDisk, StatisticsSnapshot current, StatisticsSnapshot base) {
		return new StatisticsSnapshot(onDisk.total() + current.total() - base.total(),
				earliest(onDisk.countingSinceEpochMs(), current.countingSinceEpochMs()),
				Math.max(onDisk.lastGeneratedEpochMs(), current.lastGeneratedEpochMs()),
				merge(onDisk.dependencies(), current.dependencies(), base.dependencies()),
				merge(onDisk.javaVersions(), current.javaVersions(), base.javaVersions()),
				merge(onDisk.bootVersions(), current.bootVersions(), base.bootVersions()),
				merge(onDisk.languages(), current.languages(), base.languages()),
				merge(onDisk.buildSystems(), current.buildSystems(), base.buildSystems()),
				merge(onDisk.packaging(), current.packaging(), base.packaging()),
				merge(onDisk.uiActions(), current.uiActions(), base.uiActions()),
				merge(onDisk.referrers(), current.referrers(), base.referrers()));
	}

	private static Map<String, Long> merge(Map<String, Long> onDisk, Map<String, Long> current,
			Map<String, Long> base) {
		Set<String> keys = keys(onDisk, current);
		keys.addAll(base.keySet());
		Map<String, Long> out = new LinkedHashMap<>();
		for (String key : keys) {
			long value = onDisk.getOrDefault(key, 0L) + current.getOrDefault(key, 0L) - base.getOrDefault(key, 0L);
			if (value > 0) {
				out.put(key, value);
			}
		}
		return out;
	}

	private static Set<String> keys(Map<String, Long> first, Map<String, Long> second) {
		Set<String> keys = new HashSet<>(first.keySet());
		keys.addAll(second.keySet());
		return keys;
	}

	private static long earliest(long first, long second) {
		if (first <= 0) {
			return second;
		}
		return (second <= 0) ? first : Math.min(first, second);
	}

	private static StatisticsSnapshot emptySnapshot() {
		return new StatisticsSnapshot(0, 0, 0, null, null, null, null, null, null, null, null);
	}

	private StatisticsSnapshot snapshot() {
		return new StatisticsSnapshot(this.total.get(), this.countingSinceEpochMs, this.lastGeneratedEpochMs,
				toMap(this.dependencies), toMap(this.javaVersions), toMap(this.bootVersions), toMap(this.languages),
				toMap(this.buildSystems), toMap(this.packaging), toMap(this.uiActions), toMap(this.referrers));
	}

	private static Map<String, Long> toMap(Map<String, LongAdder> map) {
		Map<String, Long> out = new LinkedHashMap<>();
		map.forEach((key, value) -> out.put(key, value.sum()));
		return out;
	}

	@Override
	public void destroy() {
		if (this.saver != null) {
			this.saver.shutdownNow();
		}
		save();
	}

}
