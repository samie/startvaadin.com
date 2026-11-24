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
import io.spring.initializr.web.support.InitializrMetadataUpdateStrategy;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ClassPathResource;

/**
 * Builds the Initializr metadata the same way {@link InitializrAutoConfiguration} does,
 * with the {@link ExtraDependencies} merged in. Registered as an auto-configuration that
 * runs first, so that the upstream metadata provider backs off without any change to
 * upstream code.
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
	InitializrMetadataProvider initializrMetadataProvider(InitializrProperties properties,
			ExtraDependencies extraDependencies,
			ObjectProvider<InitializrMetadataUpdateStrategy> initializrMetadataUpdateStrategy) {
		InitializrMetadata metadata = InitializrMetadataBuilder.fromInitializrProperties(properties)
			.withCustomizer(extraDependencies)
			.build();
		return new DefaultInitializrMetadataProvider(metadata,
				initializrMetadataUpdateStrategy.getIfAvailable(() -> (current) -> current));
	}

}
