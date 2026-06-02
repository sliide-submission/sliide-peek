# Architecture & build

Engineering reference for Sliide Peek. For what the app is and how it was built, see the [README](../README.md).

## Layers

Clean Architecture in a single shared module:

```text
ui            Compose Multiplatform screens, shell, theme, navigation (100% shared)
presentation  ViewModel + use cases + form validation (state, no platform APIs)
domain        Models, repository interfaces, AppResult/AppError, time + connectivity
data          Ktor API client, SQLDelight cache, repositories, DTO↔domain mappers
```

Key decisions:

- **One result type end to end.** Repositories return `AppResult<T>` (`Success` / `Failure(AppError)`); the ViewModel maps `AppError` to user-facing copy. Network/timeout failures fall back to the cache instead of surfacing an error.
- **Use cases own side effects.** Cache writes live in the use cases and are non-fatal — a DB write failure never fails an otherwise-successful network call — keeping the ViewModel a pure, easily unit-tested state machine.
- **Optimistic delete with Undo.** Deletes leave local state immediately and are committed to the server only after the Undo window closes; a server `404` is treated as success, any other failure restores the row.

## KMP usage

UI and business logic are fully shared. `expect`/`actual` is used only where a platform API is genuinely required:

| Seam | Android | iOS |
|---|---|---|
| `DatabaseDriverFactory` | `AndroidSqliteDriver` | `NativeSqliteDriver` |
| `ConnectivityMonitor` | `ConnectivityManager` callback flow | `nw_path_monitor` (cinterop) |
| `PlatformBackHandler` | `BackHandler` | no-op (single hosted controller) |
| Ktor engine | OkHttp | Darwin |

Everything else — the relative-timestamp formatter, validation, state, and all Compose UI — lives in `commonMain`.

## Project layout

```text
SliidePeek/
  androidApp/   Android entry point
  iosApp/       iOS entry point
  shared/       Shared Kotlin + Compose code (ui / presentation / domain / data)

design/         UX, high-fidelity UI, and engineering handoff files
.plans/         Roadmap implementation plans
.agents/skills/ Local AI-agent skills and workflow guidance
docs/ai/        AI-assisted development logs and session exports
```

## Build and run

```bash
cd SliidePeek
./gradlew :shared:testAndroidHostTest                    # shared unit tests (Android host)
./gradlew :shared:allTests                               # all targets incl. iOS
./gradlew :androidApp:assembleDebug                      # Android APK
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64    # iOS framework (macOS)
```

Run Android from Android Studio's app run configuration. Run iOS by opening the iOS project in Xcode or via Android Studio's KMP tooling.

### Authentication

GoREST write operations require a bearer token. The token is injected at build time from a local file / environment variable and is kept out of committed source — set `gorest.token` in `SliidePeek/local.properties` (or `GOREST_TOKEN` in the environment) before running the add/delete flows.
