---
name: dependency-management
description: |-
  Apply when adding, updating, or reviewing Gradle dependencies for the small Kotlin Multiplatform Compose app.
---

## When to Use This Skill

Use this skill when adding or modifying:

- Gradle build files
- version catalogs
- plugins
- new libraries
- dependency scopes
- KMP source-set dependencies

## Goal

Keep dependencies explicit, minimal, and easy to understand.

## Rules

- Use Gradle version catalogs where possible: `gradle/libs.versions.toml`.
- Do not hardcode dependency versions directly in build files.
- Check for existing dependencies before adding new ones.
- Prefer `implementation` unless a dependency type is intentionally exposed.
- Avoid adding large frameworks for small problems.

## Expected Core Dependencies

Likely useful dependencies:

- Kotlin Multiplatform
- Compose Multiplatform
- Compose Material3
- Ktor Client
- Ktor Content Negotiation
- kotlinx.serialization JSON
- kotlinx.coroutines
- kotlinx.datetime, if dates need formatting/parsing
- A lightweight KMP ViewModel/lifecycle solution, if needed

Official challenge dependencies now likely required:

- SQLDelight or Room KMP for offline caching
- Koin for dependency injection

Optional dependencies:

- Image loading library, only if the final UI needs remote images

## Dependency Access Style

Prefer:

```kotlin
implementation(libs.ktor.client.core)
implementation(libs.kotlinx.serialization.json)
implementation(compose.material3)
```

Avoid:

```kotlin
implementation("io.ktor:ktor-client-core:3.x.x")
```

## Scope Guidance

Use `implementation` by default.

Use `api` only when another module must compile against types from that dependency.

For a small app, excessive `api` usage is usually a smell.

## Avoid Unless Required

Do not add these unless the official spec or a plan clearly needs them:

- Retrofit
- Dagger/Hilt
- Large navigation frameworks
- Analytics SDKs

Note: an offline database stack is now expected by the official challenge. Prefer SQLDelight or Room KMP, and keep the setup as small as practical.

## Checklist

Before adding a dependency, check:

1. Is it actually needed for the current spec?
2. Can Compose/KMP/Ktor already do this?
3. Is there an existing dependency that covers it?
4. Is the version in `libs.versions.toml`?
5. Is `implementation` sufficient?
6. Is the added complexity worth it for a small app?

## Output Expected

Dependency changes should be:

- minimal
- version-catalog based
- easy to explain in review
- scoped correctly to the source set that needs them

## Common Mistakes

Avoid:

- adding libraries by habit
- hardcoding dependency strings and versions
- using `api` by default
- adding navigation frameworks before they are needed
- overbuilding Koin or persistence beyond the official offline requirement

## Key Rules

- Small dependency set.
- Version catalog only.
- Prefer built-in Compose/KMP capabilities first.
- Add libraries intentionally, not by habit.
