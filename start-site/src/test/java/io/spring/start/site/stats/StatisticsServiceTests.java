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
	void talliesBootVersionPackagingAndUiAction() {
		StatisticsService service = new StatisticsService(JsonMapper.builder().build(), null);
		service.onProjectGenerated(uiEvent("3.5.0", "jar", "generate"));
		service.onProjectGenerated(uiEvent("3.5.0", "war", "explore"));
		service.onProjectGenerated(uiEvent("3.4.0", "jar", null));

		assertThat(service.bootVersionDistribution()).extracting(Count::id).containsExactly("3.5.0", "3.4.0");
		assertThat(service.packagingDistribution()).extracting(Count::id).containsExactly("jar", "war");
		assertThat(service.uiActionDistribution()).extracting(Count::id)
			.containsExactlyInAnyOrder("generate", "explore", "cli");
	}

	@Test
	void recordsArrivalsByReferrerHost() {
		StatisticsService service = new StatisticsService(JsonMapper.builder().build(), null);
		service.recordArrival("https://news.ycombinator.com/item?id=1", "start.spring.io");
		service.recordArrival(null, "start.spring.io");
		service.recordArrival("https://start.spring.io/", "start.spring.io");

		assertThat(service.referrerDistribution()).extracting(Count::id)
			.containsExactlyInAnyOrder("news.ycombinator.com", "direct");
		assertThat(service.referrerDistribution()).filteredOn((count) -> count.id().equals("direct"))
			.first()
			.satisfies((count) -> assertThat(count.count()).isEqualTo(2));
	}

	@Test
	void referrerHostNormalization() {
		assertThat(StatisticsService.referrerHost(null, "start.spring.io")).isEqualTo("direct");
		assertThat(StatisticsService.referrerHost("   ", "start.spring.io")).isEqualTo("direct");
		assertThat(StatisticsService.referrerHost("https://start.spring.io/x", "start.spring.io")).isEqualTo("direct");
		assertThat(StatisticsService.referrerHost("https://Google.com/search", "start.spring.io"))
			.isEqualTo("google.com");
		assertThat(StatisticsService.referrerHost("not a url", "start.spring.io")).isEqualTo("direct");
	}

	@Test
	void persistsAndReloadsFromFile(@TempDir Path dir) {
		Path file = dir.resolve("stats.json");
		StatisticsService first = new StatisticsService(JsonMapper.builder().build(), file.toString());
		first.onProjectGenerated(uiEvent("3.5.0", "war", "generate", "web"));
		first.onProjectGenerated(uiEvent("3.5.0", "jar", "explore", "actuator"));
		first.recordArrival("https://news.ycombinator.com/", "start.spring.io");
		first.destroy();
		assertThat(file).exists();

		StatisticsService reloaded = new StatisticsService(JsonMapper.builder().build(), file.toString());
		assertThat(reloaded.total()).isEqualTo(2);
		assertThat(reloaded.topDependencies(10)).extracting(Count::id).contains("web", "actuator");
		assertThat(reloaded.javaVersionDistribution()).extracting(Count::id).containsExactly("21");
		assertThat(reloaded.packagingDistribution()).extracting(Count::id).containsExactlyInAnyOrder("war", "jar");
		assertThat(reloaded.uiActionDistribution()).extracting(Count::id)
			.containsExactlyInAnyOrder("generate", "explore");
		assertThat(reloaded.referrerDistribution()).extracting(Count::id).containsExactly("news.ycombinator.com");
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

	private static ProjectGeneratedEvent uiEvent(String bootVersion, String packaging, String uiAction,
			String... dependencies) {
		WebProjectRequest request = new WebProjectRequest();
		request.setType("maven-project");
		request.setBootVersion(bootVersion);
		request.setJavaVersion("21");
		request.setLanguage("java");
		request.setPackaging(packaging);
		if (uiAction != null) {
			request.getParameters().put(StatisticsService.UI_ACTION_PARAMETER, uiAction);
		}
		request.setDependencies(new ArrayList<>(List.of(dependencies)));
		return new ProjectGeneratedEvent(request, null);
	}

}
