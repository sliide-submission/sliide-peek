# Post-MVP Improvements (Roadmap 12.9)

## Goal

Plan a focused post-MVP pass for four selected improvements: paginated/infinite user feed, read-only user detail, live device connectivity status, and an additional unit/UI test sweep. Keep the completed 12.4–12.8 architecture intact and do **not** add edit-user, token/settings, or posts/comments/todos UX.

## Approach summary

- Keep the current shared KMP layers: Ktor client → repository → use cases → `UserFeedViewModel` → stateless Compose UI.
- Treat page 1 as the latest users page and load older users by requesting page 2, page 3, etc. as scroll nears the end.
- Reuse the existing tablet/iPad action panel for read-only detail where practical; add a compact/mobile detail route that renders the selected feed item only.
- Add connectivity as device online/offline state. Repository/API failures remain separate `AppError`/error-flow concerns.
- Add tests where they prove behaviour, not visuals-for-visuals' sake.

## Codebase findings

- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/presentation/users/GetSmartUserFeedUseCase.kt:13-21` defines `LoadUserFeedUseCase` with no page/load-more parameters and `UserFeedResult` with only `users/fromCache/lastUpdatedMillis`.
- `GetSmartUserFeedUseCase.kt:30-52` always requests `page = 1`, caches that single page, and returns it as the feed. This is the correct starting page for the latest feed but has no older-page support yet.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/domain/model/Page.kt:3-10` already models `page`, `perPage`, `totalPages`, and `hasNextPage`, so no new pagination model is needed at the domain level.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/data/remote/GorestApiClient.kt:26-38` already accepts explicit `page`/`perPage` and parses GoREST pagination headers.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/data/local/SqlDelightUserCacheDataSource.kt:11-38` replaces the cached feed with one page; `:40-54` reads that one cached feed; `:92-95` still names the key `users:last-page` even though current behaviour is page 1.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/presentation/users/UserFeedState.kt:3-14` holds loading/refresh/users/error/offline/add/delete state but no `isLoadingMore`, `nextPage`, or `hasMore` fields.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/presentation/users/UserFeedViewModel.kt:37-53` exposes initial load/refresh/retry only; `:231-269` replaces all users on each load result.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/users/UserFeedScreen.kt:117-153` renders a `LazyColumn` with `rememberLazyListState`, but it has no end-of-list observer or load-more footer yet.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/AppRoot.kt:111-114` currently handles row click by clearing highlight and selecting a user only. It does not push `AppRoute.UserDetail` on compact/mobile.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/CompactAppShell.kt:70-76` routes `AppRoute.UserDetail` to `UserDetailPlaceholder`, which still exposes posts/todos callbacks. This must be replaced with a read-only user detail view and not reintroduce posts/comments/todos.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/ExpandedAppShell.kt:74-89` already resolves the selected `UserFeedItem` and shows `UserActionPanel`, which is a good tablet/iPad detail surface.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/users/UserActionPanel.kt:75-156` already renders a selected user hero/details/actions. For this item, keep it read-only apart from existing add/delete actions; do not add edit.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/navigation/AppRoute.kt:3-8` still contains posts/todos/post detail routes from earlier scope. They can remain if removing them would churn, but new work should not use them.
- No live connectivity monitor exists. `rg` only finds offline state derived from `AppError.Network`/`Timeout` in `UserFeedViewModel.kt:260-265`; platform `expect/actual` usage currently exists for DB drivers and back handling only.
- Koin is already wired in `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/di/AppModules.kt:32-71`, so connectivity/use-case providers should be added there rather than introducing another service locator.
- Current tests include API/repository/use-case/ViewModel/navigation/layout/display tests under `SliidePeek/shared/src/commonTest/...`, including `GetSmartUserFeedUseCaseTest`, `UserFeedViewModelTest`, `AppNavigatorTest`, `AppLayoutTest`, and `UserDisplayTest`. Shared build/test dependencies are in `SliidePeek/shared/build.gradle.kts:124-144`; no Compose UI test dependency is present yet.

## Implementation order

### 1. Extend feed result/state for pagination metadata

**Files:**
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/presentation/users/GetSmartUserFeedUseCase.kt`
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/presentation/users/UserFeedState.kt`

Add minimal pagination fields:
- `UserFeedResult.currentPage: Int`
- `UserFeedResult.totalPages: Int`
- `UserFeedResult.hasNextPage: Boolean`
- `UserFeedState.isLoadingMore: Boolean = false`
- `UserFeedState.loadMoreErrorMessage: String? = null`
- `UserFeedState.nextPage: Int? = null`
- `UserFeedState.hasMoreUsers: Boolean = false`

Keep the initial feed semantics unchanged: page 1 is still the latest users page.

### 2. Add an explicit older-users page use case

**Files:**
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/presentation/users/GetSmartUserFeedUseCase.kt` or new `LoadOlderUsersUseCase.kt`
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/di/AppModules.kt`

Create a small `LoadUserFeedPageUseCase` or `LoadOlderUsersUseCase` that requests a caller-supplied page number through `UserRepository.getUsers(page, perPage)` and maps that page to `UserFeedResult`.

Rules:
- `page = 1` remains latest users.
- `page > 1` loads older users and should be appended to existing state.
- Do not discover or start from the final GoREST page.
- Do not load posts/comments/todos.
- Keep error mapping via existing repository `AppResult` flow.

### 3. Update cache support without rewriting persistence

**Files:**
- `SliidePeek/shared/src/commonMain/sqldelight/com/sliide/useractivity/data/local/UserCache.sq`
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/data/local/UserCacheDataSource.kt`
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/data/local/SqlDelightUserCacheDataSource.kt`
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/data/local/CachedUserFeed.kt`

Extend the existing cache so it can represent the loaded feed window, not only one replaced page:
- Rename constants/copy from `last-page` meaning to latest-feed where possible.
- Add an append/merge method such as `appendCachedPage(page, fetchedAtMillis, cachedAtMillis)` that inserts page 2+ rows after existing rows, de-duping by `id` and preserving order.
- Preserve `replaceCachedFeed` for page 1 refreshes.
- Persist enough metadata to restore `currentPage`, `totalPages`, and `hasNextPage` for cached feeds.

Keep this as a small SQLDelight extension. Do not introduce a new database library or large sync engine.

### 4. Add ViewModel load-more behaviour

**Files:**
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/presentation/users/UserFeedViewModel.kt`
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/presentation/users/UserFeedState.kt`
- `SliidePeek/shared/src/commonTest/kotlin/com/sliide/useractivity/presentation/users/UserFeedViewModelTest.kt`

Add `fun loadMoreUsers()`:
- No-op if `isLoading`, `isRefreshing`, `isLoadingMore`, `!hasMoreUsers`, or `nextPage == null`.
- On success, append older users to `state.users` while de-duping IDs.
- Update `nextPage`/`hasMoreUsers` from the page result.
- On failure, keep existing users visible and set `loadMoreErrorMessage`; optionally emit a snackbar event for retry copy.
- Refresh should reset pagination to page 1 and replace the feed.
- Add/delete behaviour should remain coherent: newly added users still prepend to the list, delete/undo still use current indexes.

### 5. Trigger infinite scroll from the feed UI

**Files:**
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/users/UserFeedScreen.kt`
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/CompactAppShell.kt`
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/ExpandedAppShell.kt`
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/AppRoot.kt`

Thread `onLoadMoreUsers: () -> Unit` into `UserFeedScreen`.

In `UserFeedScreen`:
- Use `snapshotFlow` over `LazyListState.layoutInfo` to call `onLoadMoreUsers` when the last visible item is within ~3 items of the end.
- Show a small Material 3/Signal footer row when `state.isLoadingMore` is true.
- If `loadMoreErrorMessage` is set, show a compact retry footer/banner rather than replacing the feed.
- Avoid auto-scrolling to top when older pages append. The existing `LaunchedEffect(state.users.firstOrNull()?.id, state.lastUpdatedLabel)` should only run for refresh/new-top changes, not page append.

### 6. Add read-only compact/mobile user detail

**Files:**
- New `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/users/UserDetailScreen.kt`
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/CompactAppShell.kt`
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/AppRoot.kt`
- Optionally `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/navigation/AppNavigator.kt`

Create a read-only detail composable that accepts `UserFeedItem?` and callbacks for back handled by the shell only:
- Show avatar initials, name, email, status, gender, relative timestamp, and user ID.
- Use the same Signal visual treatment as `UserActionPanel`.
- If the user is missing from state (e.g. after refresh/delete), show a small empty/error message and a back affordance via the existing top bar.

Update compact behaviour:
- On compact/mobile row tap, navigate to `AppRoute.UserDetail(userId)`.
- In `CompactAppShell`, replace `UserDetailPlaceholder` with the new read-only detail screen.
- Do not expose posts/todos links and do not add edit.

Expanded/tablet behaviour:
- Continue using selection-in-place (`navigator.selectUser(userId)`) and `UserActionPanel`.
- If useful, extract shared detail content from `UserActionPanel` into a reusable `UserDetailContent` used by both compact detail and tablet panel.

### 7. Add live connectivity monitor abstraction

**Files:**
- New `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/domain/connectivity/ConnectivityMonitor.kt`
- New Android actual/implementation under `SliidePeek/shared/src/androidMain/kotlin/.../connectivity/`
- New iOS actual/implementation under `SliidePeek/shared/src/iosMain/kotlin/.../connectivity/`
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/di/AppModules.kt`
- `SliidePeek/androidApp/src/main/AndroidManifest.xml` if `ACCESS_NETWORK_STATE` is required

Add a simple common API:

```kotlin
interface ConnectivityMonitor {
    val status: Flow<ConnectivityStatus>
}

enum class ConnectivityStatus { Online, Offline, Unknown }
```

Implementation direction:
- Android: use `ConnectivityManager`/`NetworkCallback` and current active network capabilities.
- iOS: use `NWPathMonitor`.
- Keep `Unknown` as safe startup/fallback.
- Register via Koin, similar to DB/platform setup.

Do **not** treat connectivity as API reachability. GoREST failures still flow through repositories as `AppError` and screen errors.

### 8. Integrate connectivity into feed state/UI

**Files:**
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/presentation/users/UserFeedState.kt`
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/presentation/users/UserFeedViewModel.kt`
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/users/UserFeedScreen.kt`
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/AppRoot.kt`

Add `connectivityStatus` or `isDeviceOffline` to `UserFeedState` and collect the monitor in the ViewModel or root callback layer.

Behaviour:
- When device is offline and cached users exist, show the offline cached banner immediately.
- When device is offline and no users/cache exist, show the no-internet/no-cache state immediately.
- Disable or guard load-more while offline; keep existing retry semantics.
- On transition from offline to online, optionally call `refresh()` once if not already loading.
- Do not overwrite API error messages with connectivity messages when an API failure happens while online.

### 9. Additional shared unit test pass

**Files:** existing/new tests under `SliidePeek/shared/src/commonTest/kotlin/com/sliide/useractivity/...`

Add useful shared tests for:
- Initial feed loads page 1 and sets `nextPage = 2` when `hasNextPage` is true.
- Load-more appends page 2 and page 3 in order and de-dupes IDs.
- Load-more failure preserves current users and exposes retry/error footer state.
- Refresh after pagination replaces the feed with page 1 and resets pagination metadata.
- Connectivity offline with cached users sets offline banner state without requiring an API failure.
- Connectivity offline with no users/cache shows the no-cache offline state.
- Compact row click navigates to `AppRoute.UserDetail(id)`; expanded row click only selects the row.
- Read-only detail renders the selected user data from `UserFeedState` (pure display helpers if Compose UI tests are not practical).

Prefer small fakes over mocking frameworks, consistent with existing tests.

### 10. Compose/UI test pass

**Files/dependencies:**
- `SliidePeek/gradle/libs.versions.toml`
- `SliidePeek/shared/build.gradle.kts` or `SliidePeek/androidApp/build.gradle.kts`
- New UI tests under the most practical target, likely `SliidePeek/shared/src/androidHostTest/...` or `SliidePeek/androidApp/src/androidTest/...`

First inspect whether Compose Multiplatform UI testing APIs are available for the current plugin/version. If adding dependencies, use the version catalog and keep scope minimal.

Candidate UI tests:
- Feed shows rows and triggers `onLoadMoreUsers` when scrolled near the end.
- Compact user row click displays the read-only detail screen.
- Offline banner appears when `state.isDeviceOffline`/offline cached state is true.
- Load-more footer appears while `state.isLoadingMore` is true.

If Compose UI tests are too brittle or dependency-heavy, document that decision in the plan implementation notes and add equivalent pure/shared state tests instead.

## Verification

Run from `SliidePeek/`:

```bash
./gradlew :shared:compileKotlinMetadata
./gradlew :shared:allTests
./gradlew :androidApp:assembleDebug
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64
```

If Compose/UI tests are added, also run the relevant target, for example:

```bash
./gradlew :shared:connectedDebugAndroidTest
# or the exact androidHost/androidApp UI-test task introduced by the implementation
```

Manual checks:
- Fresh online load starts at page 1 and shows latest users.
- Scrolling near the end loads older users; appending does not jump to the top.
- Load-more errors keep existing users visible and offer retry/feedback.
- Pull/explicit refresh returns to latest page 1 and resets older-page state.
- Compact/mobile row tap pushes a read-only detail screen; Back returns to the feed.
- Tablet/iPad row tap updates the right detail/action panel in place.
- Device offline state changes update the offline banner/state without waiting for an API request.
- API failures while online still show existing repository error UI, not connectivity UI.
- Add/delete/undo behaviours from 12.6/12.7 still work after pagination.

## Risks

- GoREST pagination can change as new users are created; page 2 may shift between refreshes. Keep infinite scroll simple and de-dupe by ID rather than trying to build a production cursor system.
- Extending the cache from one page to a loaded feed window is the only persistence change with meaningful churn. Keep SQLDelight changes small and avoid a sync engine.
- Platform connectivity APIs require careful lifecycle cleanup. Use `callbackFlow`/`awaitClose` or equivalent and test fakes in common code.
- Compose UI tests may require additional dependencies/tasks and can be brittle in KMP. Prefer a small number of high-value UI tests plus shared state tests.
- Existing navigation still has legacy posts/todos routes. This plan should not expand them; compact detail must be read-only user info only.
