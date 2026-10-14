# Vendored Vaadin agent skills

The contents of the `skills/` directory are vendored verbatim from Vaadin's official
agent-skills project and copied into every generated Vaadin project (under both
`.agents/skills/` and `.claude/skills/`).

- Source: https://github.com/vaadin/agent-skills
- Plugin: `vaadin-skills`
- Version: 0.2.1
- Branch/commit: `main` @ `21caf0ac474ecf3bc0b44ac1027d651b4e95b7d6`
- License: Apache-2.0

Skills vendored: `aura-theme`, `vaadin-frontend-design`, `vaadin-form-layout`.

## Updating

To refresh to a newer release, re-copy the `skills/**` tree from the upstream repo at
the desired tag and update the version/commit above. Keep the layout identical
(`skills/<name>/SKILL.md` + `references/...`); `VaadinAgentConfigProjectContributor`
copies the whole tree, so no code change is needed when files are added or removed.

This file is a sibling of `skills/` and is deliberately **not** copied into generated
projects.
