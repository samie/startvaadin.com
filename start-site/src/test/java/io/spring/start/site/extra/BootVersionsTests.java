/*
 * Copyright and licensing information for this source code can be found in
 * the NOTICE and LICENSE files distributed with this project.
 */

package io.spring.start.site.extra;

import java.io.IOException;
import java.util.List;

import io.spring.initializr.generator.version.Version;
import io.spring.initializr.metadata.DefaultMetadataElement;
import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.metadata.InitializrMetadataBuilder;
import io.spring.initializr.metadata.InitializrProperties;
import org.junit.jupiter.api.Test;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link BootVersions} and the committed snapshot.
 *
 * @author Sami Ekblad
 */
class BootVersionsTests {

	private final BootVersions snapshot = BootVersions.load(new ClassPathResource(BootVersions.LOCATION));

	@Test
	void snapshotHasOneDefaultAndOnlySupportedVersions() {
		List<DefaultMetadataElement> versions = this.snapshot.getVersions();
		assertThat(versions).isNotEmpty();
		assertThat(versions).filteredOn(DefaultMetadataElement::isDefault).hasSize(1);
		assertThat(versions).allSatisfy(
				(version) -> assertThat(Version.parse(version.getId())).isGreaterThanOrEqualTo(Version.parse("4.0.0")));
	}

	@Test
	void snapshotReplacesTheFallbackVersions() throws IOException {
		InitializrMetadata metadata = InitializrMetadataBuilder.fromInitializrProperties(upstream())
			.withCustomizer(this.snapshot)
			.build();
		assertThat(metadata.getBootVersions().getContent()).extracting(DefaultMetadataElement::getId)
			.containsExactlyElementsOf(
					this.snapshot.getVersions().stream().map(DefaultMetadataElement::getId).toList());
		assertThat(metadata.getBootVersions().getDefault().getId()).isEqualTo(this.snapshot.getVersions()
			.stream()
			.filter(DefaultMetadataElement::isDefault)
			.findFirst()
			.orElseThrow()
			.getId());
	}

	@Test
	void missingSnapshotKeepsTheFallbackVersions() throws IOException {
		BootVersions none = BootVersions.load(new ByteArrayResource(new byte[0]) {
			@Override
			public boolean exists() {
				return false;
			}
		});
		InitializrMetadata fallback = InitializrMetadataBuilder.fromInitializrProperties(upstream()).build();
		InitializrMetadata metadata = InitializrMetadataBuilder.fromInitializrProperties(upstream())
			.withCustomizer(none)
			.build();
		assertThat(metadata.getBootVersions().getContent()).extracting(DefaultMetadataElement::getId)
			.containsExactlyElementsOf(
					fallback.getBootVersions().getContent().stream().map(DefaultMetadataElement::getId).toList());
	}

	private static InitializrProperties upstream() throws IOException {
		ClassPathResource resource = new ClassPathResource("application.yml");
		Binder binder = new Binder(
				ConfigurationPropertySources.from(new YamlPropertySourceLoader().load("application.yml", resource)));
		return binder.bind("initializr", InitializrProperties.class).get();
	}

}
