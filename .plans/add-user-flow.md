# Adaptive Add User Flow

## Goal

Implement roadmap item 12.6: a FAB-launched add-user flow that follows `design/ux_v2.html`, supports phone bottom-sheet and tablet/iPad action-panel presentation, validates the form in real time, submits `POST /users`, and inserts the successfully created user at the top of the feed and local cache.

## Approach summary

- Keep the add flow in the existing user-feed feature rather than adding a new navigation stack.
- Extend `UserFeedViewModel` to own feed state, add-form state, submit state, and one-shot success/error events.
- Add a small `CreateUserUseCase` that performs the authenticated repository create call, stamps the local-created timestamp, updates the SQLDelight cache, and returns a `UserFeedItem` for immediate UI insertion.
- Use a token-provider seam so write operations can be authenticated without committing secrets. If no token is configured, return a clear unauthorised/token-required error and preserve the form input.
- Do not implement Delete with Undo or broad high-fidelity polish in this item.

## Codebase findings

- The GoREST client currently supports read-only endpoints only. `getUsers` and pagination header handling are in `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/data/remote/GorestApiClient.kt:18-30` and `:52-65`; there is no `POST /users` or auth header support yet.
- `UserDTO` already matches the response shape for created users in `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/data/remote/dto/UserDTO.kt:5-12`, and mapping to domain enums happens in `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/data/remote/mapper/UserMapper.kt:8-26`.
- `UserRepository` exposes only read methods in `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/domain/repository/UserRepository.kt:9-14`; `UserRepositoryImpl` is network-only and maps read responses in `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/data/repository/UserRepositoryImpl.kt:12-36`.
- Repository error mapping already handles `401/403` as `AppError.Unauthorized` and `422` as `AppError.Validation` in `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/data/repository/RepositoryErrorMapper.kt:25-36`.
- Smart-feed loading is cache-aware: `GetSmartUserFeedUseCase` fetches page 1, fetches the last page, replaces the cache, and returns feed metadata in `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/presentation/users/GetSmartUserFeedUseCase.kt:30-61`.
- The cache interface currently supports `replaceLastPage` and `getLastPageFeed` only in `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/data/local/UserCacheDataSource.kt:6-14`; it needs an add-at-top operation for successful creates.
- The SQLDelight schema already stores ordered cached users and metadata in `SliidePeek/shared/src/commonMain/sqldelight/com/sliide/useractivity/data/local/UserCache.sq:1-18`; cached rows are ordered by `position` in `:20-23`, and `insertUser` can be reused after adding position-shift queries in `:33-44`.
- `SqlDelightUserCacheDataSource.replaceLastPage` clears and rewrites rows inside a transaction in `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/data/local/SqlDelightUserCacheDataSource.kt:11-38`; `getLastPageFeed` maps rows back to a `Page<User>` in `:40-55`.
- Koin is already wired for clock, Ktor, SQLDelight, repositories, smart-feed use case, and `UserFeedViewModel` in `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/di/AppModules.kt:26-49`.
- `UserFeedViewModel` currently owns only load/refresh/retry state in `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/presentation/users/UserFeedViewModel.kt:14-80`; it has no add-form state or one-shot events yet.
- `UserFeedState` has feed/offline/error fields only in `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/presentation/users/UserFeedState.kt:3-14`.
- `AppRoot` passes no-op add callbacks to compact and expanded shells in `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/AppRoot.kt:48-62`.
- Compact UI already has the FAB entry point and passes `onAddUserClick` through `UserFeedScreen` in `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/users/UserFeedScreen.kt:26-62`; it needs to show the phone modal bottom sheet when add state is visible.
- Expanded UI already exposes a tablet app-bar add button and an action panel seam in `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/ExpandedAppShell.kt:42-67`; `UserActionPanel` currently shows selected-user or empty states only in `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/users/UserActionPanel.kt:28-99`.
- The adaptive breakpoint and master-pane widths are simple and suitable for this item in `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/AppLayout.kt:11-19`.
- Current tests cover API parsing, repository error mapping, use-case cache fallback, and feed ViewModel load/refresh states, for example `SliidePeek/shared/src/commonTest/kotlin/com/sliide/useractivity/data/remote/GorestApiClientTest.kt:16-92`, `SliidePeek/shared/src/commonTest/kotlin/com/sliide/useractivity/data/repository/UserRepositoryImplTest.kt:22-56`, and `SliidePeek/shared/src/commonTest/kotlin/com/sliide/useractivity/presentation/users/UserFeedViewModelTest.kt:21-166`.
- Existing dependencies already include Compose Material 3, Ktor content negotiation, kotlinx serialization, coroutines test, Koin, and SQLDelight in `SliidePeek/shared/build.gradle.kts:51-70`; no new dependency should be required for this flow.

## Decisions to lock in first

1. **Gender options:** use only `Female` and `Male` in the implemented form. UX v2 shows an `Other` segment, but the official GoREST requirement says gender must be `male` or `female`.
2. **Status options:** use `Active` and `Inactive`, with `Active` selected by default.
3. **Token strategy:** add an injectable `BearerTokenProvider` seam backed by a generated compile-time `BuildSecrets` object. The generator should read `GOREST_TOKEN` from the environment, `SliidePeek/local.properties`, or `SliidePeek/.env`; generated Kotlin lives under `build/generated/...` and must not be committed. Mock/tests can provide a fake token. If no local token exists, the generated value is blank and writes fail gracefully with a token-required error.
4. **No offline create queue in this item:** if submit fails due to no network or missing token, preserve input and show an inline submit error. Do not fake success locally.
5. **Timestamp strategy:** created users get `createdLocallyAtMillis`/`fetchedAtMillis = clock.nowMillis()` and display `just now`; the created row is cached with the same timestamp.

> Steps that require personal access, secrets, external systems, or explicit approval are tagged **[Human]**. Untagged steps are pure implementation work an agent can do.

## Implementation order

1. **Add domain create models and auth seam**
   - Create `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/domain/model/CreateUserRequest.kt` with `name`, `email`, `gender: UserGender`, and `status: UserStatus`.
   - Create `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/domain/auth/BearerTokenProvider.kt` with `fun getToken(): String?` or `suspend fun getToken(): String?`.
   - Add a `BuildSecretsBearerTokenProvider` that reads from a generated common `BuildSecrets.gorestToken` constant and returns `null` when blank.
   - Keep this provider isolated from UI code and do not add any hand-written token constants.

2. **Add compile-time local token ingestion**
   - Touch `SliidePeek/shared/build.gradle.kts`.
   - Add a small Gradle source-generation task instead of adding a new dependency/plugin unless implementation proves this too awkward.
   - The task should generate a file such as `SliidePeek/shared/build/generated/build-secrets/commonMain/kotlin/com/sliide/useractivity/config/BuildSecrets.kt` containing:
     - `internal object BuildSecrets { const val gorestToken: String = "..." }`
   - Read the token in this precedence order:
     1. environment variable `GOREST_TOKEN`
     2. `SliidePeek/local.properties` key `gorest.token`
     3. `SliidePeek/.env` line `GOREST_TOKEN=...` or `gorest.token=...`
   - Treat missing values as `""`; the app should still compile and run without write access.
   - Register the generated directory as a `commonMain` Kotlin source directory so both Android and iOS frameworks get the same compile-time value.
   - Ensure the task runs before common metadata/iOS/Android compilation.
   - Touch `SliidePeek/.gitignore` and root `.gitignore` if needed so `.env` is ignored. `local.properties` is already ignored.
   - Optionally add a committed `SliidePeek/.env.example` with `GOREST_TOKEN=` and no real value, or document the local property in README later.

3. **Add create-user DTOs and mapper helpers**
   - Create `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/data/remote/dto/CreateUserRequestDTO.kt` with serialised string fields `name`, `email`, `gender`, `status`.
   - Update or create a mapper under `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/data/remote/mapper/UserMapper.kt` or `CreateUserMapper.kt` to map `CreateUserRequest` to the DTO using GoREST lowercase values.
   - Ensure unknown enum values are not accepted by validation before this mapper is called.

4. **Add authenticated `POST /users` to the API client**
   - Touch `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/data/remote/GorestApiClient.kt`.
   - Add `suspend fun createUser(request: CreateUserRequestDTO, bearerToken: String): UserDTO` using `httpClient.post(baseUrl)`, `appendPathSegments("users")`, `contentType(ContentType.Application.Json)`, `setBody(request)`, and `header(HttpHeaders.Authorization, "Bearer $bearerToken")`.
   - Rely on `expectSuccess = true` so `201`, `401/403`, and `422` flow through existing repository error mapping.

5. **Expose create through the repository**
   - Touch `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/domain/repository/UserRepository.kt` and add `suspend fun createUser(request: CreateUserRequest): AppResult<User>`.
   - Touch `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/data/repository/UserRepositoryImpl.kt`.
   - Inject `BearerTokenProvider` into `UserRepositoryImpl`.
   - If `getToken()` is blank/null, return `AppResult.Failure(AppError.Unauthorized)` with no network call.
   - Otherwise call `apiClient.createUser(...)` and map `UserDTO.toDomain()`.

6. **Add cache support for created users at the top**
   - Touch `SliidePeek/shared/src/commonMain/sqldelight/com/sliide/useractivity/data/local/UserCache.sq`.
   - Add queries such as:
     - `deleteUserById` already exists; reuse it to avoid duplicates.
     - `incrementFeedPositions: UPDATE cached_user_feed SET position = position + 1;`
     - optional `trimFeedToLimit: DELETE FROM cached_user_feed WHERE position >= ?;` if keeping the cached page bounded to metadata `per_page`.
   - Touch `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/data/local/UserCacheDataSource.kt` and add `suspend fun insertCreatedUserAtTop(user: User, createdAtMillis: Long, cachedAtMillis: Long)`.
   - Touch `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/data/local/SqlDelightUserCacheDataSource.kt`.
   - Implement the insert in a transaction: delete existing row with the same id, shift positions, insert created user at position `0`, update/retain metadata, and optionally trim to the cached page size.
   - If metadata does not exist yet, create sensible metadata for a one-page local cache so offline display remains coherent.

7. **Create the add-user use case**
   - Create `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/presentation/users/CreateUserUseCase.kt`.
   - Define an interface, e.g. `interface CreateUserUseCase { suspend operator fun invoke(request: CreateUserRequest): AppResult<UserFeedItem> }`.
   - Inject `UserRepository`, `UserCacheDataSource`, `AppClock`, and `RelativeTimeFormatter`.
   - On success, stamp `createdAtMillis = clock.nowMillis()`, call `insertCreatedUserAtTop`, and return a `UserFeedItem` with `relativeTimestamp = "just now"` from the formatter.
   - On failure, return the repository failure unchanged so the ViewModel can show an appropriate form-level error.

8. **Add form state and validation logic**
   - Create `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/presentation/users/AddUserFormState.kt`.
   - Include fields for `name`, `email`, `gender`, `status`, per-field error strings, `isSubmitEnabled`, `isSubmitting`, and `submitErrorMessage`.
   - Create `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/presentation/users/AddUserFormValidator.kt`.
   - Validate:
     - name is required after trimming.
     - email is required and must match a simple pragmatic email regex.
     - gender is `UserGender.Female` or `UserGender.Male`.
     - status is `UserStatus.Active` or `UserStatus.Inactive`.
   - Keep validation pure and covered by common tests.

9. **Extend `UserFeedViewModel` for add state and events**
   - Touch `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/presentation/users/UserFeedState.kt` and add fields such as `isAddUserVisible`, `addUserForm`, and `highlightedUserId` if needed.
   - Touch `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/presentation/users/UserFeedViewModel.kt`.
   - Inject `CreateUserUseCase`.
   - Add public actions:
     - `openAddUser()`
     - `dismissAddUser()`
     - `onAddUserNameChanged(value)`
     - `onAddUserEmailChanged(value)`
     - `onAddUserGenderSelected(gender)`
     - `onAddUserStatusSelected(status)`
     - `submitAddUser()`
   - Add a one-shot event flow, e.g. `UserFeedEvent.ShowMessage("Maya Reed added")`, for snackbar/toast feedback.
   - On submit: block double-submit, set `isSubmitting`, call `CreateUserUseCase`, then on success close the form, clear submit error, insert the new item at index `0` in `state.users`, set highlight id if implemented, and emit success event.
   - On submit failure: keep the sheet/panel open, preserve all input, clear `isSubmitting`, and set a clear `submitErrorMessage` based on `AppError` (`Unauthorized` should mention that an API token is required for GoREST writes).

10. **Wire Koin dependencies**
   - Touch `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/di/AppModules.kt`.
   - Register `BearerTokenProvider` as `BuildSecretsBearerTokenProvider` by default.
   - Update `UserRepositoryImpl(get(), get())` to receive the token provider.
   - Register `CreateUserUseCase` factory.
   - Update `UserFeedViewModel` factory to pass `createUser = get()`.
   - Keep Koin setup small; do not add Koin Compose or extra DI libraries.

11. **Hook add actions from `AppRoot` into shells**
    - Touch `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/AppRoot.kt`.
    - Collect `userFeedViewModel.events` in a `LaunchedEffect` and expose event state to a `SnackbarHost` if the existing scaffold can support it; otherwise use the smallest local snackbar mechanism needed.
    - Replace no-op `onAddUserClick = {}` with `userFeedViewModel::openAddUser` for both compact and expanded shells.
    - Pass all form callbacks and submit callbacks down through `CompactAppShell` and `ExpandedAppShell`.

12. **Implement reusable add-user form UI**
    - Create `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/users/AddUserForm.kt`.
    - Make it stateless: accept `AddUserFormState` and callbacks for field changes, segment selections, submit, and cancel.
    - Use Material 3 `OutlinedTextField`, segmented-style buttons/filters using existing lightweight patterns, and theme colours.
    - Include inline field errors and a form-level error banner matching UX v2.
    - Disable/lock inputs and show `Adding…` while `isSubmitting` is true.
    - Do not include an `Other` gender segment.

13. **Phone modal bottom sheet**
    - Touch `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/CompactAppShell.kt` or `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/users/UserFeedScreen.kt`.
    - When `state.isAddUserVisible` is true, show a Material 3 modal bottom sheet over the feed.
    - The sheet title should be `Add user`, with dismiss affordance, scrim dismissal, and `Add user` CTA.
    - Keep the existing feed visible behind the sheet as UX v2 shows; do not navigate to a full screen.

14. **Tablet/iPad action panel form mode**
    - Touch `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/ExpandedAppShell.kt` and `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/users/UserActionPanel.kt`.
    - Add an action-panel mode: if `state.isAddUserVisible`, render `AddUserForm` in the right pane instead of selected-user details.
    - Use the existing top-bar `+` in the master pane as the entry point.
    - On cancel, return to the previously selected user panel or empty prompt.
    - Use the extra width to put gender/status selectors side-by-side where simple, but avoid over-polishing.

15. **Immediate feed/cache update after `201`**
    - Ensure `CreateUserUseCase` updates cache only after repository success.
    - Ensure `UserFeedViewModel` inserts the returned `UserFeedItem` at the top immediately after success.
    - If the created id already exists in the visible list, remove the old copy before inserting at top.
    - Keep the count derived from `state.users.size`; no separate count state is needed.

16. **[Human] Provide a GoREST bearer token for real-device/manual success testing**
    - Obtain a GoREST token from the developer’s GoREST account.
    - Add it locally using one of these uncommitted options:
      - shell environment: `export GOREST_TOKEN=...` before running Gradle.
      - `SliidePeek/local.properties`: `gorest.token=...`.
      - `SliidePeek/.env`: `GOREST_TOKEN=...`.
    - Prefer `local.properties` for Android Studio/Gradle local development because the file already exists in typical Android projects and is ignored by git.
    - Do not commit the token, the generated `BuildSecrets.kt`, logs containing the token, rendered AI transcripts containing the token, or screenshots exposing it.
    - Understand the trade-off: this is compile-time ingestion for a coding challenge, so the token is not in the repository but may be embedded in local debug binaries. Do not use a production or personal high-privilege token.
    - If no token is available, verify mocked tests and the UI’s token-required error state instead; document the limitation in the final README/submission notes.

17. **Add tests**
    - Add `SliidePeek/shared/src/commonTest/kotlin/com/sliide/useractivity/presentation/users/AddUserFormValidatorTest.kt` for name/email/gender/status validation and submit-enabled state.
    - Update `SliidePeek/shared/src/commonTest/kotlin/com/sliide/useractivity/data/remote/GorestApiClientTest.kt` to assert `POST /users`, JSON body, `Authorization: Bearer ...`, and parsing the `201` response.
    - Update `SliidePeek/shared/src/commonTest/kotlin/com/sliide/useractivity/data/repository/UserRepositoryImplTest.kt` to cover create success, missing token => `Unauthorized` without a request, and `422` validation mapping.
    - Add or update use-case tests for cache insert-after-create using a fake `UserCacheDataSource`.
    - Extend `SliidePeek/shared/src/commonTest/kotlin/com/sliide/useractivity/presentation/users/UserFeedViewModelTest.kt` for:
      - opening/dismissing the add form.
      - real-time validation updates.
      - disabled submit when invalid.
      - submit loading state.
      - success closes form and inserts new user at top.
      - submit failure keeps input and shows error.

## Verification

Run from `SliidePeek/`:

```bash
./gradlew :shared:allTests
./gradlew :shared:compileKotlinMetadata
./gradlew :androidApp:assembleDebug
```

Manual checks:

- Compact/phone width: tap FAB, bottom sheet appears over the feed, invalid fields show inline errors, submit is disabled until valid, loading prevents double-submit, error preserves input, success closes sheet and inserts the new row at the top with a success snackbar/message.
- Expanded/tablet width: tap the `+` app-bar action, the right action panel becomes the add-user form, cancel returns to selected/empty panel, success inserts the new row at the top of the left feed.
- Token missing: with no `GOREST_TOKEN`, no `gorest.token`, and no `.env` token, valid submit shows a clear token/auth error and does not mutate feed/cache.
- Token supplied locally: add `gorest.token=...` to `SliidePeek/local.properties` or export `GOREST_TOKEN=...`, rebuild so the generated compile-time `BuildSecrets` value is updated, submit the form, confirm GoREST returns `201`, the returned user appears immediately at the top, and after an app restart/offline fallback the cached created user remains coherent.
- Inspect git diff before commit to confirm no bearer token, `.env`, or generated secret file is present.

## Risks

- GoREST write operations require a real bearer token; without one, only mocked tests and the missing-token UI path can be verified.
- Compile-time token ingestion avoids committing secrets but embeds the token in locally built binaries; this is acceptable for the challenge/demo strategy, not production-grade secret storage.
- Compose Multiplatform Material 3 bottom-sheet APIs may vary by version; if `ModalBottomSheet` is unavailable or unstable in the current dependency set, implement a simple custom sheet surface rather than adding dependencies.
- SQLDelight cache position updates must be transaction-safe to avoid duplicate/stale row ordering after create.
- The current app shell does not yet have a global snackbar host; adding events should stay small and not turn into a broader scaffold/navigation rewrite.
- UX v2 shows a gender `Other` option, but GoREST/spec only accepts `male`/`female`; implementation should favour API correctness and note this decision if asked.
