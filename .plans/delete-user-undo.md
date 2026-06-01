# Delete User with Confirmation and Undo (Roadmap 12.7)

## Goal

Implement the official destructive delete flow: long-press a user → confirm → animated optimistic removal → Snackbar with **Undo** → local restore on Undo, remote `DELETE /users/{id}` committed only when the Undo window closes. Local UI/cache stay coherent in all branches (undo, commit success, commit failure). Tablet exposes the same action via the existing action panel.

## Approach summary

The flow is **local-first and optimistic**, mirroring the existing add-user flow:

1. **Trigger** — long-press a feed row (phone) or the panel "Delete user" button (tablet) opens a confirmation dialog.
2. **Confirm** — the row is removed from `UserFeedState.users` immediately and remembered (item + original index). The cache is **not** touched yet.
3. **Undo window** — a Snackbar with an Undo action is shown. The Material 3 Snackbar's own duration is the timer (no separate ViewModel timer), and its result decides the outcome:
   - **Undo tapped** (`SnackbarResult.ActionPerformed`) → re-insert the row at its original index, briefly highlight it, never call the API.
   - **Dismissed/timed-out** (`SnackbarResult.Dismissed`) → commit: call `DELETE /users/{id}` and remove from cache on success.
4. **Commit failure** (incl. no token / `Unauthorized`) → re-insert the row at its original index and show an error Snackbar with **Retry**.

**Cache strategy (key decision):** the local DB is mutated **only on a confirmed successful remote delete**. During the undo window the user still lives in the cache, so Undo and commit-failure require **zero** cache work, and an app kill mid-window safely leaves the user present. This avoids any position-shifting/restore logic in SQLDelight.

**Remote-vs-local undo semantics (documented trade-off):** Undo is purely local and instant — it happens *before* any API call, so there is nothing to reverse on the server. A remote delete is only attempted after the window closes; once GoREST returns `204` it is final and cannot be undone. This is consistent with the spec (§4.3) and the UX note ("Undo is local and instant… If the snackbar times out, the delete is committed to the API"). Document this in `README.md`.

## Codebase findings

Real paths/lines from inspection:

- **API client** — `shared/.../data/remote/GorestApiClient.kt`. Has `getUsers`/`createUser` etc., **no `deleteUser`**. `createUser` (line 43) shows the bearer-token + `appendPathSegments("users")` pattern to copy. Client uses `expectSuccess = true` (`HttpClientFactory.kt:9`) so non-2xx throws `ResponseException`.
- **Repository** — `domain/repository/UserRepository.kt` (interface, lines 10-16, no delete); `data/repository/UserRepositoryImpl.kt`. `createUser` (line 30) reads token via `bearerTokenProvider.getToken() ?: return Failure(Unauthorized)` then `repositoryCall { … }` — mirror this for delete.
- **Error mapping** — `data/repository/RepositoryErrorMapper.kt` `repositoryCall { }`; `domain/AppError.kt` (`Network, Timeout, Unauthorized, NotFound, Validation, Server, Unknown`).
- **Cache** — `data/local/UserCacheDataSource.kt` (interface: `replaceLastPage`, `getLastPageFeed`, `insertCreatedUserAtTop` — **no delete**). `SqlDelightUserCacheDataSource.kt`. `commonMain/sqldelight/.../UserCache.sq` **already defines `deleteUserById` (lines 56-58)** — only needs to be exposed on the data source.
- **Use case template** — `presentation/users/CreateUserUseCase.kt` (interface + `CreateUserUseCaseImpl`, calls repo then `userCacheDataSource.insertCreatedUserAtTop`). New `DeleteUserUseCase` lives beside it.
- **ViewModel** — `presentation/users/UserFeedViewModel.kt`. Optimistic insert at `handleCreateUserSuccess` (lines 125-137), event send via `eventsChannel`, `highlightedUserId` (state line 13) for the add highlight — reuse for restore highlight. `AppError.toUserMessage()`/`toCreateUserMessage()` (lines 187-205) are the message-mapping pattern.
- **State/events** — `presentation/users/UserFeedState.kt` (add `deleteConfirmation`); `UserFeedEvent.kt` (only `ShowMessage` today — add undo/failed variants); `UserFeedItem.kt` (id/name/etc).
- **Feed row** — `ui/users/UserFeedRow.kt`. Uses `Modifier.clickable(onClick)` (line 37) — switch to `combinedClickable` for long-press.
- **Feed screen** — `ui/users/UserFeedScreen.kt`. Renders rows in a `Column` (lines 116-125, **not** a `LazyColumn`); `onUserClick` is threaded through. Add `onUserLongPress`.
- **Action panel (tablet)** — `ui/users/UserActionPanel.kt`. Delete button is present but `enabled = false` (lines 109-113) with a placeholder caption (lines 119-123) — enable it and drop the placeholder line.
- **Shells** — `ui/shell/CompactAppShell.kt` (phone; modal add sheet at lines 86-99), `ui/shell/ExpandedAppShell.kt` (passes `onDeleteUserClick = {}` at line 82). Both receive callbacks from…
- **Root** — `ui/shell/AppRoot.kt`. Owns `SnackbarHostState` (line 41) and the events collector (lines 46-52, only `ShowMessage`). The confirmation dialog + undo/retry snackbars belong here so they overlay both layouts (UX §04: "appear over the whole window").
- **DI** — `di/AppModules.kt`. `factory<CreateUserUseCase>` (line 48) and the `UserFeedViewModel` factory (line 55) show exactly where to add `DeleteUserUseCase` and the new VM constructor arg.
- **Tests** — `commonTest/.../presentation/users/UserFeedViewModelTest.kt` (fakes for load/create use cases, `FixedClock`, `TestScope` — extend with a `FakeDeleteUserUseCase`); `data/remote/GorestApiClientTest.kt` (MockEngine pattern incl. a 204-style handler); `data/repository/UserRepositoryImplTest.kt` (repo success/failure pattern).

## Decisions to lock in first

1. **Cache mutated only on remote success** (see Approach). No SQLDelight changes beyond exposing the existing `deleteUserById`.
2. **Snackbar result drives commit/undo** — no ViewModel-side delay timer. ViewModel exposes pure, individually-testable functions (`requestDeleteUser`, `cancelDeleteUser`, `confirmDeleteUser`, `undoDelete(id)`, `commitDeletion(id)`, `retryDelete(id)`); the UI decides *when* to call commit vs undo from `SnackbarResult`.
3. **One pending deletion is tracked per user** in a private `Map<Long, PendingDeletion>` (item + original index). Sequential deletes each get their own snackbar/commit; a replaced snackbar returns `Dismissed` → commits the prior one naturally.
4. **Undo restores to the exact original index** (UX §03), not the top.
5. **No-token / `Unauthorized` is treated as a normal commit failure** → restore + Retry snackbar, same as add-user requires a token. Documented, not silently swallowed.
6. **Animation scope for 12.7**: wrap each row in `AnimatedVisibility` with fade+shrink so removal/restore animate. The countdown progress bar, haptics, ripple polish, and `animateItemPlacement` are **deferred to 12.8** (per "no hi-fi polish beyond what's necessary").

> This plan has no `[Human]` steps. The GoREST bearer token is already handled by the existing `BuildSecretsBearerTokenProvider`; delete simply reuses it. If no token is configured, the documented fallback (local removal + "couldn't delete — restored" Retry) applies automatically.

## Implementation order

### 1. Data layer — remote delete
- **`data/remote/GorestApiClient.kt`**: add
  ```kotlin
  suspend fun deleteUser(id: Long, bearerToken: String) {
      httpClient.delete(baseUrl) {
          url { appendPathSegments("users", id.toString()) }
          header(HttpHeaders.Authorization, "Bearer $bearerToken")
      }
  }
  ```
  (import `io.ktor.client.request.delete`). No `.body()` — `204` has no content; `expectSuccess` makes non-2xx throw.

### 2. Data layer — repository delete
- **`domain/repository/UserRepository.kt`**: add `suspend fun deleteUser(id: Long): AppResult<Unit>`.
- **`data/repository/UserRepositoryImpl.kt`**: implement, mirroring `createUser`:
  ```kotlin
  override suspend fun deleteUser(id: Long): AppResult<Unit> {
      val token = bearerTokenProvider.getToken() ?: return AppResult.Failure(AppError.Unauthorized)
      return repositoryCall { apiClient.deleteUser(id, token) }
  }
  ```

### 3. Data layer — cache delete
- **`data/local/UserCacheDataSource.kt`**: add `suspend fun deleteUser(id: Long)`.
- **`data/local/SqlDelightUserCacheDataSource.kt`**: implement using the existing query:
  ```kotlin
  override suspend fun deleteUser(id: Long) {
      queries.deleteUserById(id)
  }
  ```
  No `.sq` change required.

### 4. Domain/presentation — DeleteUserUseCase
- **New `presentation/users/DeleteUserUseCase.kt`** (beside `CreateUserUseCase.kt`):
  ```kotlin
  interface DeleteUserUseCase {
      suspend operator fun invoke(id: Long): AppResult<Unit>
  }
  class DeleteUserUseCaseImpl(
      private val userRepository: UserRepository,
      private val userCacheDataSource: UserCacheDataSource,
  ) : DeleteUserUseCase {
      override suspend fun invoke(id: Long): AppResult<Unit> =
          when (val result = userRepository.deleteUser(id)) {
              is AppResult.Success -> { userCacheDataSource.deleteUser(id); result }
              is AppResult.Failure -> result
          }
  }
  ```
  Cache removal happens only after remote success — matches the cache strategy.

### 5. State + events
- **`presentation/users/UserFeedState.kt`**: add `val deleteConfirmation: UserFeedItem? = null` (user awaiting the confirm dialog). Reuse existing `highlightedUserId` for the restored-row highlight. (`pendingDeletions` is internal VM bookkeeping, not UI state.)
- **`presentation/users/UserFeedEvent.kt`**: extend
  ```kotlin
  sealed interface UserFeedEvent {
      data class ShowMessage(val message: String) : UserFeedEvent
      data class ShowUndoDelete(val userId: Long, val message: String) : UserFeedEvent
      data class ShowDeleteFailed(val userId: Long, val message: String) : UserFeedEvent
  }
  ```

### 6. ViewModel logic
- **`presentation/users/UserFeedViewModel.kt`**: add `deleteUser: DeleteUserUseCase` constructor param. Private `data class PendingDeletion(val item: UserFeedItem, val index: Int)` and `private val pendingDeletions = mutableMapOf<Long, PendingDeletion>()`. Add:
  - `requestDeleteUser(id)` → set `deleteConfirmation` to the matching `users` item (no-op if not found).
  - `cancelDeleteUser()` → clear `deleteConfirmation`.
  - `confirmDeleteUser()` → read `deleteConfirmation`; capture `index = users.indexOfFirst { it.id == id }`; store `pendingDeletions[id]`; remove from `users`; clear `deleteConfirmation`; clear `highlightedUserId` if it was this id; `eventsChannel.send(ShowUndoDelete(id, "${item.name} deleted"))`.
  - `undoDelete(id)` → pop `pendingDeletions[id]`; re-insert `item` at `min(index, users.size)`; set `highlightedUserId = id`. No API call.
  - `commitDeletion(id)` → look up pending (return if absent); `scope.launch { when (deleteUser(id)) { Success -> pendingDeletions.remove(id); Failure(e) -> { restore item at index; pendingDeletions.remove(id); send(ShowDeleteFailed(id, e.toDeleteUserMessage())) } } }`.
  - `retryDelete(id)` → if the user is currently visible, re-run the optimistic remove + `ShowUndoDelete` path for that id (gives the user another undo window); implement as `requestDeleteUser`-then-`confirmDeleteUser` style without the dialog.
  - Add `AppError.toDeleteUserMessage()` mirroring `toCreateUserMessage()` (e.g. `Unauthorized -> "GoREST API token required to delete users. Add it locally and rebuild."`, default `"Couldn’t delete — restored."`).
- Guard against double-commit: `commitDeletion`/`undoDelete` are no-ops if `pendingDeletions[id]` is already gone, so a late Undo tap after commit is safe.

### 7. DI wiring
- **`di/AppModules.kt`**: add
  ```kotlin
  factory<DeleteUserUseCase> { DeleteUserUseCaseImpl(userRepository = get(), userCacheDataSource = get()) }
  ```
  and pass `deleteUser = get()` into the `UserFeedViewModel { … }` factory (line 55-62).

### 8. Feed row — long-press
- **`ui/users/UserFeedRow.kt`**: add `onLongClick: (() -> Unit)? = null`; replace `Modifier.clickable(onClick = onClick)` with
  ```kotlin
  Modifier.combinedClickable(onClick = onClick, onLongClick = onLongClick)
  ```
  `@OptIn(ExperimentalFoundationApi::class)`. Keep tap = select.

### 9. Feed screen — thread long-press + animate
- **`ui/users/UserFeedScreen.kt`**: add `onUserLongPress: (Long) -> Unit` param; pass `onLongClick = { onUserLongPress(user.id) }` to `UserFeedRow`. Wrap each row in `AnimatedVisibility(visible = true, enter = fadeIn()+expandVertically(), exit = fadeOut()+shrinkVertically())` keyed by `user.id` so removal/restore animate (the `Column` at lines 116-125 stays; rely on enter/exit, not item placement).

### 10. Phone shell — confirm dialog + long-press
- **`ui/shell/CompactAppShell.kt`**: add `onUserLongPress`, plus delete callbacks are handled centrally in `AppRoot` (see step 12). Thread `onUserLongPress` into `UserFeedScreen`.

### 11. Tablet shell — enable panel delete
- **`ui/users/UserActionPanel.kt`**: set the Delete button `enabled = true`, keep `onClick = { onDeleteUserClick(user.id) }`, remove the placeholder caption text (lines 119-123). Drop the now-unused "Add user" secondary button label change only if needed (leave add behaviour intact).
- **`ui/shell/ExpandedAppShell.kt`**: replace `onDeleteUserClick = {}` (line 82) with the threaded `onDeleteUserClick` from `AppRoot`. Also thread `onUserLongPress` into its `UserFeedScreen`.

### 12. Root — dialog + undo/retry snackbars (shared overlay)
- **`ui/shell/AppRoot.kt`**:
  - Pass new callbacks down both shells: `onUserLongPress = userFeedViewModel::requestDeleteUser`, `onDeleteUserClick = userFeedViewModel::requestDeleteUser`.
  - Render the confirmation `AlertDialog` when `userFeedState.deleteConfirmation != null`: title `"Delete ${name}?"`, body `"This removes them from your feed. You can undo right after."`, confirm = `confirmDeleteUser()` (error-coloured), dismiss = `cancelDeleteUser()`. UX §03: Cancel left, Delete right.
  - Extend the events collector. To avoid one suspending snackbar blocking later events, handle each in its own `launch`:
    ```kotlin
    is ShowUndoDelete -> launch {
        val r = snackbarHostState.showSnackbar(event.message, actionLabel = "Undo", duration = SnackbarDuration.Long)
        if (r == SnackbarResult.ActionPerformed) userFeedViewModel.undoDelete(event.userId)
        else userFeedViewModel.commitDeletion(event.userId)
    }
    is ShowDeleteFailed -> launch {
        val r = snackbarHostState.showSnackbar(event.message, actionLabel = "Retry", duration = SnackbarDuration.Long)
        if (r == SnackbarResult.ActionPerformed) userFeedViewModel.retryDelete(event.userId)
    }
    ```
  - Keep `ShowMessage` as-is.

### 13. Tests
- **`commonTest/.../presentation/users/UserFeedViewModelTest.kt`**: add a `FakeDeleteUserUseCase` (configurable success/failure + records ids) and cover:
  - `requestDeleteUser` sets `deleteConfirmation`; `cancelDeleteUser` clears it and leaves `users` untouched.
  - `confirmDeleteUser` removes the row immediately, clears `deleteConfirmation`, emits `ShowUndoDelete`.
  - `undoDelete` re-inserts at the **original index** (delete a middle user, assert order restored) and sets `highlightedUserId`; asserts the delete use case was **not** invoked.
  - `commitDeletion` success → use case called once, row stays gone, pending cleared.
  - `commitDeletion` failure (`Server`/`Unauthorized`) → row re-inserted at original index, `ShowDeleteFailed` emitted with the mapped message.
  - Edge: `undoDelete`/`commitDeletion` after the pending entry is gone → no-op, use case not called twice (guards double-commit).
  - Edge: confirm-delete of the only user → list empty → `isEmpty` true; undo restores → not empty.
  - Use the existing `events` collection style (collect into a list) for event assertions.
- **`commonTest/.../data/remote/GorestApiClientTest.kt`**: add a test that `deleteUser` issues `DELETE /public/v2/users/{id}` with `Authorization: Bearer …` and succeeds on a `204 No Content` MockEngine handler.
- **`commonTest/.../data/repository/UserRepositoryImplTest.kt`**: add `deleteUser` success (token present → `Success`) and `Unauthorized` (token null → `Failure(Unauthorized)`, API not called) cases, plus a non-2xx → mapped `AppError` case.
- (Optional) a small `DeleteUserUseCase` test asserting cache delete is called only on remote success.

## Verification

From `SliidePeek/`:

- `./gradlew :shared:compileKotlinIosX64 :shared:compileDebugKotlinAndroid` (or the project's usual compile tasks) — shared code compiles on both targets.
- `./gradlew :shared:testDebugUnitTest` (Android host) and/or `:shared:allTests` — all new + existing tests pass.
- `./gradlew :shared:check` for lint/format if configured.
- Manual (phone): long-press a row → dialog → Delete → row animates out + Undo snackbar → tap Undo → row returns to original position, highlighted; repeat and let the snackbar time out → confirm the user is gone after reload (committed). With no token configured, confirm the "couldn’t delete — restored" Retry snackbar appears and the row returns.
- Manual (tablet/expanded width): select a user → panel "Delete user" button is enabled → same confirm + undo flow overlays the whole window.

## Risks

- **Token availability**: without a GoREST token, every remote delete fails with `Unauthorized` → restore + Retry. This is the documented fallback (consistent with add-user) but means delete never persists remotely in token-less demos. Call this out in `README.md`.
- **Snackbar-driven commit edge cases**: a snackbar dismissed by a *newer* snackbar also returns `Dismissed` and commits the prior delete — intended, but verify multiple rapid deletes behave (each pending committed once). The double-commit guard (step 6) covers late Undo taps.
- **App killed during undo window**: commit coroutine is cancelled, so the user is neither remotely deleted nor cache-removed → reappears on next load. Acceptable and coherent; note it.
- **Column vs LazyColumn**: the feed uses a plain `Column`, so `animateItemPlacement` isn't available; `AnimatedVisibility` enter/exit is sufficient for 12.7. If the feed is later migrated to `LazyColumn` (12.8 polish), revisit item-placement animation.
- **GoREST data volatility**: the public sandbox periodically purges data, so a previously-fetched user id may already be gone server-side → `404`. Treat `NotFound` on commit as effectively deleted (optionally map to success-equivalent) or surface as a failure; keep the local state coherent either way. Decide during step 6 and note in tests.
