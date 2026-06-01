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
