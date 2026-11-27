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

import io.spring.initializr.generator.project.contributor.MultipleResourcesProjectContributor;
import io.spring.initializr.generator.project.contributor.ProjectContributor;

import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StreamUtils;

/**
 * A {@link ProjectContributor} that adds agent configuration (Claude Code and OpenAI
 * Codex) to generated Vaadin projects. It configures the public Vaadin documentation and
 * Vaadin Directory MCP servers, per-agent instruction files, and Vaadin's official agent
 * skills ({@code aura-theme}, {@code vaadin-frontend-design}, {@code vaadin-form-layout},
 * {@code vaadin-browserless-testing} and {@code vaadin-usage-review}) so the project is
 * ready to use with AI coding agents without manual setup. The skills are fetched at
 * build time from {@code vaadin/agent-skills} at the commit pinned by the
 * {@code vaadin-agent-skills.ref} property in {@code start-site/pom.xml}.
 *
 * @author Sami Ekblad
 */
class VaadinAgentConfigProjectContributor implements ProjectContributor {

	private static final String RESOURCE_BASE = "vaadin/agent-config/";

	private static final String SKILLS_RESOURCE = "classpath:/" + RESOURCE_BASE + "skills";

	@Override
	public void contribute(Path projectRoot) throws IOException {
		writeString(projectRoot.resolve(".mcp.json"), readResource("mcp.json"));
		writeString(projectRoot.resolve(".codex/config.toml"), readResource("codex-config.toml"));
		writeString(projectRoot.resolve("AGENTS.md"), readResource("AGENTS.md"));
		writeString(projectRoot.resolve("CLAUDE.md"), readResource("CLAUDE.md"));
		// The skills tree is written from a single source to both locations so the two
		// copies cannot drift.
		MultipleResourcesProjectContributor skills = new MultipleResourcesProjectContributor(SKILLS_RESOURCE);
		skills.contribute(projectRoot.resolve(".agents/skills"));
		skills.contribute(projectRoot.resolve(".claude/skills"));
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
