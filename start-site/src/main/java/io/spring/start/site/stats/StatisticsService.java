/*
 * Copyright 2012 - present the original author or authors.
 */

package io.spring.start.site.stats;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.LongAdder;

import io.spring.initializr.web.project.ProjectGeneratedEvent;
import io.spring.initializr.web.project.ProjectRequest;
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
 * snapshotted to a JSON file so they survive restarts. Numbers are scoped to this
 * deployment; they are meant to be interesting, not authoritative.
 * <p>
 * Note: the UI's "Explore" (preview) action also generates a project and therefore also
 * emits an event, so counts include previews, not only downloads. That is intentional
 * here (the page need not be exhaustive) and noted so it is not mistaken for a bug.
 *
 * @author Sami Ekblad
 */
public final class StatisticsService implements DisposableBean {

	// Persist after this many new generations (a debounce so hot paths do not hit disk on
	// every request); a final flush also runs on shutdown via destroy().
	private static final int SAVE_EVERY = 20;

	private static final long MILLIS_PER_DAY = 86_400_000L;

	private static final Log logger = LogFactory.getLog(StatisticsService.class);

	private final JsonMapper jsonMapper;

	private final Path file;

	private final Object saveLock = new Object();

	private final AtomicLong total = new AtomicLong();

	private final AtomicLong sinceLastSave = new AtomicLong();

	private final Map<String, LongAdder> dependencies = new ConcurrentHashMap<>();

	private final Map<String, LongAdder> javaVersions = new ConcurrentHashMap<>();

	private final Map<String, LongAdder> bootVersions = new ConcurrentHashMap<>();

	private final Map<String, LongAdder> languages = new ConcurrentHashMap<>();

	private final Map<String, LongAdder> buildSystems = new ConcurrentHashMap<>();

	private volatile long countingSinceEpochMs = System.currentTimeMillis();

	private volatile long lastGeneratedEpochMs;

	public StatisticsService(JsonMapper jsonMapper, String filePath) {
		this.jsonMapper = jsonMapper;
		this.file = StringUtils.hasText(filePath) ? Path.of(filePath) : null;
		load();
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
		List<String> deps = request.getDependencies();
		if (deps != null) {
			for (String dep : deps) {
				increment(this.dependencies, dep);
			}
		}
		maybeSave();
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

	private void maybeSave() {
		if (this.file == null) {
			return;
		}
		if (this.sinceLastSave.incrementAndGet() >= SAVE_EVERY) {
			this.sinceLastSave.set(0);
			save();
		}
	}

	private void load() {
		if (this.file == null || !Files.exists(this.file)) {
			return;
		}
		try {
			StatisticsSnapshot snapshot = this.jsonMapper.readValue(Files.readString(this.file),
					StatisticsSnapshot.class);
			this.total.set(snapshot.total());
			if (snapshot.countingSinceEpochMs() > 0) {
				this.countingSinceEpochMs = snapshot.countingSinceEpochMs();
			}
			this.lastGeneratedEpochMs = snapshot.lastGeneratedEpochMs();
			restore(this.dependencies, snapshot.dependencies());
			restore(this.javaVersions, snapshot.javaVersions());
			restore(this.bootVersions, snapshot.bootVersions());
			restore(this.languages, snapshot.languages());
			restore(this.buildSystems, snapshot.buildSystems());
		}
		catch (IOException | RuntimeException ex) {
			// A missing/corrupt file must never stop the app; start fresh instead.
			logger.warn("Could not read statistics from " + this.file + ", starting fresh", ex);
		}
	}

	private static void restore(Map<String, LongAdder> map, Map<String, Long> values) {
		values.forEach((key, value) -> {
			LongAdder adder = new LongAdder();
			adder.add(value);
			map.put(key, adder);
		});
	}

	private void save() {
		if (this.file == null) {
			return;
		}
		synchronized (this.saveLock) {
			try {
				String json = this.jsonMapper.writeValueAsString(snapshot());
				Path parent = this.file.getParent();
				if (parent != null) {
					Files.createDirectories(parent);
				}
				// Write to a sibling temp file then swap, so a crash mid-write cannot
				// leave a half-written stats file behind.
				Path tmp = this.file.resolveSibling(this.file.getFileName() + ".tmp");
				Files.writeString(tmp, json);
				Files.move(tmp, this.file, StandardCopyOption.REPLACE_EXISTING);
			}
			catch (IOException | RuntimeException ex) {
				logger.warn("Could not persist statistics to " + this.file, ex);
			}
		}
	}

	private StatisticsSnapshot snapshot() {
		return new StatisticsSnapshot(this.total.get(), this.countingSinceEpochMs, this.lastGeneratedEpochMs,
				toMap(this.dependencies), toMap(this.javaVersions), toMap(this.bootVersions), toMap(this.languages),
				toMap(this.buildSystems));
	}

	private static Map<String, Long> toMap(Map<String, LongAdder> map) {
		Map<String, Long> out = new LinkedHashMap<>();
		map.forEach((key, value) -> out.put(key, value.sum()));
		return out;
	}

	@Override
	public void destroy() {
		save();
	}

}
