# Repo Skills

This repository versions project-specific Codex skills under `.codex/skills/`. Personal or machine-specific skills belong in `~/.codex/skills/` and should not be documented here.

The same skills are exposed to Claude Code through symlinks under `.claude/skills/` pointing into `.codex/skills/`. Edit skill content only in `.codex/skills/`; do not replace the symlinks with copies.

## Available Skills

- `implement-tuindice-module`: Create or modify Kotlin Multiplatform modules in this `tuindice` app while respecting the current architecture, Gradle setup, Koin wiring, shared navigation, platform bindings, and smoke-test conventions. It also includes scaffolding and component templates for `ViewModel`, `Route`, navigation, use cases, repositories, and smoke tests. Use it when the task adds a module, changes module boundaries, updates module wiring, or needs detailed project architecture context before changing `base`, `persistence`, `maincore`, `app`, `iosApp`, or any feature module. File: `.codex/skills/implement-tuindice-module/SKILL.md`

## Usage Rules

- When a task clearly matches a repo skill, open its `SKILL.md` first and load only the referenced files you actually need.
- Keep repo-local skills focused on facts and workflows that are true for this repository.
- Do not put secrets, absolute local paths, usernames, or machine-specific setup in repo-local skills.
- If a convention is shared by the whole team and matters for this codebase, it belongs either in a repo skill or in this file.
- If a convention is personal, experimental, or reused across unrelated repositories, keep it in `~/.codex/skills/`.
