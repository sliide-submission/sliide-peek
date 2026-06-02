# User Activity Manager - Application Requirements

## 1. Overview

Build a high-fidelity **User Management System** using **Kotlin Multiplatform** and **Compose Multiplatform**.

The app will primarily use the public GoREST API as its backend:

```text
https://gorest.co.in/public/v2
```

If GoREST is unavailable, the official challenge allows using an alternative such as:

```text
https://dummyjson.com/docs/users
```

The purpose of the app is to demonstrate AI-assisted development, clean KMP architecture, shared Compose UI, UX polish, offline-capable user management, loading/error handling, dark mode, adaptive layouts, shimmer states, and high-quality implementation within a short timeframe.

This document has been updated to reflect the official Sliide KMP "UX Innovator" challenge. The official challenge supersedes earlier speculative requirements.

---

## 2. Target Technology

The application should be built using:

- Kotlin Multiplatform
- Compose Multiplatform
- Kotlin coroutines
- Ktor Client for networking
- kotlinx.serialization for JSON parsing
- SQLDelight or Room KMP for local caching/offline support
- Koin for dependency injection, preferred by the official challenge
- MVVM or MVI presentation architecture
- Material 3 / Compose Material theming

Target platforms:

- Android
- iOS

The iOS implementation should support both iPhone and iPad form factors.

Optional future target:

- Desktop

---

## 3. API Service

Base URL:

```text
https://gorest.co.in/public/v2
```

Primary resource:

```text
/users
```

Core endpoints:

```http
GET /users
POST /users
DELETE /users/{id}
```

Authenticated write operations require:

```http
Authorization: Bearer <token>
```

The app should be structured so read-only operations can function without authentication, while create/delete actions use a securely supplied bearer token or a clearly documented challenge/demo strategy.

Previous exploratory resources such as `/posts`, `/comments`, and `/todos` are no longer core requirements. They may remain in lower-level data code if already implemented, but they should not drive the primary UX unless time allows as optional extras.

---

## 4. Functional Requirements

## 4.1 Smart User Feed

The app shall fetch and display the **latest** users from the `/users` endpoint — i.e. the **first page (`page=1`)**.

Endpoint:

```http
GET /users?page=1
```

> **Spec correction:** the original brief said "last page". On GoREST `/users` is returned newest-first, so the last page holds the *oldest* records while page 1 holds the *newest*. For a feed that surfaces the latest users (and shows newly-created users immediately), the app fetches **page 1** rather than the last page. No pagination discovery is required.

Each user row must display:

- Name
- Email
- Relative timestamp, for example `5 minutes ago`

The relative timestamp must be calculated in shared KMP logic.

Important note: GoREST user records may not include a server-created timestamp. If no suitable API timestamp exists, the app should use a clearly documented local timestamp strategy, such as `fetchedAt`, `cachedAt`, or `createdLocallyAt`, and calculate the relative display from that shared model.

The user feed must support:

- Shimmer loading state
- Graceful error state
- No-internet/offline state
- Empty state where applicable
- Pull-to-refresh or explicit refresh where practical
- Offline display from local cache

---

## 4.2 Adaptive Add User Flow

The app shall provide a polished add-user flow launched from a floating action button.

Endpoint:

```http
POST /users
```

Form fields:

- Name
- Email
- Gender
- Status

Validation rules:

- Name is required
- Name should be validated in real time
- Email is required
- Email must be validated in real time
- Gender must be `male` or `female`
- Status must be `active` or `inactive`

On successful creation (`201`):

- the new user must appear immediately at the top of the list
- the UI should provide clear success feedback
- local cache/state should be updated so the app remains coherent offline

Authentication note:

- GoREST write operations require a bearer token.
- The implementation must handle the token safely and must not commit secrets.
- If token access is unavailable, the limitation and fallback/demo behaviour must be documented clearly.

---

## 4.3 Destructive Delete with Undo

The app shall support deleting users with a premium-feeling undo interaction.

Endpoint:

```http
DELETE /users/{id}
```

Required behaviour:

- Long-press a user to start deletion.
- Show a delete confirmation before calling the API.
- On successful deletion (`204`), animate the item disappearing from the list.
- Show a Snackbar with an **Undo** action.
- Undo restores the local UI/cache state before the action is finalized.

The exact remote undo semantics should be handled carefully because a completed API delete cannot necessarily be reversed on the server. At minimum, Undo must restore the local state in a coherent and clearly documented way. If the server item cannot be restored, document the trade-off.

---

## 4.4 Offline Support

The app shall work offline using local caching.

Requirements:

- Cache fetched users locally using SQLDelight or Room KMP.
- Show cached users when the network is unavailable.
- Show a clear no-internet/offline UI state.
- Keep cache updates coherent after successful add/delete actions.
- Avoid blocking the UI thread for persistence work.

---

## 4.5 Optional/Reduced Scope Features

The earlier exploratory scope included user posts, comments, todos, user detail, and edit-user flows. These are no longer core official-challenge requirements.

Optional only if time allows:

- User detail view
- Edit user
- Posts/comments/todos browsing
- Search/filtering
- Theme selector

---

## 5. UI Requirements

## 5.1 General UI

The app should have a clean, modern Compose UI.

It should include:

- Consistent spacing
- Reusable cards/list rows
- Clear typography hierarchy
- Clear navigation between screens
- Loading, error, and empty states
- Snackbar/toast-style feedback for user actions
- Responsive layouts where practical

The app should feel like a small production-quality application, not just a raw API demo.

---

## 5.2 Dark Mode

The app shall support both **light mode** and **dark mode**.

Requirements:

- Use Compose Material theming.
- Respect the system theme by default.
- Provide colours suitable for both light and dark backgrounds.
- Ensure text, icons, cards, dividers, and error states remain accessible in dark mode.
- Avoid hardcoded colours directly inside screens where possible.
- Use theme values such as:

```kotlin
MaterialTheme.colorScheme.background
MaterialTheme.colorScheme.surface
MaterialTheme.colorScheme.primary
MaterialTheme.colorScheme.error
```

Optional enhancement:

- Add a manual theme selector with:
  - System default
  - Light
  - Dark

---

## 5.3 Loading Shimmer

List-based screens should display a shimmer/skeleton loading state while network data is being fetched.

Apply shimmer loading to:

- User list rows
- Post list rows
- Todo list rows
- Comment list rows

Expected behaviour:

- Show grey placeholder cards while loading.
- Animate a highlight gradient across each placeholder.
- Replace shimmer content with real content when data is loaded.
- Avoid visible layout jumps between loading and loaded states.

---

## 5.4 Screen Transitions

Navigation between screens should include simple animations, such as:

- Fade in/out
- Slide in from right for detail screens
- Reverse slide when navigating back

Animations should be subtle and should not make the app feel slow.

---

## 5.5 iPad / Tablet Layout

The app shall support tablet-sized layouts, with particular attention to iPad.

On larger screens, the app should avoid simply stretching phone layouts. Instead, it should use an adaptive master-detail style interface.

Expected tablet behaviour:

- Use a split-view style layout where appropriate.
- Display a master list and detail content side by side.
- Keep navigation efficient on larger screens.
- Avoid unnecessary full-screen transitions when a detail panel can be updated in place.
- Support landscape and portrait orientations.
- Preserve readable line lengths and spacing on wide screens.

Example layouts:

- User feed on the left, selected/active user management panel on the right.
- User feed on the left, add-user form or delete confirmation/undo state on the right.
- Two-column/grid treatment for user cards on wide screens where it feels better than master-detail.

This can be thought of as similar to UIKit-style master-detail navigation, implemented using Compose Multiplatform adaptive layout patterns.

On compact screens such as phones, the app may use normal stacked or modal navigation:

```text
User Feed -> Add User Form / Delete Confirmation
```

On tablet/iPad-sized screens, the app should prefer adaptive navigation:

```text
User Feed | Detail/Form/Action Panel
```

or, where practical:

```text
Two-column User Feed + Contextual Action Surface
```

---

## 5.6 Content Animations

The app should use lightweight animations for UI state changes.

Examples:

- Animate list item appearance
- Animate empty/error/loading state changes
- Animate section expand/collapse if sections are collapsible
- Animate status chip colour changes
- Animate item placement in lists where supported

Suggested Compose APIs:

```kotlin
AnimatedVisibility
Crossfade
animateContentSize
animateColorAsState
```

---

## 5.7 Platform-Specific UI Expectations

Although the UI is implemented with Compose Multiplatform, it should feel appropriate on each platform.

For iOS/iPadOS:

- Support iPhone and iPad layouts.
- Respect safe areas.
- Handle rotation gracefully.
- Use adaptive layouts for tablet widths.
- Ensure touch targets are comfortable.
- Avoid Android-only assumptions in UI behaviour.

For Android:

- Support phone layouts.
- Support larger Android tablet layouts where practical.
- Respect system navigation and edge insets.

---

## 6. Networking Requirements

The networking layer should:

- Use Ktor Client
- Use kotlinx.serialization
- Parse API responses into strongly typed models
- Handle HTTP errors gracefully
- Handle network failures gracefully
- Avoid blocking the UI thread
- Be testable via repository interfaces or mock clients

Common error cases to handle:

- No internet connection
- API timeout
- HTTP 401/403 for unauthorized writes
- HTTP 404 for missing resources
- HTTP 422 for validation failures
- Unexpected server errors

---

## 7. Architecture Requirements

A simple layered architecture should be used.

Suggested structure:

```text
shared/
  data/
    remote/
    local/
    repository/
  domain/
    model/
    repository/
    usecase/
  presentation/
    users/
    adduser/
  ui/
    components/
    theme/
    navigation/
```

Suggested layers:

- API service/client
- Local cache/persistence
- DTO models
- Domain models
- Repository/use case layer
- ViewModel or state holder
- Compose screens
- DI wiring via Koin or a clearly justified lightweight alternative

Example UI state:

```kotlin
data class UsersUiState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val errorMessage: String? = null
)
```

---

## 8. Suggested Screens

Minimum implementation:

1. Smart User Feed Screen
2. Add User Form / Modal / Panel
3. Delete Confirmation
4. Snackbar Undo flow
5. Offline / No Internet State
6. Adaptive Tablet/iPad Layout

Optional screens:

7. User Detail Screen
8. Edit User Screen
9. Settings / API Token Screen, if needed for safe local token entry
10. Theme Settings Screen
11. Posts/comments/todos screens, only if time allows

---

## 9. AI-Assisted Development Documentation Requirements

The company has explicitly stated that AI should be used during development and that the submission should demonstrate how AI was used. The project should therefore include clear AI usage documentation as part of the repository.

The `README.md` should include an **AI-Assisted Development** section containing:

- A short statement that the application was built using AI-assisted development.
- The AI tools used, for example:
  - Pi coding agent
  - OpenAI Codex / GPT model used through Pi
  - Any other AI tools used during implementation
- The main development sessions used to build the app.
- The skills, prompts, or workflows used.
- Which parts were generated, reviewed, edited, or manually adjusted.
- Any important limitations, corrections, or decisions made by the developer.

The aim is to show responsible AI usage rather than pretending the work was entirely hand-written.

---

## 9.1 AI Session Evidence

The project should preserve both readable and raw AI session evidence.

Recommended repository structure:

```text
docs/
  ai/
    README.md
    sessions/
      raw/
      rendered/
    prompts/
    summaries/
```

Suggested contents:

```text
docs/ai/README.md
```

Should provide a readable index of AI usage:

- Session name
- Date/time
- Tool used
- Model used
- Purpose of the session
- Features/files affected
- Link to raw transcript
- Link to rendered transcript, if available
- Notes on human review or manual changes

Example table:

```markdown
| Date | Tool | Model | Session | Purpose | Output |
|---|---|---|---|---|---|
| 2026-05-31 | Pi | openai-codex/gpt-5.5 | app-foundation | Created KMP project structure | docs/ai/sessions/rendered/app-foundation.html |
```

Raw sessions should be kept where practical. For Pi, sessions are saved as JSONL files under:

```text
~/.pi/agent/sessions/
```

For project-specific evidence, copy relevant session JSONL files into:

```text
docs/ai/sessions/raw/
```

Care should be taken not to commit secrets, API tokens, personal credentials, or unrelated private conversations.

---

## 9.2 Rendered AI Transcripts

Readable transcripts should be provided where possible.

Pi supports session export from interactive mode:

```text
/export [file]
```

It also supports sharing/export-related workflows such as:

```text
/share
pi --export <session> [out]
```

Rendered exports can be stored in:

```text
docs/ai/sessions/rendered/
```

The submission may also include raw JSONL transcripts for transparency.

---

## 9.3 Simon Willison Transcript Tooling Reference

Simon Willison's tools site includes tooling and examples for presenting AI-assisted development sessions in a readable way:

```text
https://tools.simonwillison.net
```

Relevant examples/tools identified:

- `claude-code-timeline`
  - Displays Claude Code JSONL sessions as a browsable timeline.
  - Useful as inspiration for presenting coding-agent session history.
- `json-to-markdown-transcript`
  - Converts JSON-style chat transcripts into readable Markdown.
  - Useful as inspiration for creating human-readable transcript files.
- The tools site colophon:
  - Shows a practical example of documenting AI-assisted development history with commit messages and links to transcripts.

Although these tools are Claude-oriented, the same idea should be applied to Pi sessions: preserve raw session files and provide a readable rendered/indexed view in the repository.

---

## 9.4 AI Workflow Expectations

The development process should intentionally use AI for most implementation work.

The developer should still be responsible for:

- Reviewing generated code
- Running the app
- Running tests/checks
- Fixing incorrect assumptions
- Verifying platform behaviour on Android and iOS
- Ensuring no secrets are committed
- Ensuring the final solution matches the company specification

Suggested AI workflow:

1. Start named Pi sessions for each major feature area.
2. Ask the AI to implement one feature or layer at a time.
3. Review diffs before committing.
4. Run tests/builds and ask AI to help fix failures.
5. Export or copy the relevant Pi session transcript.
6. Add a short human-readable summary to `docs/ai/README.md`.
7. Reference the most important sessions from the root `README.md`.

Example named sessions:

```text
pi --name "project-setup-kmp-compose"
pi --name "gorest-api-client"
pi --name "user-list-shimmer-ui"
pi --name "ipad-master-detail-layout"
pi --name "ios-compose-target"
pi --name "tests-and-polish"
```

---

## 10. Nice-To-Have Features

If time allows after the official core requirements are complete, consider adding:

- Local user search
- Filter users by active/inactive
- User detail view
- Edit user flow
- API token entry/settings screen
- Pull-to-refresh polish
- Additional ViewModel edge-case tests
- Mock API layer for previews/testing
- Compose previews for light and dark themes
- Posts/comments/todos browsing as a non-core extra

---

## 11. Reference KMP Practices From Existing Production Repository

A review of the existing `sainsburys-app-kmp` repository identified several production KMP practices that are useful for this smaller application. The project should borrow the lightweight parts of these practices without copying the full production SDK complexity.

Useful practices to adopt:

- **Ktor + kotlinx.serialization** for networking.
- **SQLDelight or Room KMP** for offline caching.
- **Koin** for dependency injection, unless a simpler approach is explicitly justified.
- **Internal DTOs** with `DTO` suffix, for example `UserDTO`.
- **Mapper functions** named `toDomain()` in `Mapper.kt` files.
- **Domain models separated from API DTOs and local database entities**.
- **Stateless composables** that receive state and callbacks.
- **ViewModel state as immutable `data class State` exposed via `StateFlow`**.
- **One-shot UI events as `sealed interface Event` exposed via `Flow` or `Channel`**.
- **No direct Android dependencies in shared ViewModels**.
- **Common code in `commonMain`; platform-specific code in `androidMain` / `iosMain` only when necessary**.
- **Fixture-based tests** using realistic JSON responses.
- **Ktor mock-client style tests** for API/data-source behaviour.
- **Use `kotlin.test` and `kotlinx.coroutines.test` for shared tests**.
- **Use Gradle version catalogs instead of hardcoded dependency versions**.
- **Prefer `implementation` dependencies unless a type must be exposed publicly**.
- **Use method references, e.g. `users.map(::toDomain)`, where they improve readability**.

Practices to adapt carefully or avoid for this smaller app:

- The Sainsbury's project is an SDK-style production codebase; this app is a small demo application, so it does not need the same level of hyper-modularisation.
- Binary compatibility validation is not required unless the app starts publishing libraries.
- A full SDK/public API separation is unnecessary.
- Full production-SDK DI complexity is still overkill, but the official challenge prefers Koin, so use a small Koin setup rather than a large custom framework.
- SQLDelight/Room KMP is now part of the official offline requirement, so local caching should no longer be treated as optional.
- The Sainsbury's “do not create docs” convention does not apply here, because this assignment explicitly benefits from README and AI-session documentation.
- Sainsbury's internal design-system components should not be copied; instead, create a lightweight app-specific theme/components layer.

Suggested simplified architecture inspired by the production project:

```text
shared/
  data/
    remote/
      dto/
      mapper/
      GorestApiClient.kt
    local/
      UserCacheDataSource.kt
    repository/
  domain/
    model/
    repository/
    usecase/
  presentation/
    users/
    adduser/
  ui/
    components/
    theme/
    navigation/
```

Recommended naming style:

```text
UserDTO
UserEntity
KtorGorestRemoteDataSource
UserCacheDataSource
UserRepository
GetLastPageUsersUseCase
CreateUserUseCase
DeleteUserUseCase
UserFeedViewModel
UserFeedViewModel.State
UserFeedViewModel.Event
```

---

## 12. Suggested Development Roadmap

The items below are roadmap themes rather than rigid implementation instructions. Each roadmap item should be planned separately using the local `implementation-plan` skill before coding begins.

Roadmap items 12.1-12.3 were started before the official challenge arrived and remain broadly useful. Roadmap item 12.4 onward should follow the official Sliide KMP "UX Innovator" challenge as the source of truth.

---

## 12.1 Project Foundation — Completed/Retained

Goal: create a minimal Kotlin Multiplatform Compose application foundation.

Likely scope:

- Android and iOS targets
- shared Compose UI entry point
- basic app shell
- Gradle/version catalog setup
- minimal package structure
- initial README

Plan file:

```text
.plans/project-foundation.md
```

---

## 12.2 Theme, Navigation, and App Shell — Completed/Retained

Goal: establish the structural user experience framework before feature screens are built.

Likely scope:

- light/dark theme foundation
- basic navigation model
- phone-safe layout shell
- iPad/tablet adaptive layout strategy
- reusable loading/error/empty containers

Plan file:

```text
.plans/theme-navigation-shell.md
```

Note: existing 12.2 work may need light adaptation to the official focus on user feed/add/delete rather than posts/comments/todos.

---

## 12.3 GoREST API and Domain Layer — Completed/Retained

Goal: create a small, testable data layer for GoREST.

Likely retained scope:

- Ktor client
- kotlinx.serialization DTOs
- domain models
- `toDomain()` mappers
- repositories or data sources
- basic error handling
- fixture-based mapper/API tests

Plan file:

```text
.plans/gorest-api-domain.md
```

Note: if posts/comments/todos support exists from early planning, it should be treated as non-core. New planning should prioritise `/users`, create-user, delete-user, offline caching, and shared user-feed logic.

---

## 12.4 Official Smart User Feed

Goal: implement the official primary user feed experience.

Likely scope:

- fetch the latest users from the first page (`page=1`) of `/users`
- display name, email, and shared-logic relative timestamp
- ViewModel/state holder integration
- shimmer loading
- no-internet/offline state
- refresh/retry behaviour
- local cached feed display if persistence is already available, or a clear seam if persistence is planned next
- unit tests for shared feed logic and ViewModel state

Suggested plan file:

```text
.plans/smart-user-feed.md
```

---

## 12.5 Offline Cache and Dependency Injection

Goal: meet the official offline and DI requirements.

Likely scope:

- SQLDelight or Room KMP local user cache
- cached last-page feed storage
- cached add/delete state updates
- Koin setup for API client, repositories, cache, and ViewModels/state holders
- offline-first repository behaviour where practical
- tests for cache/repository behaviour

Suggested plan file:

```text
.plans/offline-cache-koin.md
```

---

## 12.6 Adaptive Add User Flow

Goal: implement the official polished add-user flow.

Likely scope:

- FAB entry point
- adaptive form presentation for phone/tablet
- real-time name validation
- real-time email validation
- gender/status inputs
- authenticated `POST /users` path or documented challenge fallback
- immediate insertion at the top of the feed after `201`
- local cache/state update
- success/error feedback
- ViewModel/form validation tests

Suggested plan file:

```text
.plans/add-user-flow.md
```

---

## 12.7 Delete with Confirmation and Undo

Goal: implement the official destructive action flow with premium-feeling local undo.

Likely scope:

- long-press user gesture
- delete confirmation
- authenticated `DELETE /users/{id}` path or documented challenge fallback
- animated item removal after `204`
- Snackbar Undo
- local state/cache restoration when Undo is selected
- clear documentation of remote-vs-local undo semantics
- ViewModel/state tests for delete/undo edge cases

Suggested plan file:

```text
.plans/delete-user-undo.md
```

---

## 12.8 High-Fidelity UX Polish, Animation, and Accessibility

Goal: make the app feel production-ready and "expensive" as required by the official challenge.

Likely scope:

- apply high-fidelity design system/prototype
- Material 3 polish
- dark mode review
- shimmer polish
- add/delete animations
- adaptive layout polish for portrait/landscape/tablet
- accessibility labels/content descriptions where appropriate
- touch target review
- no-internet and validation-state polish

Suggested plan file:

```text
.plans/high-fidelity-polish.md
```

---

## 12.9 Post MVP Improvements

These should only be planned after the official core requirements are stable.

Candidate improvements:

- Pagination / infinite scroll for the user feed.
  - The app no longer treats the final GoREST page as the primary feed. It now starts from the latest users page.
  - Pagination should reveal additional/older users as the user scrolls.
  - Prefer infinite scroll over a manual load-more button unless implementation complexity argues otherwise.
- View user: push a read-only user detail view from the users list on mobile devices.
  - Keep this read-only; the official challenge does not require editing users.
  - On tablet/iPad, this may use the existing action/detail panel rather than a pushed route.
- Live connectivity monitor for online/offline state.
  - Treat this as device connectivity, not full GoREST API reachability.
  - API failures should still be handled separately by the existing repository/error-state flow.
- Additional UI and unit test pass.
  - Treat this as a separate quality task to review the app and identify useful additional shared unit tests and Compose/UI tests.

Removed/deprioritised improvements:

- Edit user flow — not required by the official challenge.
- API token/settings screen — token setup should remain documented in README/runtime environment configuration rather than becoming a user-facing screen.

Enhancements should be promoted to `.plans/<enhancement>.md` only when selected for implementation.

This roadmap should provide useful groundwork while prioritising the official challenge requirements.

---

## 12.10 AI Usage Documentation and Submission Readiness

Goal: document the AI-assisted development process clearly for reviewers.

Likely scope:

- root `README.md` architectural choices section
- root `README.md` AI usage section
- `docs/ai/README.md` development log
- exported/rendered Pi sessions where appropriate
- raw transcript preservation with secret review/redaction
- final verification notes
- public GitHub/ZIP submission readiness

Suggested plan file:

```text
.plans/ai-documentation-submission.md
```