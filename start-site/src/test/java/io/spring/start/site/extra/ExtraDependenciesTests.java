/*
 * Copyright and licensing information for this source code can be found in
 * the NOTICE and LICENSE files distributed with this project.
 */

package io.spring.start.site.extra;

import java.io.IOException;

import io.spring.initializr.metadata.Dependency;
import io.spring.initializr.metadata.DependencyGroup;
import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.metadata.InitializrMetadataBuilder;
import io.spring.initializr.metadata.InitializrProperties;
import org.junit.jupiter.api.Test;

import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;

/**
 * Tests for {@link ExtraDependencies}, merged into the real start.spring.io catalog.
 *
 * @author Sami Ekblad
 */
class ExtraDependenciesTests {

	private final ExtraDependencies extras = ExtraDependencies.load(new ClassPathResource(ExtraDependencies.LOCATION));

	@Test
	void extrasAreMergedIntoTheStartSpringIoCatalog() throws IOException {
		InitializrMetadata metadata = InitializrMetadataBuilder.fromInitializrProperties(upstream())
			.withCustomizer(this.extras)
			.build();
		Dependency webPush = metadata.getDependencies().get("vaadin-webpush");
		assertThat(webPush).isNotNull();
		assertThat(webPush.getBom()).isEqualTo("vaadin");
		Dependency viritin = metadata.getDependencies().get("viritin");
		assertThat(viritin).isNotNull();
		assertThat(viritin.getVersion()).isNotBlank();
		assertThat(metadata.getConfiguration().getEnv().getRepositories()).containsKey("vaadin-addons");
		assertThat(metadata.getDependencies().getContent()).extracting(DependencyGroup::getName)
			.contains("Web", "Vaadin Framework", "Vaadin Add-ons");
	}

	@Test
	void groupNameOfStartSpringIoIsRejected() throws IOException {
		InitializrProperties clashing = new InitializrProperties();
		DependencyGroup group = DependencyGroup.create("Web");
		group.getContent().add(Dependency.withId("my-extra", "com.example", "my-extra"));
		clashing.getDependencies().add(group);
		InitializrMetadataBuilder builder = InitializrMetadataBuilder.fromInitializrProperties(upstream())
			.withCustomizer(new ExtraDependencies(clashing));
		assertThatIllegalStateException().isThrownBy(builder::build).withMessageContaining("'Web'");
	}

	@Test
	void dependencyIdOfStartSpringIoIsRejected() throws IOException {
		InitializrProperties clashing = new InitializrProperties();
		DependencyGroup group = DependencyGroup.create("My Extras");
		group.getContent().add(Dependency.withId("vaadin", "com.example", "my-vaadin"));
		clashing.getDependencies().add(group);
		InitializrMetadataBuilder builder = InitializrMetadataBuilder.fromInitializrProperties(upstream())
			.withCustomizer(new ExtraDependencies(clashing));
		assertThatIllegalStateException().isThrownBy(builder::build).withMessageContaining("'vaadin'");
	}

	private static InitializrProperties upstream() throws IOException {
		ClassPathResource resource = new ClassPathResource("application.yml");
		Binder binder = new Binder(
				ConfigurationPropertySources.from(new YamlPropertySourceLoader().load("application.yml", resource)));
		return binder.bind("initializr", InitializrProperties.class).get();
	}

}
