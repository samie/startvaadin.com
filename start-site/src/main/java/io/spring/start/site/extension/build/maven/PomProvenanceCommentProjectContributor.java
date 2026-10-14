/*
 * Copyright 2012 - present the original author or authors.
 */

package io.spring.start.site.extension.build.maven;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import io.spring.initializr.generator.project.contributor.ProjectContributor;

import org.springframework.core.Ordered;

/**
 * A {@link ProjectContributor} that adds a provenance comment to the generated
 * {@code pom.xml}. The Maven build model has no API to emit a top-level comment, so this
 * runs after {@code pom.xml} has been written and inserts the comment right after the XML
 * declaration.
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
		int newline = content.indexOf('\n');
		String result;
		if (newline >= 0 && content.startsWith("<?xml")) {
			// Insert right after the XML declaration line.
			result = content.substring(0, newline + 1) + COMMENT + "\n" + content.substring(newline + 1);
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
