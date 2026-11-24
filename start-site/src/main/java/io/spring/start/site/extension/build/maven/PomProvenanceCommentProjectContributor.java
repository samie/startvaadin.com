/*
 * Copyright and licensing information for this source code can be found in
 * the NOTICE and LICENSE files distributed with this project.
 */

package io.spring.start.site.extension.build.maven;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import io.spring.initializr.generator.project.contributor.ProjectContributor;

import org.springframework.core.Ordered;

/**
 * A {@link ProjectContributor} that adds a provenance comment to the generated
 * {@code pom.xml}. The Maven build model has no API to emit comments, so this runs after
 * {@code pom.xml} has been written and inserts the comment into it.
 *
 * @author Sami Ekblad
 */
class PomProvenanceCommentProjectContributor implements ProjectContributor {

	private static final String COMMENT = "<!-- Project from https://pro.startvaadin.com/ -->";

	@Override
	public void contribute(Path projectRoot) throws IOException {
		Path pom = projectRoot.resolve("pom.xml");
		if (!Files.exists(pom)) {
			return;
		}
		String content = Files.readString(pom);
		if (content.contains(COMMENT)) {
			return;
		}
		int project = content.indexOf("<project");
		int startTagEnd = (project >= 0) ? content.indexOf('>', project) : -1;
		String result;
		if (startTagEnd >= 0) {
			result = content.substring(0, startTagEnd + 1) + "\n\t" + COMMENT + content.substring(startTagEnd + 1);
		}
		else {
			result = COMMENT + "\n" + content;
		}
		Files.writeString(pom, result);
	}

	@Override
	public int getOrder() {
		// Run after the pom.xml has been written by MavenBuildProjectContributor (order
		// 0).
		return Ordered.LOWEST_PRECEDENCE;
	}

}
