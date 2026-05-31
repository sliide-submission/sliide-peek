# User Activity Manager - Application Requirements

## 1. Overview

Build a simple **User Activity Manager** application using **Kotlin Multiplatform** and **Compose Multiplatform**.

The app will use the public GoREST API as its backend:

```text
https://gorest.co.in/public/v2
```

The purpose of the app is to demonstrate API integration, user-based interactions, loading/error handling, clean UI structure, dark mode support, and polished animations such as shimmer loading states.

This document is a provisional specification intended to provide foundation work until the final company-provided specification is available.

---

## 2. Target Technology

The application should be built using:

- Kotlin Multiplatform
- Compose Multiplatform
- Kotlin coroutines
- Ktor Client for networking
- kotlinx.serialization for JSON parsing
- MVVM or similar presentation architecture
- Material Design / Compose Material theming

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

Main resources:

```text
/users
/posts
/comments
/todos
```

Authenticated write operations require:

```http
Authorization: Bearer <token>
```

The app should be structured so that read-only API calls work without authentication, while create/update/delete actions can be enabled once an API token is provided.

---

## 4. Functional Requirements

## 4.1 User List

The app shall display a list of users from the API.

Endpoint:

```http
GET /users
```

Each user row should display:

- Name
- Email
- Gender
- Status: active/inactive

The user list should support:

- Loading state
- Error state
- Empty state
- Pull-to-refresh or manual refresh
- Pagination or “load more” behaviour
- Local filtering by active/inactive status, if time allows

---

## 4.2 User Detail

When a user is selected, the app shall display a user detail screen.

Suggested endpoints:

```http
GET /users/{id}
GET /users/{id}/posts
GET /users/{id}/todos
```

The detail screen should display:

- Name
- Email
- Gender
- Status
- User posts
- User todos

The posts and todos sections may be displayed as separate sections, tabs, or navigable child screens.

---

## 4.3 Posts

The app shall allow the user to browse posts associated with a selected user.

Suggested endpoint:

```http
GET /users/{id}/posts
```

Each post should display:

- Title
- Body preview

Selecting a post should open a post detail screen.

Suggested endpoints:

```http
GET /posts/{id}
GET /posts/{id}/comments
```

---

## 4.4 Comments

The app shall display comments for a selected post.

Suggested endpoint:

```http
GET /posts/{post_id}/comments
```

Each comment should display:

- Commenter name
- Email
- Comment body

Optional authenticated feature:

```http
POST /posts/{post_id}/comments
```

---

## 4.5 Todos

The app shall display todos associated with a selected user.

Suggested endpoint:

```http
GET /users/{user_id}/todos
```

Each todo should display:

- Title
- Due date
- Status

Optional authenticated actions:

```http
POST /users/{user_id}/todos
PATCH /todos/{id}
DELETE /todos/{id}
```

---

## 4.6 Create User

If an API token is available, the app shall allow creating a user.

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
- Email is required and must be valid
- Gender must be `male` or `female`
- Status must be `active` or `inactive`

---

## 4.7 Edit User

If an API token is available, the app shall allow editing an existing user.

Endpoint:

```http
PATCH /users/{id}
```

Editable fields:

- Name
- Email
- Gender
- Status

The app should show success and error feedback after attempting to update a user.

---

## 4.8 Delete User

If an API token is available, the app shall allow deleting a user.

Endpoint:

```http
DELETE /users/{id}
```

The app must show a confirmation dialog before deletion.

After successful deletion, the app should return to the user list and refresh the data.

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

- User list on the left, selected user detail on the right.
- User detail as the parent view, with posts/todos/comments shown as sibling or child panels.
- Post list as a secondary panel, with selected post comments/details in a child panel where space allows.

This can be thought of as similar to UIKit-style master view controllers with sibling or child detail view controllers, implemented using Compose Multiplatform adaptive layout patterns.

On compact screens such as phones, the app may use normal stacked navigation:

```text
User List -> User Detail -> Posts -> Post Detail -> Comments
```

On tablet/iPad-sized screens, the app should prefer adaptive navigation:

```text
Master List | Detail Content
```

or, where practical:

```text
Master List | Secondary Content | Detail Content
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
    repository/
    model/
  domain/
  presentation/
    users/
    posts/
    todos/
  ui/
    components/
    theme/
```

Suggested layers:

- API service/client
- DTO models
- Repository
- ViewModel or state holder
- Compose screens

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

1. User List Screen
2. User Detail Screen
3. User Posts Screen
4. Post Detail / Comments Screen
5. User Todos Screen
6. Adaptive Tablet/iPad Master-Detail Layout

Optional screens:

6. Create User Screen
7. Edit User Screen
8. Settings / API Token Screen
9. Theme Settings Screen

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

If time allows, consider adding:

- Local user search
- Filter users by active/inactive
- Offline cache using SQLDelight
- Retry button on failed requests
- API token entry/settings screen
- Pull-to-refresh
- Unit tests for repositories and view models
- Mock API layer for previews/testing
- Compose previews for light and dark themes

---

## 11. Reference KMP Practices From Existing Production Repository

A review of the existing `sainsburys-app-kmp` repository identified several production KMP practices that are useful for this smaller application. The project should borrow the lightweight parts of these practices without copying the full production SDK complexity.

Useful practices to adopt:

- **Ktor + kotlinx.serialization** for networking.
- **Internal DTOs** with `DTO` suffix, for example `UserDTO`, `PostDTO`, `TodoDTO`.
- **Mapper functions** named `toDomain()` in `Mapper.kt` files.
- **Domain models separated from API DTOs**.
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
- Complex kotlin-inject wiring may be overkill; a simpler DI approach or manual dependency creation is acceptable unless the final spec asks otherwise.
- SQLDelight should be treated as optional. It is useful for offline caching, but not required for the first version.
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
    repository/
  domain/
    model/
    repository/
    usecase/
  presentation/
    users/
    posts/
    todos/
  ui/
    components/
    theme/
    navigation/
```

Recommended naming style:

```text
UserDTO
PostDTO
CommentDTO
TodoDTO
KtorGorestRemoteDataSource
UserRepository
GetUsersUseCase
UserListViewModel
UserListViewModel.State
UserListViewModel.Event
```

---

## 12. Suggested Development Roadmap

The items below are intended as roadmap themes rather than a rigid implementation plan. Each roadmap item should be planned separately using the local `implementation-plan` skill before coding begins.

The planning agent should be free to adjust file structure, ordering, and implementation details as long as the final result remains simple, clean, and aligned with the requirements.

---

## 12.1 Project Foundation

Goal: create a minimal Kotlin Multiplatform Compose application foundation.

Likely scope:

- Android and iOS targets
- shared Compose UI entry point
- basic app shell
- Gradle/version catalog setup
- minimal package structure
- initial README

Suggested plan file:

```text
.plans/project-foundation.md
```

---

## 12.2 Theme, Navigation, and App Shell

Goal: establish the basic user experience framework before feature screens are built.

Likely scope:

- light/dark theme
- basic navigation model
- phone-safe layout shell
- iPad/tablet adaptive layout strategy
- reusable loading/error/empty containers

Suggested plan file:

```text
.plans/theme-navigation-shell.md
```

---

## 12.3 GoREST API and Domain Layer

Goal: create a small, testable data layer for users, posts, comments, and todos.

Likely scope:

- Ktor client
- kotlinx.serialization DTOs
- domain models
- `toDomain()` mappers
- repositories or data sources
- basic error handling
- fixture-based mapper/API tests where practical

Suggested plan file:

```text
.plans/gorest-api-domain.md
```

---

## 12.4 User List and User Detail Experience

Goal: implement the primary user browsing experience.

Likely scope:

- user list screen
- user detail screen
- loading shimmer
- refresh/retry behaviour
- empty/error states
- ViewModel/state holder integration

Suggested plan file:

```text
.plans/user-list-detail.md
```

---

## 12.5 Posts, Comments, and Todos

Goal: add the supporting user activity views.

Likely scope:

- user posts section/list
- post detail
- comments list
- user todos section/list
- status chips and lightweight content animations

Suggested plan file:

```text
.plans/activity-posts-comments-todos.md
```

---

## 12.6 iPad / Tablet Adaptive Layout

Goal: ensure the app behaves well on iPad/tablet rather than stretching phone screens.

Likely scope:

- master-detail layout
- selected item highlighting
- split-view behaviour
- portrait/landscape handling
- phone/tablet layout switching

Suggested plan file:

```text
.plans/ipad-tablet-layout.md
```

---

## 12.7 Polish, Animation, and Accessibility Pass

Goal: make the app feel finished without adding unnecessary complexity.

Likely scope:

- shimmer polish
- subtle transitions
- dark mode review
- spacing/typography pass
- accessibility labels/content descriptions where appropriate
- touch target review

Suggested plan file:

```text
.plans/polish-animation-accessibility.md
```

---

## 12.8 AI Usage Documentation and Submission Readiness

Goal: document the AI-assisted development process clearly for reviewers.

Likely scope:

- root `README.md` AI usage section
- `docs/ai/README.md` development log
- exported/rendered Pi sessions where appropriate
- raw transcript preservation with secret review/redaction
- final verification notes

Suggested plan file:

```text
.plans/ai-documentation-submission.md
```

---

## 12.9 Optional Enhancements

These should only be planned after the core roadmap is stable or if the final company specification requires them.

Possible optional plans:

- create/edit/delete user flows
- API token/settings screen
- local search/filtering
- offline cache
- additional tests
- theme selector

This roadmap should provide useful groundwork while remaining flexible enough to adapt to the final PDF specification.
