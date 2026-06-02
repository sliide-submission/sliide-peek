# Sliide Peek KMP App

This is the generated Kotlin Multiplatform project for Sliide Peek.

- `androidApp/` contains the Android application.
- `iosApp/` contains the iOS application.
- `shared/` contains shared Kotlin and Compose Multiplatform UI.

The shared UI entry point is:

```text
shared/src/commonMain/kotlin/com/sliide/useractivity/App.kt
```

## Commands

```bash
./gradlew projects
./gradlew :shared:compileKotlinMetadata
./gradlew :androidApp:assembleDebug
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

The current foundation includes the roadmap 12.2 structural UX shell informed by `../design/ux_v1.html`:

- compact phone push/stack navigation;
- iPad/tablet two-pane master-detail shell;
- shared route model and lightweight navigator;
- reusable app top bar, section header, segmented filter, status chip, skeleton, empty, and error containers;
- neutral light/dark Material theme foundations.

The UI is intentionally low/mid-fi. Hi-fi visual polish, final design tokens, real GoREST data screens, and feature ViewModels will be added in later roadmap items.

The shared module also includes the roadmap 12.3 read-only GoREST data/domain layer:

- Ktor Client and kotlinx.serialization JSON setup;
- internal DTOs and explicit `toDomain()` mappers for users, posts, comments, and todos;
- domain models plus repository interfaces returning simple `AppResult` values;
- repository implementations with basic HTTP/network error mapping;
- fixture-based mapper, API-client, and repository tests.

Roadmap 12.4A adds the smart user feed foundation:

- the feed loads the latest users from the first `/users` page (`page=1`); GoREST returns newest-first, so page 1 surfaces the most recent users and newly-created ones immediately (the original "last page" brief is corrected in `userapplicationspecs.md` §4.1). When a GoREST token is configured locally, the app also sends it on feed reads so user records created under that token are visible after refresh;
- shared KMP relative timestamp logic formats a local `fetchedAt` timestamp because GoREST users do not expose a creation timestamp;
- `UserFeedViewModel` exposes loading, refresh, retry, empty, error, and no-internet/offline-ready state;
- the compact and tablet shells are wired to a minimal repository-backed feed UI with existing skeleton loading components;
- shared tests cover timestamp logic, last-page behaviour, and ViewModel state transitions.

Durable offline caching, Koin DI, add-user, delete/undo, and final high-fidelity feed styling are intentionally deferred to later roadmap items/design pass 12.4B+.

UX v2 alignment now keeps the smart feed focused on the re-scoped user-management experience:

- feed rows align to name, email, right-side status chip, and relative last active;
- phone has a visible FAB seam and tablet has a `+` app-bar seam for the future add-user flow;
- loading skeleton geometry mirrors feed rows more closely;
- API error, no-internet/no-cache, refreshing, and in-memory offline/cached states use distinct feed states/banners;
- tablet selection shows a lightweight user action-panel placeholder instead of the earlier UX v1 posts/todos drill-down.

Roadmap 12.7 adds destructive delete with local Undo:

- long-press a feed row (phone) or the tablet action-panel "Delete user" button opens a confirmation dialog;
- confirming optimistically removes the row and shows a Snackbar with **Undo**;
- the delete is **local-first**: Undo restores the row at its exact original index and never touches the network; the cache is only mutated on a confirmed remote success;
- the authenticated `DELETE /users/{id}` is committed only when the Undo window closes (the Snackbar is dismissed/times out);
- on commit failure — including when no GoREST token is configured (`Unauthorized`) — the row is re-inserted at its original index and an error Snackbar offers **Retry**; a `404` is treated as already-deleted;
- shared `UserFeedViewModel` tests cover confirm/undo/commit-success/commit-failure, original-index restore, not-found, and double-commit edge cases.

**Remote-vs-local undo semantics:** Undo is purely local and instant because it happens *before* any API call, so there is nothing to reverse on the server. Once the window closes and GoREST returns `204`, the delete is final and cannot be undone. If the app is closed during the undo window, the commit coroutine is cancelled and the user safely reappears on the next load (the cache still holds them). Remote deletes require the GoREST bearer token; without it, delete stays local and surfaces the "restored" Retry Snackbar — the same documented fallback used by add-user.

Full high-fidelity motion/visual polish (countdown bar, haptics, ripple) remains later work (12.8).

Roadmap 12.8 applies the high-fidelity **"Signal"** design system (see `../design/`):

- **Theme tokens** rewritten to the Signal palette — cool-grey surfaces, blue accent (`#2D5BFF`), green Active status, squared radii. Core tokens map onto the Material 3 `ColorScheme`; semantic extras (green, shimmer, soft accent, inverse) live in `SignalColors` / `MaterialTheme.signal`. Light + dark are both tuned per the handoff sheet.
- **Typography** uses bundled **Space Grotesk** (UI) + **JetBrains Mono** (emails, timestamps, IDs, counts) via `composeResources/font/` (OFL-1.1; licences in `shared/licenses/`).
- **Components** restyled to the hi-fi reference: gradient-sweep shimmer, green/neutral status chips, rounded-square mono avatars, accent FAB, icon-button app bar with count pill, offline/refreshing/error banners, M3 text-field validation states, segmented controls, destructive confirm dialog, inverse-surface undo snackbar.
- **Motion** tokens (`SignalMotion`) drive shimmer, staggered row insert, press-scale, refresh spin, and emphasized easing.
- **Accessibility:** content descriptions on FAB/refresh/add/back, long-press exposed as a row accessibility action ("Delete {name}") with a haptic tick, banners announced via `liveRegion`, colour always paired with a label.

**Decisions deliberately deferred to the design team (visual applied, behaviour unchanged):**

- **Undo window:** kept at the existing `SnackbarDuration.Long` with the restyled snackbar; the spec's 5s window + countdown bar was **not** adopted (would change tested commit timing).
- **Gender "Other":** dropped to match the GoREST API / existing model (Male/Female only).
- **Status = green, selection = blue:** implemented per the Signal tokens (previously both reused amber); flagged for design confirmation.
- **Space Grotesk SemiBold (600):** no static instance ships, so `body-strong` maps to Medium (500); titles stay Bold (700).
