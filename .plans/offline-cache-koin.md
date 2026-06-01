# Offline Cache and Koin DI

## Goal

Add the smallest practical offline persistence and dependency-injection setup needed for roadmap item 12.5: cache the GoREST `/users` last-page feed locally, show cached users when the network is unavailable, and wire API/cache/repositories/use cases/state holders through Koin.

## Approach summary

- Use **SQLDelight** rather than Room KMP. It is the smaller fit here because it avoids KSP/annotation setup, works well in common code with Android/iOS drivers, and is enough for a single cached feed table.
- Use **Koin core** for shared DI wiring. Add Koin Compose only if it materially simplifies `AppRoot`; otherwise instantiate a small `KoinApplication` from `AppRoot` and retrieve factories from it.
- Keep the existing network-first smart feed behaviour, but on `Network`/`Timeout` failures fall back to the cached last-page feed and mark the resulting UI state as offline/cached.
- Do not implement Add User, Delete/Undo, or high-fidelity polish in this item. Add cache seams that those later items can update.

## Codebase findings

- Gradle dependencies already use a version catalog in `SliidePeek/gradle/libs.versions.toml:1-53`; Ktor/coroutines/test aliases are present, but SQLDelight and Koin aliases/plugins are not yet present.
- Shared build setup is in `SliidePeek/shared/build.gradle.kts:3-9` and source-set dependencies are in `SliidePeek/shared/build.gradle.kts:39-66`; no local database plugin/dependencies are configured yet.
- The root Gradle plugin block applies shared plugin aliases from the catalog in `SliidePeek/build.gradle.kts:1-9`; any SQLDelight plugin alias should also be added here with `apply false` if required by the plugin.
- The current GoREST client fetches paged users and reads pagination headers in `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/data/remote/GorestApiClient.kt:18-30` and `:52-65`.
- `UserRepositoryImpl` is network-only and currently maps API pages directly to domain models in `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/data/repository/UserRepositoryImpl.kt:12-23`.
- The domain repository exposes generic user/page methods only in `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/domain/repository/UserRepository.kt:9-14`; there is no cache-aware feed method yet.
- The smart feed use case discovers the last page by fetching page 1 then `totalPages`, stamps users with `fetchedAtMillis`, and returns only `List<UserFeedItem>` in `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/presentation/users/GetSmartUserFeedUseCase.kt:13-39`.
- `UserFeedState` already has offline and last-updated fields in `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/presentation/users/UserFeedState.kt:3-14`, so the UI model can represent UX v2 cached/offline banners without a large screen rewrite.
- `UserFeedViewModel` maps network/timeout errors into `offlineMessage` but cannot currently receive cached-success metadata; see `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/presentation/users/UserFeedViewModel.kt:49-72`.
- `UserFeedScreen` already renders the two UX v2 offline cases: offline/no cache at `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/users/UserFeedScreen.kt:88-92` and offline-with-cache banner at `:105-110`.
- Manual dependency construction currently lives in `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/data/AppDataContainer.kt:13-24` and `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/AppRoot.kt:19-30`; this is what Koin should replace.
- Android and iOS entry points call parameterless `App()` at `SliidePeek/androidApp/src/main/kotlin/com/sliide/useractivity/MainActivity.kt:15-17` and `SliidePeek/shared/src/iosMain/kotlin/com/sliide/useractivity/MainViewController.kt:3-5`; SQLDelight drivers will require passing or creating a platform database driver factory.
- Existing tests cover network repository mapping (`SliidePeek/shared/src/commonTest/kotlin/com/sliide/useractivity/data/repository/UserRepositoryImplTest.kt:22-56`), last-page use-case behaviour (`SliidePeek/shared/src/commonTest/kotlin/com/sliide/useractivity/presentation/users/GetSmartUserFeedUseCaseTest.kt:18-58`), and feed ViewModel offline/error states (`SliidePeek/shared/src/commonTest/kotlin/com/sliide/useractivity/presentation/users/UserFeedViewModelTest.kt:67-112`).

## Decisions to lock in first

1. **Persistence:** SQLDelight, one generated database in `shared`, one cached last-page feed table plus small metadata table.
2. **Cache semantics:** network-first for explicit load/refresh; on successful last-page fetch, replace the cached feed atomically. On `Network`/`Timeout`, read cache; if cache is empty return the existing offline error state.
3. **Timestamp strategy:** preserve the existing local timestamp strategy. Store both `fetchedAtMillis` per cached row and `cachedAtMillis` in metadata. Use `cachedAtMillis` for the UX v2 banner (`updated 12 min ago`) and `fetchedAtMillis` for each row’s relative timestamp.
4. **DI:** Koin replaces `AppDataContainer`; avoid introducing a large DI abstraction or platform-specific service locators.

## Implementation order

1. **Add SQLDelight and Koin Gradle aliases**
   - Touch `SliidePeek/gradle/libs.versions.toml`.
   - Add versions and aliases for:
     - SQLDelight Gradle plugin.
     - `app.cash.sqldelight:runtime`.
     - `app.cash.sqldelight:android-driver`.
     - `app.cash.sqldelight:native-driver`.
     - optionally `app.cash.sqldelight:sqlite-driver` for JVM/host tests only if a suitable test target is added.
     - `io.insert-koin:koin-core`.
     - optionally `io.insert-koin:koin-compose` only if using Koin’s Compose helper API.
   - Touch `SliidePeek/build.gradle.kts` and `SliidePeek/shared/build.gradle.kts`.
   - Apply the SQLDelight plugin to `shared`, add SQLDelight database config, and add dependencies to the smallest source sets that need them.

2. **Create SQLDelight schema for the cached last-page feed**
   - Create `SliidePeek/shared/src/commonMain/sqldelight/com/sliide/useractivity/data/local/UserCache.sq`.
   - Suggested schema:
     - `cached_user_feed(id INTEGER PRIMARY KEY, name TEXT NOT NULL, email TEXT NOT NULL, gender TEXT NOT NULL, status TEXT NOT NULL, fetched_at_millis INTEGER NOT NULL, cached_at_millis INTEGER NOT NULL, position INTEGER NOT NULL)`.
     - `cached_feed_metadata(cache_key TEXT PRIMARY KEY, page INTEGER NOT NULL, per_page INTEGER NOT NULL, total_pages INTEGER NOT NULL, cached_at_millis INTEGER NOT NULL)`.
   - Add queries for `selectCachedFeed`, `selectMetadata`, `clearFeed`, `insertUser`, `replaceMetadata`, and optional `deleteUserById` / `upsertUserAtTop` seams for later add/delete work.

3. **Add platform SQLDelight driver factories**
   - Create common expect/abstraction under `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/data/local/DatabaseDriverFactory.kt`.
   - Create Android actual implementation under `SliidePeek/shared/src/androidMain/kotlin/com/sliide/useractivity/data/local/DatabaseDriverFactory.android.kt` using `AndroidSqliteDriver` and an Android `Context`.
   - Create iOS actual implementation under `SliidePeek/shared/src/iosMain/kotlin/com/sliide/useractivity/data/local/DatabaseDriverFactory.ios.kt` using `NativeSqliteDriver`.
   - Update `SliidePeek/androidApp/src/main/kotlin/com/sliide/useractivity/MainActivity.kt` to pass an Android driver factory into `App(...)`.
   - Update `SliidePeek/shared/src/iosMain/kotlin/com/sliide/useractivity/MainViewController.kt` to pass the iOS driver factory into `App(...)`.
   - Keep previews compiling by providing a preview-safe overload or a simple preview driver path if needed.

4. **Create local cache models, mappers, and data source**
   - Create files under `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/data/local/`:
     - `CachedUserFeed.kt` or equivalent small local model.
     - `UserCacheMapper.kt` for generated-row-to-domain/feed mapping and enum string mapping.
     - `UserCacheDataSource.kt` interface.
     - `SqlDelightUserCacheDataSource.kt` implementation.
   - Keep the data source suspend-based and simple:
     - `suspend fun replaceLastPage(page: Page<User>, fetchedAtMillis: Long, cachedAtMillis: Long)`.
     - `suspend fun getLastPageFeed(): CachedUserFeed?`.
     - optional later seams: `upsertCreatedUser(...)`, `removeUser(...)`, `restoreUser(...)` but do not wire add/delete flows yet.
   - Use SQLDelight transactions for clear-and-insert replacement.

5. **Make the smart feed use case cache-aware**
   - Touch `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/presentation/users/GetSmartUserFeedUseCase.kt`.
   - Replace `AppResult<List<UserFeedItem>>` with `AppResult<UserFeedResult>` where:
     - `UserFeedResult.users: List<UserFeedItem>`.
     - `UserFeedResult.fromCache: Boolean`.
     - `UserFeedResult.lastUpdatedMillis: Long?`.
   - Inject `UserCacheDataSource` into `GetSmartUserFeedUseCase`.
   - On network success: fetch page 1, fetch last page if needed, cache that final page with `fetchedAtMillis/cachedAtMillis`, and return `fromCache = false`.
   - On `Network`/`Timeout` failure: read cached feed; if present, return `Success(UserFeedResult(..., fromCache = true, lastUpdatedMillis = cachedAtMillis))`; if absent, return the original failure.
   - For non-offline errors, do not fall back silently unless the UX would be clearer; prefer existing error handling.

6. **Update ViewModel/state handling for cached success**
   - Touch `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/presentation/users/UserFeedViewModel.kt` and `UserFeedState.kt` only as needed.
   - When `fromCache = true`, keep `users` populated, set `offlineMessage` to a short UX v2-compatible message, clear `errorMessage`, set `canRetry = true`, and compute `lastUpdatedLabel` from `lastUpdatedMillis` via `RelativeTimeFormatter` and `AppClock`.
   - When network success arrives, clear `offlineMessage`, update users, and set `lastUpdatedLabel` from the current successful fetch/cache time rather than the first row’s relative label if practical.
   - Preserve existing `load`, `refresh`, and `retry` public functions.

7. **Replace manual construction with Koin modules**
   - Create `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/di/AppModules.kt`.
   - Define Koin providers for:
     - `AppClock` (`SystemAppClock`).
     - Ktor `HttpClient` and `GorestApiClient`.
     - SQLDelight database and `UserCacheDataSource`.
     - `UserRepository`, `PostRepository`, `TodoRepository`.
     - `LoadUserFeedUseCase` / `GetSmartUserFeedUseCase`.
     - `UserFeedViewModel` as a factory with a `CoroutineScope` parameter.
   - Delete or deprecate `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/data/AppDataContainer.kt` after all call sites are replaced.
   - Ensure the `HttpClient` is closed when the remembered Koin application is disposed, either via a small close hook or by retaining explicit disposal in `AppRoot`.

8. **Update shared app entry and root composition**
   - Touch `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/App.kt` and `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/AppRoot.kt`.
   - Change `App`/`AppRoot` to accept `DatabaseDriverFactory` (or a prebuilt Koin module) and create/remember the Koin application once.
   - Resolve `UserFeedViewModel` through Koin, passing `rememberCoroutineScope()` as the state-holder scope.
   - Keep `AppRoot` otherwise focused on layout and UI callbacks.

9. **Add cache and DI tests where practical**
   - Update `SliidePeek/shared/src/commonTest/kotlin/com/sliide/useractivity/presentation/users/GetSmartUserFeedUseCaseTest.kt` for the new `UserFeedResult` contract.
   - Add use-case tests for:
     - successful network fetch writes to a fake cache.
     - `Network`/`Timeout` with cache returns cached users and `fromCache = true`.
     - `Network`/`Timeout` with no cache returns failure.
   - Update `SliidePeek/shared/src/commonTest/kotlin/com/sliide/useractivity/presentation/users/UserFeedViewModelTest.kt` to assert cached-success offline banner state.
   - Add `SliidePeek/shared/src/commonTest/kotlin/com/sliide/useractivity/di/AppModulesTest.kt` if Koin can be started with fake/test modules in common tests; assert key definitions resolve.
   - Add SQLDelight integration tests only if a practical host target exists. If not, rely on generated query compilation plus fake-cache behavioural tests for this item.

10. **Keep future add/delete cache seams but no UI work**
    - Do not add Add User form UI or Delete/Undo UI in this plan.
    - It is acceptable to add local cache data-source methods that future add/delete repository work can call, but leave them uncalled unless required by tests/compilation.

## Verification

- From `SliidePeek/`, run:
  - `./gradlew :shared:compileKotlinMetadata`
  - `./gradlew :shared:allTests`
  - `./gradlew :androidApp:assembleDebug`
- Manual checks after implementation:
  - Launch online: users load from the GoREST last page and the feed looks unchanged from 12.4.
  - Launch/refresh with network unavailable after one successful load: cached users remain visible with an offline banner and an “updated N ago” label.
  - Launch offline before any cache exists: the existing no-internet/no-cache state appears.
  - Return online and refresh: banner clears and cache is replaced with fresh last-page users.
- Review dependency diff to confirm all new dependencies are in `libs.versions.toml`, not hardcoded in build files.

## Risks

- SQLDelight setup can be sensitive to Kotlin/Gradle/plugin version compatibility; choose the latest stable SQLDelight version compatible with the project’s Kotlin/AGP versions and adjust if Gradle resolution fails.
- Android SQLDelight requires a `Context`, so the currently parameterless `App()` entry point must change or gain a platform wrapper.
- There is no JVM target in the current KMP setup, so true SQLDelight integration tests may be awkward; prefer common fake-cache tests unless adding a test target is worth the extra Gradle complexity.
- Koin must not leak long-lived objects across previews/tests. Remember the Koin application at the app root and close the Ktor client on disposal.
- Cached GoREST users do not have server-created timestamps. The plan intentionally uses local `fetchedAtMillis/cachedAtMillis`, which should be documented in code comments or README later.
- Later Add/Delete work will need cache mutation semantics. Keep seams simple now, but avoid implementing those flows prematurely.
