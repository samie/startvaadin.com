/*
 * Copyright and licensing information for this source code can be found in
 * the NOTICE and LICENSE files distributed with this project.
 */

package io.spring.start.site.extension.dependency.vaadin;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

import io.spring.initializr.generator.test.project.ProjectStructure;
import io.spring.initializr.web.project.ProjectRequest;
import io.spring.start.site.SupportedBootVersion;
import io.spring.start.site.extension.AbstractExtensionTests;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.contentOf;

/**
 * Tests for {@link VaadinAgentConfigProjectContributor}.
 *
 * @author Sami Ekblad
 */
class VaadinAgentConfigProjectContributorTests extends AbstractExtensionTests {

	/**
	 * The skills fetched from {@code vaadin/agent-skills}. Kept in step with the skill
	 * names listed in {@code AGENTS.md} and {@code CLAUDE.md}; when upstream adds,
	 * removes or renames a skill, {@link #skillsMatchTheFetchedTree()} fails until both
	 * are updated.
	 */
	private static final List<String> SKILLS = List.of("aura-theme", "vaadin-frontend-design", "vaadin-form-layout",
			"vaadin-browserless-testing", "vaadin-usage-review");

	@Test
	void agentConfigIsContributedWithVaadin() {
		ProjectRequest request = createProjectRequest(SupportedBootVersion.V4_1, "vaadin");
		Path project = generateProject(request).getProjectDirectory();
		assertThat(project.resolve(".mcp.json")).isRegularFile();
		assertThat(project.resolve(".codex/config.toml")).isRegularFile();
		assertThat(project.resolve("AGENTS.md")).isRegularFile();
		assertThat(project.resolve("CLAUDE.md")).isRegularFile();
		for (String skill : SKILLS) {
			assertThat(project.resolve(".agents/skills/" + skill + "/SKILL.md")).isRegularFile();
			assertThat(project.resolve(".claude/skills/" + skill + "/SKILL.md")).isRegularFile();
		}
		assertThat(project.resolve(".agents/skills/aura-theme/references/components.md")).isRegularFile();
		assertThat(project.resolve(".claude/skills/aura-theme/references/components.md")).isRegularFile();
		assertThat(contentOf(project.resolve(".mcp.json").toFile())).contains("\"type\": \"http\"")
			.contains("https://mcp.vaadin.com/docs")
			.contains("\"vaadin-directory\"")
			.contains("https://vaadin.com/directory/mcp")
			.doesNotContain("vaadin-direcotry");
		assertThatCode(() -> new ObjectMapper().readTree(project.resolve(".mcp.json").toFile()))
			.doesNotThrowAnyException();
		assertThat(contentOf(project.resolve(".codex/config.toml").toFile())).contains("[mcp_servers.vaadin]")
			.contains("https://mcp.vaadin.com/docs")
			.contains("[mcp_servers.vaadin-directory]")
			.contains("https://vaadin.com/directory/mcp");
		assertThat(contentOf(project.resolve("AGENTS.md").toFile())).contains("`vaadin-directory` MCP server");
		assertThat(contentOf(project.resolve("CLAUDE.md").toFile())).contains("`vaadin-directory` MCP server");
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
	void skillsMatchTheFetchedTree() throws Exception {
		Path project = generateProject(createProjectRequest(SupportedBootVersion.V4_1, "vaadin")).getProjectDirectory();
		try (Stream<Path> dirs = Files.list(project.resolve(".claude/skills"))) {
			assertThat(dirs.filter(Files::isDirectory).map((dir) -> dir.getFileName().toString()))
				.as("skills fetched from vaadin/agent-skills; update SKILLS, AGENTS.md and CLAUDE.md")
				.containsExactlyInAnyOrderElementsOf(SKILLS);
		}
		String agents = contentOf(project.resolve("AGENTS.md").toFile());
		String claude = contentOf(project.resolve("CLAUDE.md").toFile());
		for (String skill : SKILLS) {
			assertThat(agents).contains("`" + skill + "`");
			assertThat(claude).contains("`" + skill + "`");
		}
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
