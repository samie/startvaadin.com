/*
 * Copyright and licensing information for this source code can be found in
 * the NOTICE and LICENSE files distributed with this project.
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
	void provenanceCommentIsAddedInsideProjectElement() throws IOException {
		ProjectRequest request = createProjectRequest("web");
		Path pom = generateProject(request).getProjectDirectory().resolve("pom.xml");
		String content = Files.readString(pom);
		assertThat(content).containsOnlyOnce(COMMENT);
		int projectStartTagEnd = content.indexOf('>', content.indexOf("<project "));
		assertThat(content.substring(projectStartTagEnd + 1)).startsWith("\n\t" + COMMENT + "\n\t<modelVersion>");
	}

	@Test
	void provenanceCommentIsNotAddedForGradle() throws IOException {
		ProjectRequest request = createProjectRequest("web");
		request.setType("gradle-project");
		Path build = generateProject(request).getProjectDirectory().resolve("build.gradle");
		assertThat(Files.readString(build)).doesNotContain(COMMENT);
	}

}
