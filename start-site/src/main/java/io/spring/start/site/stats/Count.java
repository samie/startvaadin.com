/*
 * Copyright 2012 - present the original author or authors.
 */

package io.spring.start.site.stats;

/**
 * A single tallied statistic: how many times a value (a dependency id, a Java version, a
 * language, a build system) has been seen, and what share of the grand total that
 * represents.
 *
 * @param id the raw key (e.g. {@code "web"}, {@code "21"}, {@code "maven"})
 * @param count number of occurrences observed
 * @param share fraction of the grand total this count represents (0.0 - 1.0)
 * @author Sami Ekblad
 */
public record Count(String id, long count, double share) {
}
