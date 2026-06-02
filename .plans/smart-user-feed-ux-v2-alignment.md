# Smart User Feed UX v2 Alignment Plan

**Status:** Closed — implemented and verified. UX v2 alignment delivered row content placement, FAB/add seams, shimmer geometry alignment, refreshing/API/no-internet/offline-cached states, and tablet action-panel placeholder while keeping add-user, delete/undo, persistence, and hi-fi polish out of scope. Automated verification passed and Android/iOS manual checks were confirmed by the user.

## Goal

Align the completed 12.4A smart user feed with `design/ux_v2.html` without rewriting the feed architecture. This pass should keep the existing last-page fetch, shared relative timestamp logic, and `UserFeedViewModel`, while adjusting the basic UI/state seams for the UX v2 feed states and removing obvious UX v1 shell assumptions that conflict with the re-scoped user-management app.

## Codebase findings

- `AGENTS.md:18-28` says roadmap item 12.4 onward should prioritise the official challenge: smart user feed, relative timestamp, add/delete/offline later, Koin where practical, and high-fidelity polish without unnecessary SDK complexity.
- `PROJECT_SPEC.md:83-111` requires the smart feed to fetch the last `/users` page, display name, email, and relative timestamp from shared KMP logic, and support shimmer, error, no-internet/offline, empty, refresh, and cached/offline display states.
- `PROJECT_SPEC.md:113-153` requires add-user to launch from a FAB, but the full add flow is a later roadmap item; this alignment pass should add only the visible add-action seam.
- `design/ux_v2.html:245-255` explicitly re-scopes the app to Smart Feed, Add User, Delete + Undo, and Offline states, while cutting user posts, post detail, comments, todos, and activity drill-down from the primary UX.
- `design/ux_v2.html:268-269` defines the phone smart feed as the app home and says each row carries name, email, a relative “last active” timestamp, and a status chip; a FAB adds a user; shimmer placeholders mirror row geometry; empty/error/offline/refreshing share one stable shell.
- `design/ux_v2.html:288` says loading should show shimmer rows with the FAB still present.
- `design/ux_v2.html:343` defines the API error state as soft and retry-first.
- `design/ux_v2.html:353` says the FAB is the one persistent primary action across populated/loading feed states.
- `design/ux_v2.html:361-376` distinguishes no-internet/no-cache from API error: “You’re offline”, no cached data, and a retry action.
- `design/ux_v2.html:379-394` defines offline-with-cache as the feed still visible with a persistent banner: “Offline — showing cached users” plus an “updated N ago” timestamp.
- `design/ux_v2.html:397-411` defines refreshing as existing rows staying visible, with a “Refreshing…” banner and refresh affordance state rather than replacing rows with shimmer.
- `design/ux_v2.html:415-419` reinforces that cached-vs-no-cache offline states are distinct and refresh should not wipe existing rows.
- `design/ux_v2.html:701-704` changes tablet from UX v1 activity drill-down to `Feed | Action Panel`: the feed stays persistent; the right pane is an action panel for selected user, add form, or empty prompt; selecting never pushes a full-screen page.
- `design/ux_v2.html:711-719` shows tablet list app bar with both plus and refresh icons, and compact feed rows with name, email, and short relative time.
- `design/ux_v2.html:731-746` shows the selected-user tablet action panel with user details and a visible Delete action, but delete implementation belongs to a later roadmap item.
- `design/ux_v2.html:806-825` identifies reusable components for this pass: user row, status chips, and FAB. The FAB is bottom-right on phone and becomes `＋` in the app bar on tablet.
- `design/ux_v2.html:851-853` defines the offline banner as persistent and carrying last-updated time.
- `design/ux_v2.html:924` says refresh should spin the app-bar icon, keep existing rows in place, and bounce back to cached banner when offline; animation polish can wait.
- `.plans/smart-user-feed.md` scoped 12.4A to structure/shared logic first and explicitly deferred add-user, delete/undo, Koin, SQLDelight/Room persistence, and final high-fidelity styling.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/presentation/users/GetSmartUserFeedUseCase.kt:19-38` already implements last-page behaviour and attaches local `fetchedAtMillis` to the returned users.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/domain/time/RelativeTimeFormatter.kt:3-18` currently formats timestamps as `Just now`, `5 minutes ago`, `1 hour ago`, etc. UX v2 examples use shorter/lowercase feed copy such as `just now`, `5 min ago`, `2 h ago`, and `1 d ago`.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/presentation/users/UserFeedState.kt:3-13` currently tracks loading, refreshing, users, error message, offline message, and retry, but does not expose last-updated/fetched-at display for an offline cached banner.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/presentation/users/UserFeedViewModel.kt:25-35` correctly keeps existing users during refresh/retry attempts, and `UserFeedViewModel.kt:61-70` already keeps users visible on offline failures by setting `offlineMessage` without clearing `users`.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/users/UserFeedScreen.kt:29-35` maps no-internet/no-cache to generic `ContentState.Error`, which makes it visually/copy-wise too close to API error for UX v2.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/users/UserFeedScreen.kt:43-55` renders offline, inline error, and refreshing as plain text/simple surfaces rather than a shared stable banner model.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/users/UserFeedScreen.kt:56-66` uses generic copy: `No users yet`, `Refresh to check...`, and `Could not load users`; UX v2 wants empty to invite Add, API error to say “Couldn’t load users”, and no-internet/no-cache to say “You’re offline”.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/users/UserFeedScreen.kt:67-75` renders only rows; it does not render a FAB/add seam.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/users/UserFeedRow.kt:45-80` renders avatar, then name/email/timestamp in the middle column, and a status chip on the right. UX v2 phone row wants timestamp/right-side “last active” grouped with the status chip.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/components/Skeleton.kt:27-67` renders avatar, two text bars, and one chip block. It does not mirror the UX v2 row exactly because the real row needs a right stack for status chip and relative time.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/components/AppTopBar.kt:23-30` supports only back and refresh actions. UX v2 tablet needs a plus action in the app bar; phone needs a feed FAB.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/CompactAppShell.kt:37-52` still navigates from a feed row to UX v1-style `UserDetailPlaceholder` with posts/todos affordances. UX v2 phone feed does not use activity drill-down as a primary path.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/ExpandedAppShell.kt:50-63` renders only refresh in the tablet feed app bar and uses the feed in the left pane, but has no tablet plus action.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/ExpandedAppShell.kt:69-97` still routes selected users into UX v1 placeholders for posts, todos, and post detail. UX v2 wants the right pane to be a user-management action panel, not an activity drill-down.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/RoutePlaceholders.kt:111-139` and `RoutePlaceholders.kt:142-217` contain profile/posts/todos/post/comment placeholders that are now non-core for UX v2. They can remain for optional future work but should not be reachable from the primary smart feed path.

## Approach summary

Keep 12.4A’s data and state-holder architecture intact. Make the smallest UI/state changes needed for the feed to match UX v2 structure: row content placement, feed FAB/add seam, banner states, no-internet-vs-API-error copy, refreshing behaviour, and skeleton geometry. For tablet, stop presenting UX v1 activity drill-down as the selected-user experience and replace it with a minimal action-panel placeholder backed by the selected `UserFeedItem`. The add and delete actions should be visible but non-functional seams or TODO callbacks only; full flows remain later roadmap items.

## Implementation order

1. **Adjust relative timestamp display for feed copy**
   - Touch: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/domain/time/RelativeTimeFormatter.kt`.
   - Touch: `SliidePeek/shared/src/commonTest/kotlin/com/sliide/useractivity/domain/time/RelativeTimeFormatterTest.kt`.
   - Decide whether to change the existing formatter globally to UX v2 feed copy (`just now`, `5 min ago`, `2 h ago`, `1 d ago`) or add a small display mode/default suitable for the feed.
   - Prefer the simplest option unless other screens need long-form copy. Current usage is the feed only, so changing the formatter globally is acceptable.
   - Keep this in shared KMP logic; do not format relative time inside composables.

2. **Add a last-updated/offline-cache display seam to feed state**
   - Touch: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/presentation/users/UserFeedState.kt`.
   - Touch: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/presentation/users/UserFeedViewModel.kt`.
   - Add a lightweight state field such as `lastUpdatedLabel: String?` or `cachedAtRelativeLabel: String?`, derived from loaded users’ `fetchedAtMillis`/`relativeTimestamp` for now.
   - On successful load/refresh, populate this field from the returned feed data.
   - On offline failure with existing in-memory users, preserve users and last-updated label so the banner can say `Offline — showing cached users · updated 12 min ago` or the nearest available copy.
   - Do not add durable cache or database persistence in this plan.

3. **Introduce feed action callbacks without implementing add-user**
   - Touch: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/users/UserFeedScreen.kt`.
   - Touch: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/CompactAppShell.kt`.
   - Touch: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/ExpandedAppShell.kt`.
   - Touch: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/components/AppTopBar.kt` if adding a tablet `onAdd` slot there is cleaner.
   - Add an `onAddUserClick: () -> Unit` parameter through the feed/shell path.
   - Phone: render a simple Material 3 FAB or current-theme surface button at bottom-right of the feed for loading, populated, empty, refreshing, and offline-with-cache states. It may no-op for now or call a shell-level placeholder callback.
   - Tablet: add a `＋` app-bar action next to refresh in the left feed pane; do not open a real add form yet.
   - Do not implement form state, validation, POST, auth, snackbar, or insertion logic.

4. **Align user row content and placement**
   - Touch: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/users/UserFeedRow.kt`.
   - Keep the row stateless and theme-based.
   - Phone/default row should show avatar, name, email, and a right-side vertical stack containing status chip above relative last-active text.
   - Keep status as chip + label; do not rely on colour alone.
   - Consider adding a `compact: Boolean = false` parameter so tablet master rows can use shorter geometry if needed, but only if this avoids cramped status chips in the 330dp list pane.
   - Avoid final typography/elevation polish; only align structure.

5. **Align shimmer/skeleton geometry**
   - Touch: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/components/Skeleton.kt`.
   - Make `SkeletonRow` mirror the real feed row: avatar circle, two text lines in the middle, and a right-side stack with chip-sized block plus a small timestamp block.
   - Keep the existing lightweight alpha animation if easier; gradient shimmer/high-fidelity animation can wait for the polish pass.
   - Ensure loading screen still shows the feed FAB as UX v2 requires.

6. **Replace ad-hoc state messages with UX v2 feed states**
   - Touch: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/users/UserFeedScreen.kt`.
   - Add small private composables if useful, for example `FeedStatusBanner`, `NoInternetState`, and `ApiErrorState`.
   - API error with no users: title `Couldn’t load users`, retry-first copy, retry action.
   - No internet/no cache: title `You’re offline`, body communicating no connection and nothing cached yet, retry action.
   - Empty: title `No users yet`, body should invite add-user, and primary action should be `Add user` wired to the add seam rather than `Refresh`.
   - Offline with users: show persistent banner above rows, e.g. `Offline — showing cached users` plus `updated <relative>` if available.
   - Refreshing with users: show a banner-style `Refreshing…` indicator above rows; do not replace rows with shimmer.
   - Refreshing with no users may continue to use initial loading shimmer.
   - Keep copy close to UX v2 but avoid illustration/high-fidelity styling.

7. **Remove UX v1 activity drill-down from the primary feed path**
   - Touch: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/CompactAppShell.kt`.
   - Touch: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/ExpandedAppShell.kt`.
   - For compact phone, do not navigate a normal feed row tap into posts/todos/profile placeholders. Either make row tap a no-op for now or keep only lightweight selection state if useful for future delete/add work.
   - For expanded tablet, selecting a row should update the right pane in place, as it already does structurally, but the right pane content should stop showing posts/todos/post detail from UX v1.
   - Do not delete optional placeholder files unless they are trivial to disconnect; simply stop exposing them from the smart feed path.

8. **Add a minimal tablet selected-user action panel placeholder**
   - Create or touch: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/users/UserActionPanel.kt`.
   - Touch: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/ExpandedAppShell.kt`.
   - The panel should accept the selected `UserFeedItem?` and callbacks for future `onAddUserClick`/`onDeleteUserClick` seams.
   - Empty panel: friendly prompt to select a user or use add action.
   - Selected panel: show avatar/name/email/status/gender/last-active labels and a visible disabled or no-op `Delete user` action seam. Do not implement delete confirmation/API/undo.
   - If selected ID no longer exists in `userFeedState.users` after refresh, clear selection or fall back to the empty panel.

9. **Update tests for copy/state changes**
   - Touch: `SliidePeek/shared/src/commonTest/kotlin/com/sliide/useractivity/domain/time/RelativeTimeFormatterTest.kt`.
   - Touch: `SliidePeek/shared/src/commonTest/kotlin/com/sliide/useractivity/presentation/users/UserFeedViewModelTest.kt`.
   - Update relative timestamp expectations to UX v2 feed copy if changed.
   - Add/adjust ViewModel tests for preserving `lastUpdatedLabel`/cached indicator after offline refresh failure with existing users.
   - Keep UI visual tests out of scope; common presentation tests are enough for this alignment pass.

10. **Update README with UX v2 alignment note**
    - Touch: `SliidePeek/README.md`.
    - Add a short note under the 12.4 section that UX v2 alignment connected the feed to the re-scoped user-management UX: FAB seam, feed rows, state banners, no-internet vs API error, and tablet action-panel placeholder.
    - Explicitly keep full add-user, delete/undo, durable cache, and high-fidelity polish marked as later work.

## Verification

Run from `SliidePeek/`:

- `./gradlew :shared:compileKotlinMetadata` succeeds.
- `./gradlew :shared:allTests` succeeds.
- `./gradlew :androidApp:assembleDebug` succeeds.
- `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64` succeeds on macOS with iOS toolchain installed.
- Manual compact check: Users feed shows row content as name/email plus right-side status chip and relative last active; FAB is visible; loading still shows skeleton rows plus FAB.
- Manual state check with fake/error path where practical: API error shows retry-first API copy; no-internet/no-cache shows offline-specific copy; offline with existing in-memory users shows a persistent cached/offline banner; refresh keeps existing rows visible.
- Manual tablet check: left feed has plus and refresh actions; selecting a row updates a right-side action-panel placeholder rather than posts/todos/activity drill-down.
- Scope review: no POST add-user implementation, delete/undo implementation, SQLDelight/Room persistence, Koin setup, bearer-token handling, or high-fidelity animation/polish was added.

## Risks

- UX v2 references offline cached users with `updated N ago`, but durable cache is not implemented yet. This pass can only represent in-memory cached/last-successful users; persistent offline support remains roadmap 12.5.
- Adding a visible FAB before the add flow exists creates a discoverability seam that does not yet complete the action. Keep the callback no-op or placeholder-only and document that the real flow is roadmap 12.6.
- Removing UX v1 drill-down from the primary feed path may leave posts/todos placeholder code unused. That is acceptable; do not spend time deleting optional exploratory code unless it causes confusion or compile issues.
- Tablet action-panel content will be structurally aligned but not final: delete/add actions are seams only and visual polish belongs to later design/high-fidelity passes.
- If compact row status chip plus timestamp is cramped on small screens, use a responsive/compact row variant rather than changing the required row content model.
