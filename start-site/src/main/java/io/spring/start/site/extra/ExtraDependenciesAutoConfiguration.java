/*
 * Copyright and licensing information for this source code can be found in
 * the NOTICE and LICENSE files distributed with this project.
 */

package io.spring.start.site.extra;

import io.spring.initializr.metadata.InitializrMetadata;
import io.spring.initializr.metadata.InitializrMetadataBuilder;
import io.spring.initializr.metadata.InitializrMetadataProvider;
import io.spring.initializr.metadata.InitializrProperties;
import io.spring.initializr.web.autoconfigure.InitializrAutoConfiguration;
import io.spring.initializr.web.support.DefaultInitializrMetadataProvider;

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

/**
 * Builds the Initializr metadata the same way {@link InitializrAutoConfiguration} does,
 * with the {@link ExtraDependencies} merged in and the Spring Boot versions taken from
 * the deploy-time {@link BootVersions} snapshot. The metadata is not refreshed from
 * spring.io while the site runs, so what is deployed is exactly what was tested.
 * Registered as an auto-configuration that runs first, so that the upstream metadata
 * provider backs off without any change to upstream code.
 *
 * @author Sami Ekblad
 */
@AutoConfiguration(before = InitializrAutoConfiguration.class)
@EnableConfigurationProperties(InitializrProperties.class)
public class ExtraDependenciesAutoConfiguration {

	@Bean
	ExtraDependencies extraDependencies() {
		return ExtraDependencies.load(new ClassPathResource(ExtraDependencies.LOCATION));
	}

	@Bean
	BootVersions bootVersions() {
		return BootVersions.load(new ClassPathResource(BootVersions.LOCATION));
	}

	@Bean
	InitializrMetadataProvider initializrMetadataProvider(InitializrProperties properties,
			ExtraDependencies extraDependencies, BootVersions bootVersions) {
		InitializrMetadata metadata = InitializrMetadataBuilder.fromInitializrProperties(properties)
			.withCustomizer(extraDependencies)
			.withCustomizer(bootVersions)
			.build();
		return new DefaultInitializrMetadataProvider(metadata, (current) -> current);
	}

}
