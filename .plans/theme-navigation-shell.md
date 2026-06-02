# Theme, Navigation, and App Shell Implementation Plan

## Goal

Establish the structural UX framework from `design/ux_v1.html`: phone push/stack navigation, iPad/tablet 2-pane shell, a small typed route model, reusable layout/state containers, and light/dark theme foundations. Keep this roadmap item low/mid-fi and structural; do not implement high-fidelity visual polish beyond what is needed to support the UX flow.

## Codebase findings

- `AGENTS.md:18-28` requires the app to stay simple, clean, minimalist, KMP-first, Compose Multiplatform-based, suitable for Android/iPhone/iPad, and free from unnecessary production-SDK complexity.
- `PROJECT_SPEC.md:269-281` requires a clean Compose UI with consistent spacing, reusable rows/cards, clear navigation, loading/error/empty states, feedback, and responsive layouts.
- `PROJECT_SPEC.md:287-303` requires light/dark mode via Compose Material theming, system theme by default, accessible colours, and theme values instead of hardcoded screen colours.
- `PROJECT_SPEC.md:346-385` requires iPad/tablet adaptive master-detail behaviour instead of stretched phone layouts.
- `PROJECT_SPEC.md:802-812` defines roadmap item 12.2 scope: light/dark theme, basic navigation model, phone-safe layout shell, iPad/tablet adaptive layout strategy, and reusable loading/error/empty containers.
- `design/ux_v1.html:190-198` frames the wireframes as low/mid-fi “structure first”; phone uses push/stack navigation, iPad uses adaptive 2-pane master-detail, amber is only for selected/active states, and hi-fi light/dark comes later.
- `design/ux_v1.html:202-205` defines the User List as the entry screen with name/email/status, subtle gender marker, active/inactive filter under the title, and shimmer placeholders matching real row geometry.
- `design/ux_v1.html:213-239` shows the phone User List app bar with refresh action, segmented filter (`All`, `Active`, `Inactive`), and rows containing avatar initials, name, email, and status chip.
- `design/ux_v1.html:245-279` distinguishes empty and error states: empty has a recovery action such as “Show all users”; error is retry-first.
- `design/ux_v1.html:282-289` explicitly says skeleton rows should avoid layout jumps, status must not rely on colour alone, empty/error are distinct, and filtering should be a visible 3-way segmented control.
- `design/ux_v1.html:295-298` defines phone drill-down as push/stack navigation, with back drilling up one level.
- `design/ux_v1.html:305-327` shows User Detail as one scroll: profile header, status/gender chips, then stacked Posts and Todos sections with section headers that can act as see-all affordances.
- `design/ux_v1.html:338-355` shows Post Detail as a pushed route containing full post body followed by comments.
- `design/ux_v1.html:363-389` shows comments loading independently inside the post route and reusing the centered state block for no comments/comment error.
- `design/ux_v1.html:396-399` defines tablet as persistent list on the left and detail on the right, with selection updating detail in place rather than pushing full-screen.
- `design/ux_v1.html:407-448` shows iPad landscape as `Users list | User detail`, with selected user highlighted and Posts/Todos side-by-side in the detail pane.
- `design/ux_v1.html:451-478` shows iPad portrait keeping the same 2-pane model with a narrower list and stacked detail content.
- `design/ux_v1.html:481-488` defines adaptive rules: fixed list width around 320pt, selected row uses amber keyline plus tint, Posts/Todos are side-by-side on wide panes but stack on phone/portrait, and post depth can later be a third pane or sheet.
- `design/ux_v1.html:494-558` identifies reusable structural components: user row, status chips, todo row, post preview card, comment card, shimmer placeholder, empty state block, error/retry block, and filter segmented control.
- `.agents/skills/compose-ui/skill.md:25-30` says UI should be simple, stateless where practical, callback-driven, theme-based, and avoid reusable components until duplication is real.
- `.agents/skills/compose-ui/skill.md:60-66` requires dark mode support, system theme by default, `MaterialTheme.colorScheme`/typography, and understated styling.
- `.agents/skills/compose-ui/skill.md:81-83` recommends a simple Compose Multiplatform navigation approach without building a large custom navigation framework.
- `.agents/skills/compose-ui/skill.md:97-103` requires compact stacked navigation and medium/expanded master-detail behaviour that respects safe areas and orientation changes.
- `SliidePeek/settings.gradle.kts:31-32` includes only `:androidApp` and `:shared`; roadmap 12.2 should remain inside shared UI unless a platform safe-area issue is discovered.
- `SliidePeek/shared/build.gradle.kts:42-50` already includes Compose runtime, foundation, Material3, UI, resources, preview, lifecycle ViewModel Compose, and lifecycle runtime Compose dependencies; no new dependency is expected for this structural shell.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/App.kt:21-56` currently contains the shared app entry point and a centered foundation placeholder.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/theme/AppTheme.kt:6-8` currently wraps `MaterialTheme` without explicit light/dark colour schemes or system-dark handling.
- `SliidePeek/androidApp/src/main/kotlin/com/sliide/useractivity/MainActivity.kt:10-24` launches the shared `App()` composable.
- `SliidePeek/shared/src/iosMain/kotlin/com/sliide/useractivity/MainViewController.kt:5` hosts the shared `App()` composable for iOS.

## Approach summary

Use the wireframes to lock in structure, not final paint. Keep `App()` as the platform entry point and move root composition into shared `ui/shell`. Add a small typed route model plus in-memory navigation controller/state suitable for previews and early feature integration. The shell should choose compact phone push/stack or expanded 2-pane master-detail from available width. Reusable components should be low/mid-fi versions of the wireframe kit, using Material theme roles and neutral tokens so high-fidelity styling can replace them later.

## Implementation order

1. **Create light/dark theme foundations**
   - Touch: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/theme/AppTheme.kt`.
   - Add `darkTheme: Boolean = isSystemInDarkTheme()` and explicit `lightColorScheme`/`darkColorScheme`.
   - Include neutral roles aligned to the wireframe intent: background/surface, onBackground/onSurface, outline/outlineVariant, primary for muted active/selected accent, error.
   - Keep the muted amber selection/active token as a theme-level structural colour, not hardcoded inside screens.
   - Avoid final brand typography, custom fonts, elevation tuning, or “Mindera Fintech” hi-fi token work in this roadmap item.

2. **Refactor the app entry point into a root shell**
   - Touch: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/App.kt`.
   - Create: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/AppRoot.kt`.
   - Keep `App()` as `AppTheme { AppRoot() }` so Android and iOS platform files continue to call the same entry point.
   - Put remembered navigation state in `AppRoot()` for now; later feature ViewModels can be injected below this shell.

3. **Define the route model and navigation stack**
   - Create: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/navigation/AppRoute.kt`.
   - Create: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/navigation/AppNavigator.kt`.
   - Model structural routes from the UX:
     - `Users`
     - `UserDetail(userId: Long)`
     - `UserPosts(userId: Long)` for the section “see all” affordance
     - `UserTodos(userId: Long)` for the section “see all” affordance
     - `PostDetail(userId: Long? = null, postId: Long)` because post detail appears after user context but may later be opened independently
   - Implement a minimal stack API for phone: `currentRoute`, `canGoBack`, `navigate(route)`, `goBack()`, `resetToUsers()`.
   - Add expanded-selection helpers if useful: selected user ID and selected post route should update panes without forcing a full-screen push.
   - Do not add deep links, URL parsing, saved-state serialization, or a navigation framework unless later roadmap items require it.

4. **Add adaptive window/layout classification**
   - Create: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/AppLayout.kt`.
   - Use common Compose APIs such as `BoxWithConstraints` to classify width into compact vs expanded.
   - Compact/phone: stack navigation matching `design/ux_v1.html:295-298`.
   - Expanded/tablet: persistent 2-pane master-detail matching `design/ux_v1.html:396-399`.
   - Use provisional structural constants: compact below roughly 700dp, list pane around 300-320dp on landscape, narrower list around 240-260dp when width is constrained.
   - Keep thresholds and widths centralized and easy to change after real device review.

5. **Create reusable app layout containers**
   - Create: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/AppScaffold.kt`.
   - Create: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/components/AppTopBar.kt`.
   - Create: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/components/SectionHeader.kt`.
   - Provide low/mid-fi containers matching the wireframe structure:
     - top app bar with title, optional count/subtitle, optional back, optional refresh action;
     - safe content surface using `safeContentPadding()`;
     - scroll/body padding container;
     - section header with title, count, divider, and optional chevron/see-all callback.
   - Use text buttons or simple Material icons/text for back/refresh if no icon set exists; do not add an icon dependency just for polish.

6. **Implement compact phone shell host**
   - Create: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/CompactAppShell.kt`.
   - Render exactly one route at a time from the stack.
   - Root `Users` shows top bar title “Users”, optional count placeholder, refresh action placeholder, and a placeholder user-list content area.
   - Non-root routes show a back affordance and route-specific titles (`Profile`, `Posts`, `Todos`, `Post`).
   - Add simple placeholder route content to exercise the stack: user list placeholder can navigate to user detail, user detail can navigate to post detail/posts/todos, post detail can show comments-state placeholders.
   - Keep content clearly placeholder/structural; real data screens arrive in roadmap items 12.4 and 12.5.

7. **Implement expanded tablet shell host**
   - Create: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/ExpandedAppShell.kt`.
   - Render a persistent master list pane on the left and a detail pane on the right.
   - Selection in the master pane updates the detail pane in place instead of pushing a full-screen page.
   - Detail pane should support the UX hierarchy structurally:
     - no selection state;
     - selected user detail with profile header, Posts section, Todos section;
     - optional post detail state in the detail pane when a post is selected.
   - Keep a future extension seam for a third pane or sheet for post detail, but implement only 2-pane now because the UX flags third pane as a later depth decision.
   - Ensure Posts/Todos can be laid out side-by-side on wide detail panes and stacked on narrower portrait/tablet panes.

8. **Add reusable state and skeleton containers**
   - Create: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/components/ContentState.kt`.
   - Create: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/components/StateMessage.kt`.
   - Create: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/components/Skeleton.kt`.
   - Provide generic low-fi components for:
     - centered empty state with title/body/action;
     - centered error state with title/body/retry;
     - loading skeleton rows whose geometry can match eventual user/comment/list rows;
     - a content-state wrapper that switches loading/empty/error/content.
   - Include basic shimmer only if achievable with existing Compose APIs and little code; otherwise implement static skeleton blocks now and leave animated shimmer polish for roadmap 12.4/12.7.

9. **Add structural reusable controls from the wireframes**
   - Create: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/components/StatusChip.kt`.
   - Create: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/components/SegmentedFilter.kt`.
   - Optionally create `InitialsAvatar.kt` if it avoids duplication in placeholders.
   - Status chips should include a dot and label so status is not colour-only, matching `design/ux_v1.html:285-286`.
   - Segmented filter should model `All`, `Active`, and `Inactive` structurally; actual filtering logic belongs with the user list feature later.

10. **Add common tests for pure navigation/layout logic**
    - Create: `SliidePeek/shared/src/commonTest/kotlin/com/sliide/useractivity/ui/navigation/AppNavigatorTest.kt`.
    - Create: `SliidePeek/shared/src/commonTest/kotlin/com/sliide/useractivity/ui/shell/AppLayoutTest.kt` if layout classification is pure/testable.
    - Test initial route, push, back, reset, root back no-op, and compact/expanded threshold classification.
    - Avoid screenshot or pixel tests in this structural roadmap item.

11. **Update README with UX shell status**
    - Touch: `SliidePeek/README.md`.
    - Add a short note that `design/ux_v1.html` now informs the shell: phone push stack, tablet 2-pane master-detail, reusable state containers, and theme foundations.
    - State that hi-fi visual polish, final design tokens, real data screens, and feature ViewModels are planned for later roadmap items.

## Verification

Run from `SliidePeek/`:

- `./gradlew :shared:compileKotlinMetadata` succeeds.
- `./gradlew :shared:allTests` succeeds, or if native simulator test tasks are unavailable locally, run the available common/host test tasks and note the toolchain limitation.
- `./gradlew :androidApp:assembleDebug` succeeds.
- `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64` succeeds on macOS with the iOS toolchain installed.
- Manual compact check: launch Android or iPhone-sized preview/device and verify Users → Profile → Post/Posts/Todos uses one-screen-at-a-time push navigation with back returning one level.
- Manual expanded check: launch iPad/tablet-width preview/simulator if available and verify the user list remains persistent on the left while selection updates the detail pane on the right.
- Manual state check: placeholders can show loading skeleton, empty state with recovery action, and error state with retry action.
- Manual theme check: light and dark modes use `MaterialTheme.colorScheme`; active/selected state is visible with label/dot/keyline rather than colour alone.
- Review check: no GoREST data layer, repositories, feature ViewModels, authentication, or high-fidelity UI polish was introduced in this roadmap item.

## Risks

- The UX intentionally leaves high-fidelity light/dark tokens for later; implementing final colour, typography, elevation, or motion now would create rework.
- Tablet post-depth is explicitly undecided in the wireframes (`design/ux_v1.html:487` mentions third pane or sheet later). Keep the 12.2 shell 2-pane with a clean extension seam rather than committing to a third pane now.
- Compose Multiplatform available APIs can differ by version; prefer existing Material3/Foundation APIs already present in `SliidePeek/shared/build.gradle.kts:42-50` before adding dependencies.
- The iOS wrapper currently hosts Compose from `MainViewController()` and should not need platform edits, but safe-area behaviour still needs simulator/device verification.
- Placeholder route content may be mistaken for final UI if over-styled; keep labels and README clear that real screens and polish arrive in later roadmap items.
