/*
 * Copyright and licensing information for this source code can be found in
 * the NOTICE and LICENSE files distributed with this project.
 */

package io.spring.start.site.ui;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Mutable POJO bound to the Vaadin form by {@code Binder}. Mirrors the React
 * {@code Initializr.js} reducer's {@code values} shape.
 *
 * @author Vaadin UI Migration
 */
public class InitializrFormModel {

	private String type;

	private String language;

	private String bootVersion;

	private String groupId;

	private String artifactId;

	private String name;

	private String description;

	private String packageName;

	private String packaging;

	/**
	 * The lowest Java version this instance generates projects for. Vaadin 25 needs Java
	 * 21, so older versions are not offered, and an older one from a bookmark, a share
	 * link or the history is raised to it.
	 */
	public static final int MINIMUM_JAVA_VERSION = 21;

	private String javaVersion;

	private String configurationFileFormat;

	private String exampleId;

	private Set<String> dependencies = new LinkedHashSet<>();

	public String getType() {
		return this.type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getLanguage() {
		return this.language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}

	public String getBootVersion() {
		return this.bootVersion;
	}

	public void setBootVersion(String bootVersion) {
		this.bootVersion = bootVersion;
	}

	public String getGroupId() {
		return this.groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public String getArtifactId() {
		return this.artifactId;
	}

	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}

	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getPackageName() {
		return this.packageName;
	}

	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public String getPackaging() {
		return this.packaging;
	}

	public void setPackaging(String packaging) {
		this.packaging = packaging;
	}

	public String getJavaVersion() {
		return this.javaVersion;
	}

	public void setJavaVersion(String javaVersion) {
		this.javaVersion = isBelowMinimum(javaVersion) ? String.valueOf(MINIMUM_JAVA_VERSION) : javaVersion;
	}

	/**
	 * Whether the given Java version id is older than {@link #MINIMUM_JAVA_VERSION}.
	 * @param javaVersion a Java version id such as {@code 17} or {@code 21}
	 * @return {@code true} for an older version, {@code false} otherwise, including for
	 * {@code null} and ids that are not a plain number
	 */
	public static boolean isBelowMinimum(String javaVersion) {
		if (javaVersion == null) {
			return false;
		}
		try {
			return Integer.parseInt(javaVersion.strip()) < MINIMUM_JAVA_VERSION;
		}
		catch (NumberFormatException ex) {
			return false;
		}
	}

	public String getConfigurationFileFormat() {
		return this.configurationFileFormat;
	}

	public void setConfigurationFileFormat(String configurationFileFormat) {
		this.configurationFileFormat = configurationFileFormat;
	}

	public String getExampleId() {
		return this.exampleId;
	}

	public void setExampleId(String exampleId) {
		this.exampleId = exampleId;
	}

	public Set<String> getDependencies() {
		return this.dependencies;
	}

	public void setDependencies(Set<String> dependencies) {
		this.dependencies = (dependencies != null) ? new LinkedHashSet<>(dependencies) : new LinkedHashSet<>();
	}

}
