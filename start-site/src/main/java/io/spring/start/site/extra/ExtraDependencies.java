/*
 * Copyright 2012 - present the original author or authors.
 */

package io.spring.start.site.extra;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import io.spring.initializr.metadata.Dependency;
import io.spring.initializr.metadata.DependencyGroup;
import io.spring.initializr.metadata.InitializrConfiguration.Env;
import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.metadata.InitializrMetadataCustomizer;
import io.spring.initializr.metadata.InitializrProperties;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.Resource;

/**
 * Dependencies offered on top of the start.spring.io catalog. They are declared in a file
 * of their own, in the same format as the {@code initializr} section of
 * {@code application.yml}, and merged into the Initializr metadata so that the upstream
 * catalog stays untouched.
 *
 * @author Sami Ekblad
 */
public class ExtraDependencies implements InitializrMetadataCustomizer {

	/**
	 * Classpath location of the extra dependencies.
	 */
	public static final String LOCATION = "extra-dependencies.yml";

	private final InitializrProperties properties;

	ExtraDependencies(InitializrProperties properties) {
		this.properties = properties;
	}

	/**
	 * Read the extra dependencies from the given YAML resource.
	 * @param resource the resource in {@code application.yml} format
	 * @return the extra dependencies
	 */
	public static ExtraDependencies load(Resource resource) {
		try {
			Binder binder = new Binder(
					ConfigurationPropertySources.from(new YamlPropertySourceLoader().load(LOCATION, resource)));
			return new ExtraDependencies(
					binder.bind("initializr", InitializrProperties.class).orElseGet(InitializrProperties::new));
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	@Override
	public void customize(InitializrMetadata metadata) {
		List<DependencyGroup> groups = this.properties.getDependencies();
		checkNoOverlap(metadata, groups);
		Env env = metadata.getConfiguration().getEnv();
		this.properties.getEnv().getBoms().forEach(env.getBoms()::putIfAbsent);
		this.properties.getEnv().getRepositories().forEach(env.getRepositories()::putIfAbsent);
		metadata.getDependencies().merge(groups);
	}

	// Initializr would silently skip a group whose name is already taken
	private static void checkNoOverlap(InitializrMetadata metadata, List<DependencyGroup> groups) {
		Set<String> groupNames = metadata.getDependencies()
			.getContent()
			.stream()
			.map(DependencyGroup::getName)
			.collect(Collectors.toSet());
		for (DependencyGroup group : groups) {
			if (groupNames.contains(group.getName())) {
				throw new IllegalStateException("Extra dependency group '" + group.getName()
						+ "' clashes with a start.spring.io group, use a name of its own");
			}
			for (Dependency dependency : group.getContent()) {
				if (metadata.getDependencies().get(dependency.getId()) != null) {
					throw new IllegalStateException("Extra dependency '" + dependency.getId()
							+ "' clashes with a start.spring.io dependency, use an id of its own");
				}
			}
		}
	}

}
