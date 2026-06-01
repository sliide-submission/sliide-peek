# Agent Instructions

This project uses lightweight local skills for implementation guidance. They are intentionally minimal and aimed at building a clean Kotlin Multiplatform Compose app, not a large production SDK.

Before making changes, review the relevant skill:

- General style: `.agents/skills/coding-style/skill.md`
- Networking/API work: `.agents/skills/networking-ktor/skill.md`
- Compose UI, dark mode, shimmer, navigation, iPad/tablet layout: `.agents/skills/compose-ui/skill.md`
- ViewModel/state work: `.agents/skills/viewmodel-state/skill.md`
- Tests: `.agents/skills/testing/skill.md`
- Dependencies/Gradle: `.agents/skills/dependency-management/skill.md`
- Creating implementation plans: `.agents/skills/implementation-plan/skill.md`
- Implementing existing plans: `.agents/skills/implement-plan/skill.md`

## Project Direction

The official Sliide KMP "UX Innovator" challenge supersedes the earlier speculative app scope. From roadmap item 12.4 onward, prioritise:

- smart user feed from `/users`
- relative timestamp in shared logic
- add user flow
- delete with confirmation and Undo
- offline caching
- Koin-based DI where practical
- high-fidelity Material 3 polish
- AI usage documentation

Keep the app:

- simple
- clean
- minimalist
- easy to review
- KMP-first
- Compose Multiplatform-based
- suitable for Android, iPhone, and iPad

Avoid unnecessary production-SDK complexity, excessive modularisation, and frameworks that are not required by the final assignment spec.
