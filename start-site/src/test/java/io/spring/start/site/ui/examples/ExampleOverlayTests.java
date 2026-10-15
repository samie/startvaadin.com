/*
 * Copyright 2012 - present the original author or authors.
 */

package io.spring.start.site.ui.examples;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for {@link ExampleOverlay} path and {@code {{package}}} substitution.
 */
class ExampleOverlayTests {

	private final ExampleOverlay overlay = new ExampleOverlay();

	@Test
	void helloWorldOverlayWritesViewWithSubstitutedPackage(@TempDir Path project) throws Exception {
		this.overlay.apply(project, ExampleCatalog.find("helloworld"), "com.acme.app");
		Path view = project.resolve("src/main/java/com/acme/app/HelloView.java");
		assertThat(view).isRegularFile();
		assertThat(Files.readString(view)).contains("package com.acme.app;").contains("TextField").contains("Button");
	}

	@Test
	void crudOverlayWritesEntityRepositoryViewAndSeed(@TempDir Path project) throws Exception {
		this.overlay.apply(project, ExampleCatalog.find("crud"), "com.acme.app");
		Path base = project.resolve("src/main/java/com/acme/app");
		assertThat(base.resolve("Contact.java")).isRegularFile();
		assertThat(base.resolve("ContactRepository.java")).isRegularFile();
		assertThat(base.resolve("ContactView.java")).isRegularFile();
		assertThat(base.resolve("SampleData.java")).isRegularFile();
	}

	@Test
	void mobileOverlayWritesPwaShell(@TempDir Path project) throws Exception {
		this.overlay.apply(project, ExampleCatalog.find("mobile"), "x.y");
		assertThat(Files.readString(project.resolve("src/main/java/x/y/AppShell.java"))).contains("@PWA");
	}

}
