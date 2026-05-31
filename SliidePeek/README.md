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

The current foundation intentionally contains only a basic app shell. GoREST networking and feature screens will be added in later roadmap items.
