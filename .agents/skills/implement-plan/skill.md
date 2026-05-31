---
name: implement-plan
description: Implement an existing `.plans/<short-slug>.md` plan file. Use when the user asks to execute a plan, hand a plan to an agent, or implement planned work without re-planning from scratch.
---

# implement-plan skill

## When to Use This Skill

Use this skill when the user asks to:

- implement a specific plan file
- execute planned work
- hand off implementation to an agent
- continue work from `.plans/<short-slug>.md`

If no plan file exists, ask the user whether to create one first using the `implementation-plan` skill. Do not improvise a large implementation plan inline unless explicitly asked.

## Audience

Implementation should be readable by a mid-level engineer and easy for another agent or human reviewer to inspect.

Prefer simple, direct code over clever abstractions. This project should remain a clean, minimalist KMP Compose app.

## Input

A plan file at:

```text
.plans/<short-slug>.md
```

The plan is the contract. Follow it unless it is clearly wrong or conflicts with project instructions.

## Pre-flight

Before touching code, read in order:

1. The plan file.
2. `AGENTS.md`.
3. `userapplicationspecs.md`.
4. Any relevant local skill files under `.agents/skills/`.
5. Existing files named in the plan.

Local project instructions outrank generic coding preferences. If a local skill conflicts with the plan, surface the conflict rather than silently choosing one.

## Execution Discipline

Work through the plan's **Implementation order** in order.

Do not ask for permission for file edits that the plan already authorises.

Stop only when one of these is true:

- A step is marked **[Human]**.
- Required access, token, credential, or external account is missing.
- The plan is structurally wrong.
- Verification fails after two reasonable fix attempts.
- The user asks you to stop.

## Human Handoffs

If a step is marked **[Human]**:

1. Stop at that step.
2. Tell the user exactly which step is waiting.
3. Explain why it must be done by a human.
4. Provide concrete instructions, commands, URLs, values, or expected outcome where possible.
5. Resume from the next step only after the user confirms completion.

Do not skip ahead unless the plan explicitly says later steps can proceed independently.

## Plan Deviation

Allowed without stopping:

- file moved slightly
- function/class name differs but intent is the same
- small implementation detail changes
- using a simpler approach that still satisfies the plan

Note these in the final report.

Stop and ask if:

- different architecture is needed
- different files/layers are required
- the implementation order is wrong
- the plan would violate project instructions
- the change expands beyond the planned scope

## Gardener Observations

While implementing, notice issues outside the plan such as:

- dead code
- duplication
- confusing names
- stale TODOs
- obvious missing tests

Fix only if the issue is trivial and on code already being changed.

Otherwise, list observations in the final report. Do not expand scope silently.

## Guardrails

Never:

- commit secrets, tokens, `.env` files, or credentials
- bypass hooks/checks with `--no-verify` unless explicitly authorised
- force-push
- delete branches or destructive files without explicit instruction
- add heavy frameworks not called for by the plan/spec
- modify CI/infrastructure unless the plan says to
- open a PR or push remote branches without explicit user instruction

If unsure whether something is destructive, ask first.

## Branch and Commit Discipline

Do not create branches, commit, push, or open PRs unless the user or plan asks for it.

If committing is requested:

- Use logical commits that match the feature or roadmap item being implemented.
- Prefer clear imperative commit messages.
- Keep commits reviewable and truthful to the change.
- Do not commit secrets, tokens, `.env` files, credentials, or unreviewed private transcripts.

Suggested commit message examples:

```text
Add project foundation
Add GoREST API client
Implement user list experience
Add iPad master-detail layout
Document AI-assisted workflow
```

## Verification

Run the plan's Verification section exactly where practical.

If verification fails:

1. Attempt a reasonable fix.
2. Re-run verification.
3. Attempt one more fix if the issue is clearly related.
4. If it still fails, stop and report the failure with evidence.

Do not claim verification passed unless it was actually run.

## Done Criteria

The task is done when:

- all agent-doable plan steps are complete
- all `[Human]` steps are either completed by the user or clearly handed off
- verification has been run or an explicit blocker is reported
- deviations are documented
- relevant AI/session documentation is updated if the plan required it

## Feature Closeout

Before reporting done:

1. Run the plan's verification steps.
2. Check `git status`.
3. Summarise files changed.
4. Note any deviations from the plan.
5. Update AI documentation if the plan required it.
6. Suggest a commit message if committing was not requested.

## Final Report

At the end, report concisely:

- whether it was implemented as planned or with deviations
- files changed
- verification commands and results
- any blocked/human steps
- any gardener observations

Example:

```markdown
Implemented with minor deviations: used a simpler repository constructor than planned.

Files changed:
- shared/data/remote/GorestApiClient.kt
- shared/presentation/users/UserListViewModel.kt

Verification:
- ./gradlew allTests — passed

Observations:
- Todo filtering could be split into a helper if it grows.
```
