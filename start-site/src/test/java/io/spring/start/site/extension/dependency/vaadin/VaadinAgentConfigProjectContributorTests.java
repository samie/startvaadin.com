/*
 * Copyright 2012 - present the original author or authors.
 */

package io.spring.start.site.extension.dependency.vaadin;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.spring.initializr.generator.test.project.ProjectStructure;
import io.spring.initializr.web.project.ProjectRequest;
import io.spring.start.site.SupportedBootVersion;
import io.spring.start.site.extension.AbstractExtensionTests;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.contentOf;

/**
 * Tests for {@link VaadinAgentConfigProjectContributor}.
 *
 * @author Sami Ekblad
 */
class VaadinAgentConfigProjectContributorTests extends AbstractExtensionTests {

	private static final List<String> VENDORED_SKILLS = List.of("aura-theme", "vaadin-frontend-design",
			"vaadin-form-layout");

	@Test
	void agentConfigIsContributedWithVaadin() {
		ProjectRequest request = createProjectRequest(SupportedBootVersion.V4_1, "vaadin");
		Path project = generateProject(request).getProjectDirectory();
		assertThat(project.resolve(".mcp.json")).isRegularFile();
		assertThat(project.resolve(".codex/config.toml")).isRegularFile();
		assertThat(project.resolve("AGENTS.md")).isRegularFile();
		assertThat(project.resolve("CLAUDE.md")).isRegularFile();
		for (String skill : VENDORED_SKILLS) {
			assertThat(project.resolve(".agents/skills/" + skill + "/SKILL.md")).isRegularFile();
			assertThat(project.resolve(".claude/skills/" + skill + "/SKILL.md")).isRegularFile();
		}
		assertThat(project.resolve(".agents/skills/aura-theme/references/components.md")).isRegularFile();
		assertThat(project.resolve(".claude/skills/aura-theme/references/components.md")).isRegularFile();
		assertThat(contentOf(project.resolve(".mcp.json").toFile())).contains("\"type\": \"http\"")
			.contains("https://mcp.vaadin.com/docs")
			.doesNotContain("vaadin-direcotry");
		assertThatCode(() -> new ObjectMapper().readTree(project.resolve(".mcp.json").toFile()))
			.doesNotThrowAnyException();
		assertThat(contentOf(project.resolve(".codex/config.toml").toFile())).contains("[mcp_servers.vaadin]")
			.contains("https://mcp.vaadin.com/docs");
	}

	@Test
	void skillCopiesAreIdentical() throws Exception {
		ProjectRequest request = createProjectRequest(SupportedBootVersion.V4_1, "vaadin");
		Path project = generateProject(request).getProjectDirectory();
		Path agentsSkills = project.resolve(".agents/skills");
		Path claudeSkills = project.resolve(".claude/skills");
		try (Stream<Path> files = Files.walk(agentsSkills)) {
			files.filter(Files::isRegularFile).forEach((agentsFile) -> {
				Path claudeFile = claudeSkills.resolve(agentsSkills.relativize(agentsFile));
				assertThat(claudeFile).isRegularFile();
				assertThat(contentOf(claudeFile.toFile())).isEqualTo(contentOf(agentsFile.toFile()));
			});
		}
		assertThat(contentOf(agentsSkills.resolve("aura-theme/SKILL.md").toFile())).contains("name: aura-theme");
	}

	@Test
	void agentConfigIsNotContributedWithoutVaadin() {
		ProjectStructure structure = generateProject(createProjectRequest("web"));
		Path project = structure.getProjectDirectory();
		assertThat(project.resolve(".mcp.json")).doesNotExist();
		assertThat(project.resolve(".codex")).doesNotExist();
		assertThat(project.resolve(".agents")).doesNotExist();
		assertThat(project.resolve(".claude")).doesNotExist();
		assertThat(project.resolve("AGENTS.md")).doesNotExist();
		assertThat(project.resolve("CLAUDE.md")).doesNotExist();
	}

}
