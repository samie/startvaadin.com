/*
 * Copyright 2012 - present the original author or authors.
 */

package io.spring.start.site.extension.build.maven;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import io.spring.initializr.web.project.ProjectRequest;
import io.spring.start.site.extension.AbstractExtensionTests;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Tests for {@link PomProvenanceCommentProjectContributor}.
 *
 * @author Sami Ekblad
 */
class PomProvenanceCommentProjectContributorTests extends AbstractExtensionTests {

	private static final String COMMENT = "<!-- Project from https://pro.startvaadin.com/ -->";

	@Test
	void provenanceCommentIsAddedRightAfterXmlDeclaration() throws IOException {
		ProjectRequest request = createProjectRequest("web");
		Path pom = generateProject(request).getProjectDirectory().resolve("pom.xml");
		String content = Files.readString(pom);
		assertThat(content).startsWith("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" + COMMENT + "\n<project ");
	}

	@Test
	void provenanceCommentIsNotAddedForGradle() throws IOException {
		ProjectRequest request = createProjectRequest("web");
		request.setType("gradle-project");
		Path build = generateProject(request).getProjectDirectory().resolve("build.gradle");
		assertThat(Files.readString(build)).doesNotContain(COMMENT);
	}

}
