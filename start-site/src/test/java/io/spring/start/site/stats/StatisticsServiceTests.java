/*
 * Copyright 2012 - present the original author or authors.
 */

package io.spring.start.site.stats;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import io.spring.initializr.web.project.ProjectGeneratedEvent;
import io.spring.initializr.web.project.WebProjectRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import tools.jackson.databind.json.JsonMapper;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link StatisticsService}.
 *
 * @author Sami Ekblad
 */
class StatisticsServiceTests {

	@Test
	void talliesGenerationsAcrossDimensions() {
		StatisticsService service = new StatisticsService(JsonMapper.builder().build(), null);
		service.onProjectGenerated(event("maven-project", "3.5.0", "21", "java", "web", "data-jpa"));
		service.onProjectGenerated(event("gradle-project", "3.5.0", "21", "java", "web"));
		service.onProjectGenerated(event("maven-project", "3.5.0", "17", "kotlin", "web"));

		assertThat(service.total()).isEqualTo(3);
		assertThat(service.topDependencies(10)).first().satisfies((count) -> {
			assertThat(count.id()).isEqualTo("web");
			assertThat(count.count()).isEqualTo(3);
			assertThat(count.share()).isEqualTo(1.0);
		});
		assertThat(service.javaVersionDistribution()).extracting(Count::id).containsExactly("21", "17");
		assertThat(service.buildSystemDistribution()).extracting(Count::id).containsExactly("maven", "gradle");
		assertThat(service.languageDistribution()).extracting(Count::id).containsExactly("java", "kotlin");
	}

	@Test
	void ignoresBlankValues() {
		StatisticsService service = new StatisticsService(JsonMapper.builder().build(), null);
		service.onProjectGenerated(event("maven-project", null, null, null));

		assertThat(service.total()).isEqualTo(1);
		assertThat(service.javaVersionDistribution()).isEmpty();
		assertThat(service.buildSystemDistribution()).extracting(Count::id).containsExactly("maven");
	}

	@Test
	void persistsAndReloadsFromFile(@TempDir Path dir) {
		Path file = dir.resolve("stats.json");
		StatisticsService first = new StatisticsService(JsonMapper.builder().build(), file.toString());
		first.onProjectGenerated(event("maven-project", "3.5.0", "21", "java", "web"));
		first.onProjectGenerated(event("maven-project", "3.5.0", "21", "java", "actuator"));
		first.destroy();
		assertThat(file).exists();

		StatisticsService reloaded = new StatisticsService(JsonMapper.builder().build(), file.toString());
		assertThat(reloaded.total()).isEqualTo(2);
		assertThat(reloaded.topDependencies(10)).extracting(Count::id).contains("web", "actuator");
		assertThat(reloaded.javaVersionDistribution()).extracting(Count::id).containsExactly("21");
	}

	private static ProjectGeneratedEvent event(String type, String bootVersion, String javaVersion, String language,
			String... dependencies) {
		WebProjectRequest request = new WebProjectRequest();
		request.setType(type);
		request.setBootVersion(bootVersion);
		request.setJavaVersion(javaVersion);
		request.setLanguage(language);
		request.setDependencies(new ArrayList<>(List.of(dependencies)));
		return new ProjectGeneratedEvent(request, null);
	}

}
