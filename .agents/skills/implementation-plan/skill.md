---
name: implementation-plan
description: Create a concrete implementation plan for a feature or task and write it to `.plans/<short-slug>.md`. Use when the user asks to plan a feature, split work for agents, or prepare implementation steps before coding.
---

# implementation-plan skill

## When to Use This Skill

Use this skill when the user asks for:

- an implementation plan
- a feature breakdown
- steps to hand off to another agent
- a plan before coding starts
- investigation of how to implement part of the app

Do not use this skill for tiny one-line changes unless the user explicitly asks for a plan.

## Audience

Write the plan for a mid-level engineer or implementation agent.

A mid-level engineer should be able to follow it without needing extra context. If a step requires specialist knowledge, external accounts, secrets, app-store access, or manual UI interaction, flag it explicitly.

Example:

```markdown
> **Requires human/API-token access.** This step needs the GoREST bearer token and should not be attempted by an agent without the token being provided securely.
```

## Output

Write one markdown file at:

```text
.plans/<short-slug>.md
```

The slug should be short, kebab-case, and describe the change.

Examples:

```text
.plans/project-setup.md
.plans/gorest-api-client.md
.plans/user-list-shimmer.md
.plans/ipad-master-detail.md
```

Plan files are local project artefacts. They are not product specs; the spec says **what**, the plan says **how**.

## Required Sections

Each plan must include:

1. **Goal** — one or two sentences.
2. **Codebase findings** — real file paths and line numbers from inspection when code exists. If the project has not been created yet, say so clearly.
3. **Implementation order** — numbered steps; each step names the files it expects to touch/create.
4. **Verification** — commands/checks/manual checks that prove the work is done.
5. **Risks** — blockers, unknowns, external dependencies, or scope concerns. Omit only if genuinely none.

Optional sections:

- Approach summary
- Decisions to lock in first
- Agent handoff notes
- Suggested commit grouping

## Human vs Agent Steps

Most implementation steps should be agent-doable file edits.

Tag a step header with **[Human]** only when it requires:

- secrets or API tokens
- external account access
- browser login
- app-store/developer portal actions
- pushing branches/opening PRs, unless explicitly authorised
- decisions that cannot be inferred from the spec

If the plan contains any `[Human]` step, add this note above the `Implementation order` section:

```markdown
> Steps that require personal access, secrets, external systems, or explicit approval are tagged **[Human]**. Untagged steps are pure implementation work an agent can do.
```

Do not introduce other ownership tags unless the user asks for them.

## Workflow

1. Read the current requirements/spec files first, especially:
   - `PROJECT_SPEC.md`
   - `AGENTS.md`
   - relevant files under `.agents/skills/`
2. Inspect the existing codebase before drafting. Use real paths and line numbers where possible.
3. Resolve material ambiguity before writing the plan. Ask the user if the decision affects architecture, platform scope, authentication, or deliverables.
4. Draft the plan using the required sections.
5. Write the plan to `.plans/<short-slug>.md`.
6. Tell the user the path of the written plan. Do not paste the full plan unless asked.

## Planning Quality Bar

- Findings cite real files, not guesses.
- Steps are concrete enough for another agent to implement.
- Steps are ordered to reduce rework.
- The plan favours a clean, minimalist KMP Compose app.
- The plan avoids production-SDK over-engineering.
- Verification is practical and actually runnable.
- Human-only work is clearly marked.

## Checklist

Before writing the plan, confirm:

1. Have the relevant requirements been read?
2. Have the relevant local skills been considered?
3. Has the current codebase been inspected?
4. Are implementation steps concrete and file-oriented?
5. Are human-only steps marked?
6. Is the plan small enough to hand to an implementation agent?

## Common Mistakes

Avoid:

- writing vague steps like “implement API layer” without file targets
- inventing architecture not supported by the spec
- planning a huge enterprise-style module structure
- hiding decisions that need user confirmation
- skipping verification
- creating a plan that depends on secrets without marking `[Human]`
