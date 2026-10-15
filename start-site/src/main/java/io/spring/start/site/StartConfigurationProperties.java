/*
 * Copyright 2012 - present the original author or authors.
 */

package io.spring.start.site;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for the application.
 *
 * @author Moritz Halbritter
 */
@ConfigurationProperties(prefix = "application")
public class StartConfigurationProperties {

	/**
	 * Configuration for the Maven version resolver.
	 */
	private final MavenVersionResolver mavenVersionResolver = new MavenVersionResolver();

	/**
	 * Configuration for the usage statistics page.
	 */
	private final Statistics statistics = new Statistics();

	/**
	 * Public base URL of the deployed site, used to build absolute canonical, Open Graph
	 * and Twitter URLs for social sharing and SEO. A trailing slash is ignored.
	 */
	private String publicUrl = "https://pro.startvaadin.com";

	public MavenVersionResolver getMavenVersionResolver() {
		return this.mavenVersionResolver;
	}

	public Statistics getStatistics() {
		return this.statistics;
	}

	public String getPublicUrl() {
		return this.publicUrl;
	}

	public void setPublicUrl(String publicUrl) {
		this.publicUrl = publicUrl;
	}

	public static class MavenVersionResolver {

		/**
		 * Directory to use for the cache. If not set, a newly created temporary directory
		 * will be used.
		 */
		private String cacheDirectory;

		public String getCacheDirectory() {
			return this.cacheDirectory;
		}

		public void setCacheDirectory(String cacheDirectory) {
			this.cacheDirectory = cacheDirectory;
		}

	}

	public static class Statistics {

		/**
		 * File used to persist usage counters so they survive restarts. If not set,
		 * counters are kept in memory only and reset on restart. Production deployments
		 * should point this at durable storage.
		 */
		private String file;

		/**
		 * How often (in seconds) the statistics page polls for fresh numbers.
		 */
		private int pollIntervalSeconds = 10;

		public String getFile() {
			return this.file;
		}

		public void setFile(String file) {
			this.file = file;
		}

		public int getPollIntervalSeconds() {
			return this.pollIntervalSeconds;
		}

		public void setPollIntervalSeconds(int pollIntervalSeconds) {
			this.pollIntervalSeconds = pollIntervalSeconds;
		}

	}

}
