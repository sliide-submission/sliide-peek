---
name: coding-style
description: |-
  Apply when writing or reviewing Kotlin Multiplatform code for this small Compose app. Emphasises clarity, naming consistency, and avoiding over-engineering.
---

## When to Use This Skill

Use this skill for all code changes, especially when deciding:

- naming
- package/file structure
- how much abstraction to add
- whether code belongs in data/domain/presentation/UI
- whether something is becoming over-engineered

## Goal

Keep the codebase clean, boring, readable, and easy to review.

## General Rules

- Prefer simple Kotlin over clever abstractions.
- Keep common/shared code platform-neutral.
- Use `expect/actual` only when necessary.
- Prefer `suspend` functions and `Flow` for async/reactive work.
- Avoid adding frameworks unless they solve an immediate problem.
- Do not copy production-SDK complexity into this small app.

## Naming

| Type | Pattern | Example |
|---|---|---|
| DTO | `NameDTO` | `UserDTO` |
| Mapper | `toDomain()` | `UserDTO.toDomain()` |
| Repository interface | `NameRepository` | `UserRepository` |
| Repository impl | `NameRepositoryImpl` | `UserRepositoryImpl` |
| Use case | `VerbNameUseCase` | `GetUsersUseCase` |
| ViewModel | `ScreenViewModel` | `UserListViewModel` |
| UI state | `ScreenState` | `UserListState` |
| UI event | `ScreenEvent` | `UserListEvent` |

## File Organisation

Prefer a small layered structure:

```text
shared/data
shared/domain
shared/presentation
shared/ui
```

Only split into more modules/packages when it improves clarity.

## Method References

Prefer method references when they are clearer:

```kotlin
users.map(UserDTO::toDomain)
```

Use lambdas when extra logic is needed:

```kotlin
users.map { dto ->
    dto.toDomain().copy(isSelected = dto.id == selectedId)
}
```

## Sealed Types

Use `sealed interface` for simple result, state variant, route, and event hierarchies.

Use `data object` for singleton cases where available.

## Comments

- Avoid comments that repeat the code.
- Add short comments for non-obvious decisions.
- Prefer readable names over explanatory comments.

## Checklist

Before finishing a change, check:

1. Are names obvious?
2. Is the code in the right layer?
3. Is shared code platform-neutral?
4. Is any abstraction justified by current needs?
5. Could a reviewer understand this quickly?
6. Is there less code than the previous solution, or a good reason for more?

## Output Expected

Code should look:

- small
- direct
- consistent
- idiomatic Kotlin
- easy to navigate

## Common Mistakes

Avoid:

- production-SDK architecture for a small app
- needless interfaces for every class
- premature generic abstractions
- platform-specific code in common code
- clever names or clever control flow

## Key Rules

- Minimal is good.
- Consistency matters.
- Keep UI, domain, and data responsibilities separate.
- Avoid premature abstraction.
- Optimize for a reviewer understanding the project quickly.
