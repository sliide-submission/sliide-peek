# Smart User Feed Implementation Plan

**Status:** Closed — implemented and verified. 12.4A delivered shared relative timestamp logic, last-page `/users` fetch, `UserFeedViewModel`/state holder, repository-backed feed UI, loading/error/offline-ready states, and tests. Automated verification passed and Android/iOS manual checks were confirmed by the user.

## Goal

Implement roadmap 12.4 as the official primary user feed: load users from the last GoREST `/users` page, display name/email plus a relative timestamp computed in shared KMP logic, and connect the existing shell to a `UserFeedViewModel` with loading, refresh, retry, empty, error, and no-internet/offline-ready states. Keep the UI deliberately minimal so the forthcoming high-fidelity design can replace styling without reworking the data/state flow.

## Codebase findings

- `AGENTS.md:18-28` says roadmap item 12.4 onward should prioritise the official Sliide challenge while keeping the app simple, KMP-first, Compose Multiplatform-based, and free of unnecessary production-SDK complexity.
- `userapplicationspecs.md:83-111` defines Smart User Feed requirements: fetch users from the last `/users` page, show name/email/relative timestamp, compute the timestamp in shared KMP logic, use a documented local timestamp strategy if GoREST has no timestamp, and support shimmer, error, no-internet/offline, empty, refresh, and cached/offline display states.
- `userapplicationspecs.md:842-858` scopes roadmap 12.4 to last-page users, relative timestamp, ViewModel/state integration, shimmer, no-internet/offline state, refresh/retry, a local cached-feed seam if persistence is not available yet, and unit tests.
- `designer-brief.md:24-43` repeats the user-feed states and notes that the timestamp can be based on a local concept such as `fetched just now`, `cached 5 minutes ago`, or `created 2 minutes ago`.
- Requested `design-change-brief.md` was not present in the repo. The closest available design brief is `designer-brief.md`; high-fidelity updated designs are explicitly not ready, so this plan avoids final visual styling.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/App.kt:8-13` still exposes the single shared Compose entry point and wraps `AppRoot()` in `AppTheme`.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/AppRoot.kt:8-17` creates an in-memory `AppNavigator` and chooses compact vs expanded shells from width; this is the natural place to add a tiny non-Koin app container/ViewModel seam for 12.4.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/CompactAppShell.kt:20-37` currently renders `UserListPlaceholder` for the `Users` route and wires a no-op refresh callback in the top bar.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/ExpandedAppShell.kt:35-55` currently renders a persistent master pane with placeholder users, a hardcoded count of `24`, and a no-op refresh callback.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/RoutePlaceholders.kt:45-107` contains private demo users and `UserListPlaceholder`; these should be replaced only for the user feed while detail/post/todo placeholders can remain until later roadmap items.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/components/ContentState.kt:10-57` already provides generic loading/empty/error/content switching and retry wiring.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/components/Skeleton.kt:27-83` already provides an animated generic skeleton row suitable for the 12.4 loading shimmer with minimal styling work.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/data/remote/GorestApiClient.kt:18-30` can request `/users?page=&per_page=` and returns pagination metadata, including `totalPages`, from GoREST pagination headers.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/domain/repository/UserRepository.kt:9-13` exposes `getUsers(page, perPage): AppResult<Page<User>>`, which is sufficient to discover and then request the last page without adding a new API endpoint.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/data/repository/UserRepositoryImpl.kt:15-23` maps paged DTO users to domain `Page<User>`.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/domain/model/User.kt:3-9` has no server timestamp field, so 12.4 needs a shared presentation/domain feed model that attaches a local `fetchedAt` timestamp.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/domain/model/Page.kt:3-10` exposes `totalPages` and `hasNextPage` for last-page logic.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/domain/AppError.kt:3-10` distinguishes network, timeout, auth, not-found, validation, server, and unknown failures; feed presentation can map these to no-internet/error copy.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/data/AppDataContainer.kt:13-24` already manually wires `UserRepository`, `PostRepository`, and `TodoRepository`; 12.4 can use this as a temporary seam and leave Koin for roadmap 12.5.
- `SliidePeek/shared/build.gradle.kts:42-58` already includes Compose, lifecycle ViewModel Compose/runtime, Ktor, serialization, `kotlin.test`, Ktor MockEngine, and coroutines test dependencies. No dependency should be needed for 12.4 unless Kotlin time APIs prove unavailable.
- `SliidePeek/shared/src/commonTest/kotlin/com/sliide/useractivity/data/remote/GorestApiClientTest.kt:17-31` already verifies user pagination headers, so 12.4 tests can focus on use-case/ViewModel state rather than duplicating API parsing coverage.

## Approach summary

Add a small shared presentation layer for the feed. A `GetSmartUserFeedUseCase` should make one lightweight users request to discover `totalPages`, request that last page, and attach one local `fetchedAt` timestamp to every returned row. A shared `RelativeTimeFormatter` should turn that timestamp into strings like `Just now`, `5 minutes ago`, and `2 hours ago` using an injected clock/now provider so tests are deterministic. A `UserFeedViewModel` should expose immutable `StateFlow<UserFeedState>` and handle initial load, explicit refresh, and retry. The UI should be stateless and basic, reusing current skeleton/state components.

Do not implement add-user, delete/undo, Koin, SQLDelight/Room persistence, API tokens, or final design polish in this plan. A small in-memory last-successful feed retained by the ViewModel is acceptable as an offline-ready seam, but durable offline cache belongs to roadmap 12.5.

## Implementation order

1. **Add shared time and timestamp formatting logic**
   - Create: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/domain/time/AppClock.kt`.
   - Create: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/domain/time/RelativeTimeFormatter.kt`.
   - Represent time as epoch milliseconds in feed state/models to keep the UI and tests simple.
   - Implement `AppClock`/`SystemAppClock` using common Kotlin time APIs available in Kotlin `2.3.21` (for example `kotlin.time.Clock.System.now().toEpochMilliseconds()`). If that API requires opt-in, keep opt-in local to the clock implementation.
   - Implement pure formatting from `thenMillis` and `nowMillis`: future/under-60-seconds => `Just now`, then minutes, hours, days, weeks or a simple long-tail such as `30+ days ago`.
   - Keep copy centralized and easy to change when design copy arrives.

2. **Create feed presentation models**
   - Create package: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/presentation/users/`.
   - Create: `UserFeedItem.kt` with fields such as `id`, `name`, `email`, `gender`, `status`, `fetchedAtMillis`, and `relativeTimestamp` or enough data for the ViewModel to expose already-formatted text.
   - Create: `UserFeedState.kt` with at least:
     - `isLoading: Boolean` for initial load;
     - `isRefreshing: Boolean` for refresh with existing content;
     - `users: List<UserFeedItem>`;
     - `errorMessage: String?` for non-network errors;
     - `isOffline: Boolean` or `offlineMessage: String?` for no-internet/offline-ready states;
     - `canRetry: Boolean`;
     - derived empty handling can stay in UI or be represented as a boolean.
   - Keep these models UI-framework-free and shared commonMain friendly.

3. **Add a last-page users use case**
   - Create: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/domain/usecase/GetLastPageUsersUseCase.kt` or `presentation/users/GetSmartUserFeedUseCase.kt` if keeping use cases presentation-adjacent feels simpler.
   - Constructor dependencies: `UserRepository`, `AppClock`, optional `perPage: Int = 20`.
   - Implementation:
     1. call `userRepository.getUsers(page = 1, perPage = perPage)` to read `totalPages`;
     2. calculate `lastPage = maxOf(1, firstPage.totalPages)`;
     3. if `lastPage == firstPage.page`, reuse the first response; otherwise call `userRepository.getUsers(page = lastPage, perPage = perPage)`;
     4. attach one `fetchedAtMillis = clock.nowMillis()` to all feed items from the final page;
     5. return `AppResult.Success(List<UserFeedItem>)` or propagate `AppResult.Failure`.
   - Do not alter `GorestApiClient` unless a test reveals pagination header naming is insufficient.

4. **Implement `UserFeedViewModel`**
   - Create: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/presentation/users/UserFeedViewModel.kt`.
   - Use `androidx.lifecycle.ViewModel` and `viewModelScope` if available from the existing lifecycle dependencies; otherwise implement a small state-holder class with an injected `CoroutineScope` and keep the public name `UserFeedViewModel`.
   - Expose private `MutableStateFlow(UserFeedState())` and public `StateFlow<UserFeedState>`.
   - Public actions:
     - `load()` for initial load, idempotent when already loaded/loading;
     - `refresh()` for explicit refresh/pull-to-refresh or top-bar refresh;
     - `retry()` for retry after error/no internet.
   - Map errors from `AppError`:
     - `Network`/`Timeout` => no-internet/offline copy;
     - keep existing users visible during refresh failure and set `isOffline = true`;
     - if no existing users, show a no-internet state with retry;
     - server/unknown => generic error state with retry.
   - Keep an in-memory last-successful list in state only; do not add durable persistence yet.

5. **Create stateless feed UI composables**
   - Create: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/users/UserFeedScreen.kt`.
   - Create: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/users/UserFeedRow.kt` if keeping rows separate improves readability.
   - Inputs should be only `state`, `selectedUserId`, `onUserClick`, `onRefresh`, and `onRetry`.
   - Use existing `ContentStateContainer`/`SkeletonRow` for initial shimmer loading.
   - Render minimal rows/cards with name, email, and relative timestamp. Optional status/gender chips can reuse `StatusChip`, but avoid final high-fidelity layout decisions.
   - Add a small offline/no-internet banner or state message using Material theme colours only. If users exist, show the banner above the list; if no users exist, show the retry-first no-internet state.
   - Preserve the existing `SegmentedFilter` only if it is cheap and local; filtering is not core to 12.4 and should not distract from the official feed.

6. **Wire the feed into compact and expanded shells**
   - Touch: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/AppRoot.kt`.
   - Touch: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/CompactAppShell.kt`.
   - Touch: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/ExpandedAppShell.kt`.
   - Optionally create: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/AppContainer.kt` if it keeps `AppRoot` tidy.
   - Instantiate `AppDataContainer`, `SystemAppClock`, `GetLastPageUsersUseCase`, and `UserFeedViewModel` through `remember`/manual wiring for now. Do not add Koin yet.
   - Collect ViewModel state in Compose and call `load()` from a `LaunchedEffect` when the root appears.
   - Replace `UserListPlaceholder` only for the `Users` route/master pane with `UserFeedScreen`.
   - Update top-bar subtitle/count from `state.users.size` and wire top-bar refresh to `viewModel.refresh()`.
   - Keep `UserDetailPlaceholder`, posts, todos, and post placeholders unchanged for now; clicking a real feed user can still select/navigate to placeholder detail content until later roadmap items replace detail UX.

7. **Keep placeholder/demo code contained**
   - Touch: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/RoutePlaceholders.kt` only as needed.
   - Remove or stop using `UserListPlaceholder` for the main feed. If detail placeholders still need demo user data, keep the private demo models there and clearly avoid coupling them to the new feed state.
   - Do not promote placeholder posts/todos into real repository-backed UI in 12.4.

8. **Add tests for relative timestamp logic**
   - Create: `SliidePeek/shared/src/commonTest/kotlin/com/sliide/useractivity/domain/time/RelativeTimeFormatterTest.kt`.
   - Cover: future/negative delta, under 60 seconds, singular/plural minutes, hours, days, and long-tail behaviour.
   - Tests should be pure `kotlin.test` without platform clocks.

9. **Add tests for last-page feed use case**
   - Create: `SliidePeek/shared/src/commonTest/kotlin/com/sliide/useractivity/presentation/users/GetSmartUserFeedUseCaseTest.kt` or matching the chosen use-case package.
   - Use a fake `UserRepository` rather than Ktor MockEngine because API parsing is already covered.
   - Cover:
     - first request discovers `totalPages` and second request asks for that last page;
     - when `totalPages` is `1`, no second request is made;
     - users receive the injected clock timestamp;
     - repository failures propagate without crashing.

10. **Add tests for `UserFeedViewModel` state transitions**
    - Create: `SliidePeek/shared/src/commonTest/kotlin/com/sliide/useractivity/presentation/users/UserFeedViewModelTest.kt`.
    - Use `runTest`, a fake use case/repository, and deterministic clock/dispatcher setup.
    - Cover:
      - initial state is empty and not loading;
      - `load()` emits loading then success with users;
      - empty success produces empty non-error state;
      - network failure with no prior users produces no-internet retry state;
      - refresh with existing users keeps users visible, toggles `isRefreshing`, and records offline/error copy on failure;
      - `retry()` clears the previous error/offline flag before attempting again.
    - Avoid brittle tests for every intermediate emission if lifecycle/ViewModel scheduling makes them noisy; assert final state plus one or two important loading/refresh transitions.

11. **Update README with 12.4 status**
    - Touch: `SliidePeek/README.md`.
    - Add a concise note that the app now has a smart user feed backed by the last GoREST `/users` page, shared relative timestamp logic using local `fetchedAt`, a shared ViewModel/state holder, shimmer/loading/error/no-internet states, and tests.
    - Explicitly note that durable offline cache, Koin, add-user, and delete/undo are planned for later roadmap items.

## Verification

Run from `SliidePeek/`:

- `./gradlew :shared:compileKotlinMetadata` succeeds.
- `./gradlew :shared:allTests` succeeds, or run the available common/host test tasks and document any local native simulator/toolchain limitation.
- `./gradlew :androidApp:assembleDebug` succeeds.
- `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64` succeeds on macOS with the iOS toolchain installed.
- Manual compact check: launch Android/iPhone-sized app and verify the root Users screen shows shimmer on first load, then real GoREST users with name, email, and relative timestamp; refresh/retry controls invoke the ViewModel.
- Manual expanded check: launch iPad/tablet width and verify the left master pane uses the same real feed state, count, selected-row behaviour, and refresh action while detail remains placeholder-based.
- Manual no-internet check: disable network or use a failing fake endpoint during development and verify no-internet copy plus retry is shown. If users were previously loaded in memory, verify they remain visible with an offline indicator during refresh failure.
- Scope review: no add-user flow, delete/undo, Koin setup, bearer token handling, SQLDelight/Room persistence, or final high-fidelity visual styling was introduced.

## Risks

- GoREST may change or omit pagination headers. The use case should guard with `maxOf(1, totalPages)` and fall back to the first response if metadata is absent or invalid.
- GoREST users do not include creation timestamps. The plan intentionally uses local `fetchedAtMillis`; this must be documented in code/README so reviewers understand the relative timestamp source.
- Kotlin common time APIs can vary by Kotlin version/opt-in status. Keep the clock isolated so the implementation can switch to another small KMP clock approach without touching ViewModel/UI code.
- Manual ViewModel wiring is acceptable for 12.4 but should stay small; Koin-based DI belongs to roadmap 12.5.
- In-memory offline-ready behaviour is not durable offline caching. It only preserves already-loaded users during the current app process; SQLDelight/Room persistence is explicitly later scope.
- Existing user detail/post/todo placeholders may not correspond to real feed users after this change. That mismatch is acceptable for 12.4 if kept visually minimal and replaced in later roadmap items.
