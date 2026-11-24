/*
 * Copyright 2012 - present the original author or authors.
 */

package io.spring.start.site.ui.examples;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import org.springframework.core.io.ClassPathResource;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ExampleOverlay} path and token substitution.
 */
class ExampleOverlayTests {

	private final ExampleOverlay overlay = new ExampleOverlay();

	@Test
	void helloWorldOverlayWritesViewWithSubstitutedPackage(@TempDir Path project) throws Exception {
		this.overlay.apply(project, ExampleCatalog.find("helloworld"), project("com.acme.app"));
		Path view = project.resolve("src/main/java/com/acme/app/HelloView.java");
		assertThat(view).isRegularFile();
		assertThat(Files.readString(view)).contains("package com.acme.app;").contains("TextField").contains("Button");
	}

	@Test
	void overlayWritesTestSourcesWithSubstitutedPackage(@TempDir Path project) throws Exception {
		this.overlay.apply(project, ExampleCatalog.find("helloworld"), project("com.acme.app"));
		Path test = project.resolve("src/test/java/com/acme/app/HelloViewTest.java");
		assertThat(test).isRegularFile();
		assertThat(Files.readString(test)).contains("package com.acme.app;");
	}

	@Test
	void crudOverlayWritesEntityRepositoryViewAndSeed(@TempDir Path project) throws Exception {
		this.overlay.apply(project, ExampleCatalog.find("crud"), project("com.acme.app"));
		Path base = project.resolve("src/main/java/com/acme/app");
		assertThat(base.resolve("Contact.java")).isRegularFile();
		assertThat(base.resolve("ContactRepository.java")).isRegularFile();
		assertThat(base.resolve("ContactView.java")).isRegularFile();
		assertThat(base.resolve("SampleData.java")).isRegularFile();
	}

	@Test
	void mobileOverlayWritesPwaShell(@TempDir Path project) throws Exception {
		this.overlay.apply(project, ExampleCatalog.find("mobile"), project("x.y"));
		assertThat(Files.readString(project.resolve("src/main/java/x/y/AppShell.java"))).contains("@PWA")
			.contains("Aura.STYLESHEET");
		assertThat(project.resolve("src/main/java/x/y/ItemView.java")).isRegularFile();
		assertThat(project.resolve("src/main/java/x/y/ItemService.java")).isRegularFile();
		assertThat(project.resolve("src/main/java/x/y/NotificationService.java")).isRegularFile();
		assertThat(project.resolve("src/main/java/x/y/DemoNotificationSender.java")).isRegularFile();
		assertThat(project.resolve("src/main/resources/META-INF/resources/styles.css")).isRegularFile();
		assertThat(project.resolve("src/main/resources/META-INF/resources/nav-pending.js")).isRegularFile();
	}

	@Test
	void mobileOverlayCopiesIconUnchanged(@TempDir Path project) throws Exception {
		this.overlay.apply(project, ExampleCatalog.find("mobile"), project("x.y"));
		byte[] original = new ClassPathResource("examples/mobile/src/main/resources/META-INF/resources/icons/icon.png")
			.getContentAsByteArray();
		assertThat(project.resolve("src/main/resources/META-INF/resources/icons/icon.png")).hasBinaryContent(original);
	}

	@Test
	void mobileOverlayUsesProjectNameAndDescriptionForTheApp(@TempDir Path project) throws Exception {
		this.overlay.apply(project, ExampleCatalog.find("mobile"),
				new ExampleOverlay.Project("x.y", "Field Notes", "Notes from \"the field\"\nfor teams"));
		assertThat(Files.readString(project.resolve("src/main/java/x/y/AppShell.java")))
			.contains("name = \"Field Notes\"")
			.contains("description = \"Notes from \\\"the field\\\" for teams\"");
	}

	private static ExampleOverlay.Project project(String packageName) {
		return new ExampleOverlay.Project(packageName, "Demo", "A demo project");
	}

}
