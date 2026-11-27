/*
 * Copyright and licensing information for this source code can be found in
 * the NOTICE and LICENSE files distributed with this project.
 */

package io.spring.start.site.extra;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import io.spring.initializr.metadata.DefaultMetadataElement;
import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.metadata.InitializrMetadataBuilder;
import io.spring.initializr.metadata.InitializrProperties;
import io.spring.start.site.support.StartInitializrMetadataUpdateStrategy;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfSystemProperty;
import tools.jackson.databind.json.JsonMapper;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpHeaders;
import org.springframework.web.client.RestClient;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Writes the {@link BootVersions} snapshot ({@code src/main/resources/boot-versions.yml})
 * from spring.io, using the same {@link StartInitializrMetadataUpdateStrategy} that used
 * to refresh the versions while the site ran. Skipped unless {@code -DbootVersions=write}
 * is set; run on deploy by {@code .local/upgrade-pins.py}. If spring.io cannot be reached
 * the test fails and the existing snapshot is left as it is.
 *
 * @author Sami Ekblad
 */
@EnabledIfSystemProperty(named = "bootVersions", matches = "write")
class BootVersionsSnapshotTests {

	private static final Path SNAPSHOT = Path.of("src/main/resources", BootVersions.LOCATION);

	@Test
	void writeSnapshotFromSpringIo() throws IOException {
		InitializrMetadata metadata = InitializrMetadataBuilder.fromInitializrProperties(upstream()).build();
		RestClient restClient = RestClient.builder().defaultHeader(HttpHeaders.USER_AGENT, "start.spring.io").build();
		InitializrMetadata updated = new StartInitializrMetadataUpdateStrategy(restClient, JsonMapper.builder().build())
			.update(metadata);
		List<DefaultMetadataElement> versions = updated.getBootVersions().getContent();
		assertThat(versions).as("Spring Boot versions from spring.io").isNotEmpty();
		assertThat(versions).filteredOn(DefaultMetadataElement::isDefault).hasSize(1);
		Files.writeString(SNAPSHOT, toYaml(versions));
	}

	private static String toYaml(List<DefaultMetadataElement> versions) {
		StringBuilder yaml = new StringBuilder();
		yaml.append("# Spring Boot versions offered in the UI. Generated from spring.io on deploy by\n")
			.append("# .local/upgrade-pins.py (BootVersionsSnapshotTests); do not edit by hand.\n")
			.append("initializr:\n")
			.append("  bootVersions:\n");
		for (DefaultMetadataElement version : versions) {
			yaml.append("    - name: \"").append(version.getName()).append("\"\n");
			yaml.append("      id: \"").append(version.getId()).append("\"\n");
			yaml.append("      default: ").append(version.isDefault()).append("\n");
		}
		return yaml.toString();
	}

	private static InitializrProperties upstream() throws IOException {
		ClassPathResource resource = new ClassPathResource("application.yml");
		Binder binder = new Binder(
				ConfigurationPropertySources.from(new YamlPropertySourceLoader().load("application.yml", resource)));
		return binder.bind("initializr", InitializrProperties.class).get();
	}

}
