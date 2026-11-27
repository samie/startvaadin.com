/*
 * Copyright and licensing information for this source code can be found in
 * the NOTICE and LICENSE files distributed with this project.
 */

package io.spring.start.site.ui;

import java.io.ByteArrayInputStream;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import io.spring.initializr.metadata.DefaultMetadataElement;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Smoke tests for {@link InitializrUiService}, the bridge that the Vaadin UI uses to talk
 * to Initializr.
 *
 * @author Vaadin UI Migration
 */
@SpringBootTest
@ActiveProfiles("test")
class InitializrUiServiceTests {

	@Autowired
	private InitializrUiService service;

	@Test
	void defaultFormModelUsesVaadinMavenJava21Defaults() {
		InitializrFormModel model = this.service.defaultFormModel();
		assertThat(model.getType()).isEqualTo("maven-project");
		assertThat(model.getLanguage()).isEqualTo("java");
		assertThat(model.getPackaging()).isEqualTo("jar");
		assertThat(model.getJavaVersion()).isEqualTo("21");
		assertThat(model.getConfigurationFileFormat()).isEqualTo("properties");
		assertThat(model.getGroupId()).isEqualTo("org.vaadin.example");
		assertThat(model.getArtifactId()).isEqualTo("vaadin-demo");
		assertThat(model.getPackageName()).isEqualTo("org.vaadin.example");
		assertThat(model.getBootVersion()).isNotBlank();
		assertThat(model.getDependencies()).contains("vaadin");
	}

	@Test
	void generateMavenZipProducesValidZipWithPomAndApplicationClass() throws Exception {
		InitializrFormModel model = this.service.defaultFormModel();
		model.setType("maven-project");
		byte[] zip = this.service.generateZip(model);
		assertThat(zip).isNotEmpty();
		Set<String> entries = listEntries(zip);
		assertThat(entries).anyMatch((entry) -> entry.endsWith("/pom.xml"));
		assertThat(entries).anyMatch((entry) -> entry.endsWith("Application.java"));
	}

	@Test
	void typesOfferOnlyFullProjects() {
		assertThat(this.service.getTypes()).extracting(DefaultMetadataElement::getId)
			.containsExactlyInAnyOrder("gradle-project", "gradle-project-kotlin", "maven-project");
	}

	@Test
	void buildOnlyTypesAreMappedToFullProjects() {
		InitializrFormModel model = new InitializrFormModel();
		model.setType("maven-build");
		assertThat(model.getType()).isEqualTo("maven-project");
		model.setType("gradle-build");
		assertThat(model.getType()).isEqualTo("gradle-project");
		model.setType("gradle-project-kotlin");
		assertThat(model.getType()).isEqualTo("gradle-project-kotlin");
	}

	@Test
	void generatedMavenProjectKeepsPomProvenanceComment() {
		InitializrFormModel model = this.service.defaultFormModel();
		Map<String, String> files = this.service.explore(model);
		assertThat(files).hasEntrySatisfying("vaadin-demo/pom.xml",
				(pom) -> assertThat(pom).contains("<!-- Project from https://pro.startvaadin.com/ -->"));
	}

	@Test
	void generatedMavenExampleProjectKeepsPomProvenanceComment() {
		InitializrFormModel model = this.service.defaultFormModel();
		model.setExampleId("crud");
		Map<String, String> files = this.service.explore(model);
		assertThat(files).hasEntrySatisfying("vaadin-demo/pom.xml",
				(pom) -> assertThat(pom).contains("<!-- Project from https://pro.startvaadin.com/ -->"));
	}

	@Test
	void exploreReturnsTextContentForCommonFiles() {
		InitializrFormModel model = this.service.defaultFormModel();
		model.setType("maven-project");
		Map<String, String> files = this.service.explore(model);
		assertThat(files).isNotEmpty();
		String pom = files.entrySet()
			.stream()
			.filter((entry) -> entry.getKey().endsWith("/pom.xml"))
			.map(Map.Entry::getValue)
			.findFirst()
			.orElseThrow();
		assertThat(pom).contains("<groupId>org.springframework.boot</groupId>");
	}

	@Test
	void selectingHelloWorldExampleOverlaysViewSource() {
		InitializrFormModel model = this.service.defaultFormModel();
		model.setExampleId("helloworld");
		Map<String, String> files = this.service.explore(model);
		String view = files.entrySet()
			.stream()
			.filter((entry) -> entry.getKey().endsWith("/HelloView.java"))
			.map(Map.Entry::getValue)
			.findFirst()
			.orElseThrow();
		assertThat(view).contains("package org.vaadin.example;").contains("class HelloView");
	}

	@Test
	void selectingCrudExampleAddsJpaAndH2AndOverlaysSource() {
		InitializrFormModel model = this.service.defaultFormModel();
		model.setExampleId("crud");
		Map<String, String> files = this.service.explore(model);
		assertThat(files.keySet()).anyMatch((key) -> key.endsWith("/ContactView.java"))
			.anyMatch((key) -> key.endsWith("/ContactRepository.java"));
		String pom = files.entrySet()
			.stream()
			.filter((entry) -> entry.getKey().endsWith("/pom.xml"))
			.map(Map.Entry::getValue)
			.findFirst()
			.orElseThrow();
		assertThat(pom).contains("spring-boot-starter-data-jpa").contains("com.h2database");
	}

	@Test
	void noExampleSelectedLeavesProjectWithoutOverlay() {
		InitializrFormModel model = this.service.defaultFormModel();
		Map<String, String> files = this.service.explore(model);
		assertThat(files.keySet()).noneMatch((key) -> key.endsWith("/HelloView.java"));
	}

	@ParameterizedTest
	@ValueSource(strings = { "helloworld", "crud", "dashboard", "mobile" })
	void everyExampleProjectGetsTheVaadinDirectoryRepository(String exampleId) {
		InitializrFormModel model = this.service.defaultFormModel();
		model.setExampleId(exampleId);
		assertThat(pomOf(this.service.explore(model))).contains("<id>vaadin-addons</id>")
			.contains("<url>https://maven.vaadin.com/vaadin-addons</url>");
	}

	@Test
	void gradleExampleProjectGetsTheVaadinDirectoryRepository() {
		InitializrFormModel model = this.service.defaultFormModel();
		model.setType("gradle-project");
		model.setExampleId("helloworld");
		String build = this.service.explore(model)
			.entrySet()
			.stream()
			.filter((entry) -> entry.getKey().endsWith("/build.gradle"))
			.map(Map.Entry::getValue)
			.findFirst()
			.orElseThrow();
		assertThat(build).contains("https://maven.vaadin.com/vaadin-addons");
	}

	@Test
	void projectWithoutExampleHasNoVaadinDirectoryRepository() {
		InitializrFormModel model = this.service.defaultFormModel();
		assertThat(pomOf(this.service.explore(model))).doesNotContain("vaadin-addons");
	}

	@Test
	void dashboardExampleAddsSvgVisualizations() {
		InitializrFormModel model = this.service.defaultFormModel();
		model.setExampleId("dashboard");
		assertThat(pomOf(this.service.explore(model))).contains("<artifactId>svg-visualizations</artifactId>");
	}

	@Test
	void mobileExampleReplacesTheGeneratedAppShellWithItsPwaOne() {
		InitializrFormModel model = this.service.defaultFormModel();
		model.setExampleId("mobile");
		Map<String, String> files = this.service.explore(model);
		String appShell = files.entrySet()
			.stream()
			.filter((entry) -> entry.getKey().endsWith("/AppShell.java"))
			.map(Map.Entry::getValue)
			.findFirst()
			.orElseThrow();
		assertThat(appShell).contains("@PWA(").contains("@ColorScheme(ColorScheme.Value.LIGHT_DARK)");
	}

	@Test
	void noExampleSelectedStillGetsTheGeneratedAppShell() {
		InitializrFormModel model = this.service.defaultFormModel();
		Map<String, String> files = this.service.explore(model);
		assertThat(files.keySet()).anyMatch((key) -> key.endsWith("/AppShell.java"));
	}

	@Test
	void javaVersionsIncludeJava17() {
		assertThat(this.service.getJavaVersions()).extracting(DefaultMetadataElement::getId)
			.containsExactly("27", "25", "21", "17");
	}

	@Test
	void dependencyGroupsIncludeAllEntriesAndTagCompatibility() {
		InitializrFormModel model = this.service.defaultFormModel();
		var groups = this.service.dependencyGroups(model.getBootVersion());
		assertThat(groups).isNotEmpty()
			.allSatisfy((group) -> assertThat(group.items()).isNotEmpty()
				.allSatisfy((entry) -> assertThat(entry.dependency()).isNotNull()));
	}

	@Test
	void dependencyGroupsFlagOutOfRangeEntriesAsInvalid() {
		String farFutureBoot = "99.99.99";
		var groups = this.service.dependencyGroups(farFutureBoot);
		boolean anyInvalid = groups.stream()
			.flatMap((group) -> group.items().stream())
			.anyMatch((entry) -> !entry.valid() && entry.invalidMessage() != null
					&& entry.invalidMessage().startsWith("Requires Spring Boot "));
		assertThat(anyInvalid).as("at least one dep should be tagged invalid for boot version %s", farFutureBoot)
			.isTrue();
	}

	@Test
	void groupNameNamesTheCatalogGroupOfADependency() {
		assertThat(this.service.groupName("devtools")).contains("Developer Tools");
		assertThat(this.service.groupName("vaadin-webpush")).contains("Vaadin Framework");
		assertThat(this.service.groupName("svg-visualizations")).contains("Vaadin Add-ons");
		assertThat(this.service.groupName("no-such-dependency")).isEmpty();
	}

	@Test
	void incompatibilityHintReturnsNullWhenCompatible() {
		InitializrFormModel model = this.service.defaultFormModel();
		var dep = this.service.getDependency("web").orElseThrow();
		assertThat(this.service.incompatibilityHint(dep, model.getBootVersion())).isNull();
	}

	@Test
	void vaadinIsCompatibleWithSpringBoot41() {
		var vaadin = this.service.getDependency("vaadin").orElseThrow();
		assertThat(this.service.incompatibilityHint(vaadin, "4.1.0")).isNull();
	}

	private Set<String> listEntries(byte[] zip) throws Exception {
		Set<String> entries = new HashSet<>();
		try (ZipInputStream stream = new ZipInputStream(new ByteArrayInputStream(zip))) {
			for (ZipEntry entry = stream.getNextEntry(); entry != null; entry = stream.getNextEntry()) {
				entries.add(entry.getName());
			}
		}
		return entries;
	}

	private static String pomOf(Map<String, String> files) {
		return files.entrySet()
			.stream()
			.filter((entry) -> entry.getKey().endsWith("/pom.xml"))
			.map(Map.Entry::getValue)
			.findFirst()
			.orElseThrow();
	}

}
