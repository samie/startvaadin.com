/*
 * Copyright 2012 - present the original author or authors.
 */

package io.spring.start.site.stats;

import java.util.Map;

/**
 * Serializable form of the counters, persisted to a JSON file so tallies survive
 * restarts. Timestamps are epoch milliseconds to avoid a JavaTime (de)serialization
 * dependency; the map values are plain longs (the runtime {@code LongAdder} is an
 * internal detail).
 *
 * @param total total number of projects generated
 * @param countingSinceEpochMs epoch millis from which counting started
 * @param lastGeneratedEpochMs epoch millis of the most recent generation (0 if none)
 * @param dependencies per-dependency counts, keyed by dependency id
 * @param javaVersions per-Java-version counts
 * @param bootVersions per-Spring-Boot-version counts
 * @param languages per-language counts
 * @param buildSystems per-build-system counts
 * @param packaging per-packaging counts
 * @param uiActions per-UI-action counts (generate / explore / cli)
 * @param referrers per-referrer-host arrival counts
 * @author Sami Ekblad
 */
public record StatisticsSnapshot(long total, long countingSinceEpochMs, long lastGeneratedEpochMs,
		Map<String, Long> dependencies, Map<String, Long> javaVersions, Map<String, Long> bootVersions,
		Map<String, Long> languages, Map<String, Long> buildSystems, Map<String, Long> packaging,
		Map<String, Long> uiActions, Map<String, Long> referrers) {

	public StatisticsSnapshot {
		dependencies = (dependencies != null) ? dependencies : Map.of();
		javaVersions = (javaVersions != null) ? javaVersions : Map.of();
		bootVersions = (bootVersions != null) ? bootVersions : Map.of();
		languages = (languages != null) ? languages : Map.of();
		buildSystems = (buildSystems != null) ? buildSystems : Map.of();
		packaging = (packaging != null) ? packaging : Map.of();
		uiActions = (uiActions != null) ? uiActions : Map.of();
		referrers = (referrers != null) ? referrers : Map.of();
	}

}
