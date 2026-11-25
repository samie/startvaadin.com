# AGENTS.md

Guidance for OpenAI Codex when working on this generated Vaadin project.

- Use the available Vaadin skills under `.agents/skills`: `aura-theme`,
  `vaadin-frontend-design` and `vaadin-form-layout` when designing UIs, theming, or
  building forms; `vaadin-browserless-testing` when writing UI tests;
  `vaadin-usage-review` to check new Vaadin code for workarounds where Vaadin
  already has an API.
- Use the `vaadin` MCP server for Vaadin documentation, APIs, components, styling,
  and version information instead of guessing about Vaadin APIs or framework
  behavior.
- Use the `project_tools` MCP server when available for information specific to
  this generated application.
- The theme (Aura, following the system's light or dark mode) is set in `AppShell`;
  the app's own CSS goes in `src/main/resources/META-INF/resources/styles.css`. Style
  with Aura and Vaadin style properties rather than fixed colours, so both colour
  schemes keep working.
- UI tests are browserless (`com.vaadin:browserless-test-spring`): they run in the
  JVM with the normal test task, without a browser or a running server.
- Optional, Maven only, preview: Vaadin's dev loop CLI makes edits live and reports
  whether they took effect. Install it with `./mvnw vaadin:install-dev-cli`, then use
  `.vaadin/vaadin-dev start` and `.vaadin/vaadin-dev apply`; don't run the app with
  `./mvnw` at the same time.
- Follow the architecture and coding conventions documented in this repository.
- Run the project's test suite before completing any code changes.
