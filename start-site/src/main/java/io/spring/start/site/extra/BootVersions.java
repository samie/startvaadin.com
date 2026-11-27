/*
 * Copyright and licensing information for this source code can be found in
 * the NOTICE and LICENSE files distributed with this project.
 */

package io.spring.start.site.extra;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.List;

import io.spring.initializr.metadata.DefaultMetadataElement;
import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.metadata.InitializrMetadataCustomizer;
import io.spring.initializr.metadata.InitializrProperties;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.Resource;

/**
 * The Spring Boot versions offered in the UI, read from a snapshot taken at deploy time
 * instead of being fetched from spring.io while the site runs. The snapshot is written by
 * {@code BootVersionsSnapshotTests} with the unmodified upstream
 * {@code StartInitializrMetadataUpdateStrategy}, so it holds exactly what the runtime
 * refresh would have produced. Without the file, the fallback list in
 * {@code application.yml} is used.
 *
 * @author Sami Ekblad
 */
public class BootVersions implements InitializrMetadataCustomizer {

	/**
	 * Classpath location of the Spring Boot versions snapshot.
	 */
	public static final String LOCATION = "boot-versions.yml";

	private final List<DefaultMetadataElement> versions;

	BootVersions(List<DefaultMetadataElement> versions) {
		this.versions = List.copyOf(versions);
	}

	/**
	 * Read the snapshot from the given YAML resource.
	 * @param resource the resource with an {@code initializr.bootVersions} list
	 * @return the Spring Boot versions, empty if the resource does not exist
	 */
	public static BootVersions load(Resource resource) {
		if (!resource.exists()) {
			return new BootVersions(List.of());
		}
		try {
			Binder binder = new Binder(
					ConfigurationPropertySources.from(new YamlPropertySourceLoader().load(LOCATION, resource)));
			return new BootVersions(binder.bind("initializr", InitializrProperties.class)
				.map(InitializrProperties::getBootVersions)
				.orElse(List.of()));
		}
		catch (IOException ex) {
			throw new UncheckedIOException(ex);
		}
	}

	public List<DefaultMetadataElement> getVersions() {
		return this.versions;
	}

	@Override
	public void customize(InitializrMetadata metadata) {
		if (!this.versions.isEmpty()) {
			metadata.updateSpringBootVersions(this.versions);
		}
	}

}
