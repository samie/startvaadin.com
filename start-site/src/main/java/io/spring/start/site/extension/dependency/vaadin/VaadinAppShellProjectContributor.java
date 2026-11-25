/*
 * Copyright and licensing information for this source code can be found in
 * the NOTICE and LICENSE files distributed with this project.
 */

package io.spring.start.site.extension.dependency.vaadin;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import io.spring.initializr.generator.project.contributor.ProjectContributor;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

/**
 * A {@link ProjectContributor} that adds an {@code AppShell} to generated Java Vaadin
 * projects. It loads the Aura theme explicitly, follows the operating system's light or
 * dark colour scheme, and loads the project's own {@code styles.css}, which it also adds.
 * Without it, Vaadin falls back to an implicit Aura theme in light mode only.
 *
 * @author Sami Ekblad
 */
class VaadinAppShellProjectContributor implements ProjectContributor {

	private static final String RESOURCE_BASE = "vaadin/app-shell/";

	private static final String PACKAGE_TOKEN = "{{package}}";

	private final String packageName;

	VaadinAppShellProjectContributor(String packageName) {
		this.packageName = packageName;
	}

	@Override
	public void contribute(Path projectRoot) throws IOException {
		Path sources = projectRoot.resolve("src/main/java").resolve(this.packageName.replace('.', '/'));
		writeString(sources.resolve("AppShell.java"),
				readResource("AppShell.java").replace(PACKAGE_TOKEN, this.packageName));
		writeString(projectRoot.resolve("src/main/resources/META-INF/resources/styles.css"),
				readResource("styles.css"));
	}

	private String readResource(String name) throws IOException {
		try (InputStream input = new ClassPathResource(RESOURCE_BASE + name).getInputStream()) {
			return StreamUtils.copyToString(input, StandardCharsets.UTF_8);
		}
	}

	private void writeString(Path target, String content) throws IOException {
		Files.createDirectories(target.getParent());
		Files.writeString(target, content);
	}

}
