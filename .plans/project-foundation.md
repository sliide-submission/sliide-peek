# Project Foundation Implementation Plan

## Goal

Create a minimal Kotlin Multiplatform Compose foundation for the User Activity Manager app, targeting Android and iOS with a shared Compose UI entry point, basic app shell, Gradle/version-catalog setup, minimal package structure, and an initial README.

## Codebase findings

- `AGENTS.md:3` states the app should remain a clean Kotlin Multiplatform Compose app rather than a large production SDK.
- `AGENTS.md:18-28` requires the project to stay simple, minimalist, KMP-first, Compose Multiplatform-based, and suitable for Android, iPhone, and iPad.
- `PROJECT_SPEC.md:19-36` defines the target technology: Kotlin Multiplatform, Compose Multiplatform, coroutines, Ktor, kotlinx.serialization, MVVM-style architecture, Material theming, Android, and iOS with iPhone/iPad support.
- `PROJECT_SPEC.md:456-484` recommends a simple layered architecture with data, domain, presentation, and UI layers.
- `PROJECT_SPEC.md:700-730` recommends borrowing lightweight KMP practices, including common code in `commonMain`, platform-specific code only when necessary, version catalogs, and avoiding production SDK complexity.
- `PROJECT_SPEC.md:781-798` defines roadmap item 12.1 and the required plan path: `.plans/project-foundation.md`.
- `.agents/skills/coding-style/skill.md:21-28` prefers simple, platform-neutral Kotlin and avoiding unnecessary frameworks.
- `.agents/skills/coding-style/skill.md:43-54` recommends a small `shared/data`, `shared/domain`, `shared/presentation`, `shared/ui` style structure.
- `.agents/skills/dependency-management/skill.md:22-28` requires version catalogs and minimal dependencies without hardcoded versions.
- `.agents/skills/dependency-management/skill.md:30-42` identifies likely core dependencies, but the foundation should add only what is immediately needed for a compiling Compose app and defer Ktor/serialization until roadmap item 12.3 unless a template requires plugin wiring.
- `.agents/skills/compose-ui/skill.md:23-30` favours simple composables, state hoisting, theme values, and minimal reusable components.
- `.gitignore:1-4` currently only ignores local AI/auth files; it does not yet ignore Gradle, Android Studio, Kotlin, or Xcode build outputs.
- Current project inspection shows no Gradle files, Android app, iOS app, shared module, source sets, or README exist yet. Existing root files are limited to `AGENTS.md`, `Brewfile`, `designer-brief.md`, `PROJECT_SPEC.md`, and `.gitignore`.

## Approach summary

Use Android Studio's official Kotlin Multiplatform / Compose Multiplatform app template for the initial project generation instead of hand-writing brittle Gradle and Xcode scaffolding. Android Studio is better positioned to choose compatible Kotlin, Compose Multiplatform, AGP, Gradle, and iOS template wiring. After the template exists, an agent can safely do smaller, reviewable cleanup: align package names, add a minimal shared app shell, update `.gitignore`, create README content, and run verification. Avoid networking, feature screens, complex navigation, DI, persistence, and production-style modularisation in this roadmap item.

> Steps that require personal access, secrets, external systems, or explicit approval are tagged **[Human]**. Untagged steps are pure implementation work an agent can do.

## Implementation order

1. **[Human] Create the KMP Compose app using Android Studio**
   - Open Android Studio and create a new Kotlin Multiplatform / Compose Multiplatform application using the official IDE template.
   - Prefer the template option that creates Android and iOS targets/apps, not Android-only.
   - Use a simple app name such as `User Activity Manager` and package/application id such as `com.sliide.useractivity`.
   - Keep generated module names close to the template defaults unless Android Studio offers a clean `shared`, `androidApp`, and `iosApp` structure.
   - Let Android Studio generate the Gradle wrapper, Gradle files, shared module, Android entry point, and iOS project/framework wiring.
   - Sync Gradle in Android Studio and confirm the generated starter app builds or at least syncs before handing back to the agent.
   - If Android Studio MCP is available, it may be used to create/open/sync the project and inspect IDE errors, but the key value is using Android Studio's current template rather than hand-written scaffolding.

2. **Inspect and document the generated project shape**
   - Inspect generated files such as `settings.gradle.kts`, root `build.gradle.kts`, `gradle/libs.versions.toml`, `shared/build.gradle.kts`, Android module files, and iOS app/project files.
   - Record actual module names and source-set paths before editing, because the Android Studio template may not use exactly `shared`, `androidApp`, and `iosApp`.
   - Preserve the generated versions and plugin coordinates unless there is a clear build failure.

3. **Align package names and minimal source layout**
   - Ensure common Kotlin source uses a consistent package, preferably `com.sliide.useractivity`.
   - Under the shared/common source set, create or align lightweight package folders for future work:
     - `ui/`
     - `ui/theme/`
     - `ui/components/` only if a Kotlin source file is actually needed;
     - `presentation/` only if a Kotlin source file is actually needed;
     - `domain/` only if a Kotlin source file is actually needed;
     - `data/` only if a Kotlin source file is actually needed.
   - Do not add Ktor, repositories, ViewModels, DTOs, or feature-specific files in this foundation item.

4. **Add or simplify the shared app entry point and minimal app shell**
   - Keep or create the shared `App()` composable in the generated common source set, for example `shared/src/commonMain/kotlin/com/sliide/useractivity/App.kt` if that is the generated module layout.
   - Add or align a minimal `AppTheme.kt` under `ui/theme/` using Material3 theme values if the template does not already provide one.
   - Render a simple app shell with the app name and placeholder text such as “User Activity Manager” and “Project foundation ready”.
   - Keep the UI stateless and avoid hardcoded production styling beyond minimal layout constants.

5. **Verify platform entry points call the shared UI**
   - Confirm the Android launcher activity calls the shared `App()` composable.
   - Confirm the iOS app/project hosts the shared Compose UI through the generated framework or template-provided entry point.
   - Only edit platform entry points if package/module renaming broke generated references.

6. **Update `.gitignore` for generated/build artefacts**
   - Extend `.gitignore` with Gradle, Kotlin, Android Studio, Xcode, macOS, and build-output ignores.
   - Preserve the existing local AI/auth ignores from `.gitignore:1-4`.
   - Do not ignore source files, Gradle wrapper files, Xcode project files needed to build, or the plan files.

7. **Create the initial README**
   - Create `README.md` with:
     - project name and short purpose;
     - target platforms;
     - current foundation status;
     - basic build/run commands for Android and shared checks using the actual generated module names;
     - a note that GoREST/API implementation is planned in later roadmap items;
     - an initial “AI-Assisted Development” section referencing Pi, Android Studio template generation, and the local planning workflow, without claiming unavailable transcript exports.
   - Keep README content concise and updateable rather than a final submission document.

8. **Run formatting/build verification and fix foundation failures**
   - Run `./gradlew projects` to confirm module wiring.
   - Run the shared/common metadata compile task available in the generated project, for example `./gradlew :shared:compileKotlinMetadata` if the module is named `shared`.
   - Run the Android debug build task available in the generated project, for example `./gradlew :androidApp:assembleDebug` if the module is named `androidApp`.
   - On macOS with Xcode available, run the relevant iOS framework/build task, for example `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`, then open/build the generated iOS app if it exists.
   - Fix only foundation-related issues; defer feature implementation to later plans.

## Verification

- `./gradlew projects` lists `:shared` and `:androidApp` without configuration errors.
- `./gradlew :shared:compileKotlinMetadata` or equivalent common metadata compile succeeds.
- `./gradlew :androidApp:assembleDebug` succeeds.
- `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64` succeeds on macOS with the iOS toolchain installed.
- Android app launches to the minimal shared `App()` shell.
- iOS app or shared iOS framework builds far enough to prove iOS target configuration.
- `README.md` exists and explains the current foundation and next-roadmap scope.
- `.gitignore` keeps local auth/AI files ignored and also excludes common build artefacts.

## Risks

- Android Studio template availability and naming may vary by installed Android Studio/Kotlin Multiplatform plugin version, so implementation steps must adapt to the generated module names.
- Exact Kotlin, Compose Multiplatform, AGP, Gradle, and Xcode version compatibility should be trusted from the generated template unless verification exposes a real issue.
- Android Studio MCP can help with project opening, Gradle sync, running configurations, and error inspection, but it does not replace the human decision to create the app from the official IDE template.
- Android SDK or Xcode components may be missing locally, preventing full platform verification even if project files are correct.
- Renaming generated modules too aggressively can break iOS/Xcode wiring; prefer package/source cleanup over structural churn.
- Adding Ktor, serialization DTOs, ViewModels, navigation, shimmer, or real screens during this item would expand scope and should be deferred to roadmap items 12.2 and 12.3+.
