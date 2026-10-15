/*
 * Copyright 2012 - present the original author or authors.
 */

package io.spring.start.site.ui;

import com.vaadin.flow.component.icon.SvgIcon;
import io.spring.initializr.metadata.Dependency;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link VendorIcons}.
 *
 * @author Sami Ekblad
 */
class VendorIconsTests {

	@Test
	void knownDependencyResolvesToMatchingSlug() {
		assertThat(VendorIcons.resolveSlug(Dependency.withId("postgresql"))).isEqualTo("postgresql");
		assertThat(VendorIcons.resolveSlug(Dependency.withId("vaadin"))).isEqualTo("vaadin");
	}

	@Test
	void overriddenDependencyResolvesToOverrideSlug() {
		assertThat(VendorIcons.resolveSlug(Dependency.withId("data-redis"))).isEqualTo("redis");
		assertThat(VendorIcons.resolveSlug(Dependency.withId("kafka"))).isEqualTo("apachekafka");
		assertThat(VendorIcons.resolveSlug(Dependency.withId("cassandra"))).isEqualTo("apachecassandra");
	}

	@Test
	void previouslyMissingDependenciesResolveToTheirVendorSlug() {
		assertThat(VendorIcons.resolveSlug(Dependency.withId("docker-compose"))).isEqualTo("docker");
		assertThat(VendorIcons.resolveSlug(Dependency.withId("graphql"))).isEqualTo("graphql");
		assertThat(VendorIcons.resolveSlug(Dependency.withId("security"))).isEqualTo("springsecurity");
		assertThat(VendorIcons.resolveSlug(Dependency.withId("oauth2-client"))).isEqualTo("springsecurity");
	}

	@Test
	void springAuthoredDependencyWithoutVendorIconFallsBackToSpringSlug() {
		Dependency dependency = Dependency.withId("web", "org.springframework.boot", "spring-boot-starter-web");
		assertThat(VendorIcons.resolveSlug(dependency)).isEqualTo("spring");
	}

	@Test
	void unknownDependencyFallsBackToDefaultSlug() {
		assertThat(VendorIcons.resolveSlug(Dependency.withId("some-unmapped-id"))).isEqualTo("openjdk");
		Dependency oracle = Dependency.withId("oracle", "com.oracle.database.jdbc", "ojdbc11");
		assertThat(VendorIcons.resolveSlug(oracle)).isEqualTo("openjdk");
	}

	@Test
	void iconUsesAbsoluteRootRelativeSrcForResolvedSlug() {
		SvgIcon icon = VendorIcons.icon(Dependency.withId("postgresql"));
		assertThat(icon.getSrc()).isEqualTo("/images/vendors/postgresql.svg");
		assertThat(icon.getClassNames()).contains("dependency-icon");
	}

	@Test
	void iconFallsBackToDefaultSrcForUnknownDependency() {
		SvgIcon icon = VendorIcons.icon(Dependency.withId("totally-unknown"));
		assertThat(icon.getSrc()).isEqualTo("/images/vendors/openjdk.svg");
	}

}
