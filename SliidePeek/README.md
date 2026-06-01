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

- the feed discovers GoREST pagination and loads users from the last `/users` page;
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

Full add-user, delete/undo, durable offline cache, and high-fidelity motion/visual polish remain later work.
