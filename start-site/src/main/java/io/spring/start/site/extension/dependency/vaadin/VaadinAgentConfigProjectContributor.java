/*
 * Copyright 2012 - present the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
 * Codex) to generated Vaadin projects. It configures the public Vaadin MCP server,
 * per-agent instruction files, and Vaadin's official agent skills ({@code aura-theme},
 * {@code vaadin-frontend-design} and {@code vaadin-form-layout}, vendored from
 * {@code vaadin/agent-skills} 0.2.1) so the project is ready to use with AI coding agents
 * without manual setup.
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
