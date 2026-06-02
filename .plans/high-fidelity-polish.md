# High-Fidelity UX Polish, Animation & Accessibility (Roadmap 12.8)

## Goal

Apply the hi-fi **"Signal"** visual design to the existing SliidePeek Compose Multiplatform app so it
feels production-ready and "expensive", without rewriting the architecture or redoing the already-shipped
12.4–12.7 feed/cache/Koin/add/delete logic. This is overwhelmingly a **theme + component-styling pass**
plus motion/accessibility refinement.

## Source-of-truth mapping (as given)

- `design/ux_v2.html` — UX structure (states, flows, layout).
- `design/design_engineering_spec.html` — behaviour, validation, copy, a11y, API semantics.
- `design/design_tokens_and_handoff.html` — **visual tokens** (colour/type/spacing/radii/motion) + Material 3 mapping. This is the visual truth for values.
- `design/high-fidelity-ui.html` + `design/design-canvas.jsx` + `design/ums-screens.jsx` — hi-fi visual reference (exact component look per state).

> The single most important finding: **the current app is themed with the older wireframe palette
> (warm paper `#F4F3EF` + amber `#E7C64D`), but the Signal hi-fi truth is a cool-grey surface with a
> blue accent `#2D5BFF`, green for Active status, Space Grotesk + JetBrains Mono type, and squared
> radii.** 12.8 is mostly about migrating to those tokens. Behaviour stays as-is.

---

## Codebase findings (current state)

Module root: `SliidePeek/`. All shared UI is under
`shared/src/commonMain/kotlin/com/sliide/useractivity/`.

### Theme (the main thing to change)
- `ui/theme/AppTheme.kt`
  - `AppSelectionColor = Color(0xFFE7C64D)` (amber) and `AppSelectionSoftColor` at lines 11–12.
  - `LightColors` (14–27) / `DarkColors` (29–42): warm-paper palette, amber `primary`, no green/info tokens.
  - `selectionContainerColor()` (55–60) + `isLight`/`luminance` helpers (62–66).
  - **No** extended-token holder, **no** custom Typography, **no** custom Shapes, **no** motion tokens.

### Reusable components (`ui/components/`)
- `StatusChip.kt` — pill (`RoundedCornerShape(999.dp)`, line 31); active uses `primary`/selection tint (amber). Signal wants **squared `radius-xs` (5dp) chip, green Active**.
- `InitialsAvatar.kt` — `CircleShape` (line 24), `labelMedium`. Signal wants **rounded-square (`radius-lg` 11dp) avatar with JetBrains Mono initials**; selected/added avatar = accent fill.
- `Skeleton.kt` — alpha-pulse shimmer via `tween(850)` (lines 32–38). Signal wants a **left→right gradient sweep, 1400ms** and row geometry matching real rows (42dp avatar, chip + time placeholders).
- `StateMessage.kt` — 64dp rounded icon block (lines 40–53), text symbol (`⚠`,`+`,`⚡`). Signal uses **64dp `radius-xl` icon container with a vector glyph + optional mono error code line**.
- `AppTopBar.kt` — title + subtitle + `OutlinedButton("+")` (63–66) + `OutlinedButton("Refresh")` text button (68–71). Signal wants a **mono `// directory` kicker, an accent count "pill", and icon-only refresh/add buttons (36dp)**.
- Also present: `ContentState.kt`/`ContentStateContainer`, `SectionHeader.kt`, `SegmentedFilter.kt` (All/Active/Inactive — not used by the feed; leave unless restyled for free).
- **Missing dedicated components** (currently inline): snackbar w/ countdown, offline/refresh banner (inline `FeedStatusBanner` in `UserFeedScreen.kt`), confirm dialog (inline in `AppRoot.kt`).

### Feed UI
- `ui/users/UserFeedScreen.kt`
  - State routing (93–154): loading→`ContentStateContainer`; offline-no-cache→`NoInternetState`; error-no-cache→`ApiErrorState`; empty→`EmptyFeedState`; else banner + list.
  - Inline `FeedStatusBanner` (206–252) — single neutral style for offline/refresh/error (only colour differs). Signal wants **three variants**: offline = neutral `line2` + cloud-off icon + "updated N ago"; refreshing = `accent-soft` + spinning refresh icon; error = `danger-bg`.
  - FAB (60–71): `onSurface`/`surface` (black) + `Text("+")`. Signal = **accent container, white plus icon, 56dp, `radius-xl` (18dp), 6dp elevation**.
  - Row animations (132–151): per-row `fadeIn()+expandVertically()`, `animateContentSize`. No stagger; exit not really exercised because list is keyed and removal happens in VM.
- `ui/users/UserFeedRow.kt` — Surface `RoundedCornerShape(10.dp)` (41), circle avatar (53), pill chip, selection uses amber. Signal = **`radius-md`(12dp) row, rounded-square mono avatar, mono email, green chip, accent selection keyline (`inset 3px accent`)**.

### Add-user UI
- `ui/users/AddUserForm.kt` — `OutlinedTextField` name/email (58–79), custom `FormSegmentGroup` (154–195), `FormErrorBanner` (136–152), submit `Button` text "Adding…" (125–131). Signal wants **focus/valid/error field rings (accent/green/danger), mono email value, M3 segmented look, a spinner in the loading button, and the error banner styled `danger-bg`**.
- Phone presentation: modal sheet in `CompactAppShell.kt`; tablet inline in `UserActionPanel.kt` (`expanded = true`).

### Delete / undo UI
- `ui/shell/AppRoot.kt`
  - Snackbars (50–86): `ShowUndoDelete` uses `SnackbarDuration.Long` + "Undo"; `ShowDeleteFailed` uses "Retry". **No countdown bar; window ≈10s, not the spec's 5s.**
  - `DeleteUserConfirmationDialog` (146–167): plain `AlertDialog`, copy matches spec. Signal wants a **leading destructive icon, `radius` ~20dp, Cancel(text)/Delete(danger)**.
- Long-press: `combinedClickable(onLongClick=…)` in `UserFeedRow.kt:40` — gives default ripple but **no scale-to-0.98, no haptic, no a11y custom action**.

### Adaptive shell
- `ui/shell/AppLayout.kt` — `CompactWidthThreshold = 700.dp` (19); `masterPaneWidthFor` 260/320dp (14–17). (Signal reference list pane ≈340dp / narrow 280dp.)
- `ui/shell/ExpandedAppShell.kt` — master `Surface` + `AppTopBar` + feed (compactRows); detail `UserActionPanel`. Selected-row highlight via amber today.
- `ui/users/UserActionPanel.kt` — hero (54dp circle avatar) + `DetailGrid` + Delete/Add buttons. Signal hero = **66dp accent rounded-square avatar, mono email, status+gender chips, 2-col `kv-grid` with mono keys**.
- `ui/shell/AppRoot.kt` uses `BoxWithConstraints.maxWidth` (96) — keep (no WindowSizeClass dep needed).

### ViewModel / state (DO NOT rework logic)
- `presentation/users/UserFeedViewModel.kt`, `UserFeedState.kt`, `AddUserFormState.kt`, `UserFeedItem.kt`, `UserFeedEvent.kt`. Only touch if a *visual* hook is genuinely missing (see Risks — countdown timing).

### Build / fonts
- `gradle/libs.versions.toml`: Compose MP `1.11.0` (12), Material3 `1.11.0-alpha07` (16), Koin/SQLDelight/Ktor present. `compose-components-resources` already declared (38) — so `Font()` from `composeResources` is available.
- `shared/src/commonMain/composeResources/` contains only `drawable/compose-multiplatform.xml` — **no fonts**. Space Grotesk / JetBrains Mono are not bundled; app uses Material 3 default (system) type.

### Tests / verification
- Shared tests under `shared/src/commonTest/...`: ViewModel, validator, use cases, mappers, `RelativeTimeFormatterTest`, `AppLayoutTest`, `AppNavigatorTest`, repository/api.
- Verify commands (README + Gradle): `./gradlew :shared:compileKotlinMetadata`, `./gradlew :shared:allTests` (and/or `:shared:iosSimulatorArm64Test`, `:shared:testDebugUnitTest`), `./gradlew :androidApp:assembleDebug`, `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64`.

---

## Conflicts / decisions to flag (do NOT silently change behaviour)

These are places where the design and the implemented 12.4–12.7 challenge behaviour disagree. Implement the **visual** treatment; keep behaviour unless a decision is taken. Call each out in code review / README rather than quietly changing it.

**Resolved (2026-06-01) — decisions locked:**

1. **Undo window — RESOLVED: option (a).** Keep the current behaviour (`SnackbarDuration.Long`, existing commit/undo timing) and **only restyle** the snackbar. **Do NOT** add a 5s countdown bar or change timing — leaving `UserFeedViewModelTest` undo/commit timing untouched. (Discrepancy with spec §04 / `undoWindow=5000` to be fed back to the design team.)
2. **Gender "Other" — RESOLVED: ignore.** Keep **Male/Female** only, matching GoREST and the current model/validator. Do not add Other.
3. **Status colour green / selection blue — RESOLVED: implement per Signal.** Build it as designed (**Active = green semantic; selection/links/FAB = accent blue** via extended tokens). The green-vs-amber / blue-vs-amber divergence from the old palette will be **fed back to the design team** for confirmation, but proceed with the Signal tokens now.
4. **Avatar shape & email font — RESOLVED: use Signal.** Adopt the **rounded-square avatar + JetBrains Mono email/timestamp/ID** treatment. (No compelling reason to deviate was found; if implementation surfaces one, raise it rather than silently keeping circles.)
5. **Custom fonts — RESOLVED: fallback approach is fine.** Bundle Space Grotesk + JetBrains Mono if assets are available (see Step 2 / Risks for sources); otherwise ship the type **scale/weights/spacing** with `FontFamily.Default`/`Monospace` fallbacks and note it.
6. **Haptics on long-press — RESOLVED: best-effort.** Wire `LocalHapticFeedback` where it's clean; don't block the pass on platform plumbing.

> Steps that require personal access, secrets, external systems, or explicit approval are tagged **[Human]**. Untagged steps are pure implementation work an agent can do.

---

## Implementation order

Ordered to land the token foundation first so every later step consumes it (minimises rework).

### 1. Signal colour tokens + theme wiring
**Files:** `ui/theme/AppTheme.kt` (rewrite colours), new `ui/theme/SignalColor.kt`.
- Transcribe the token table from `design_tokens_and_handoff.html` (`COLORS` array / `Color.kt` block) into `SignalLight`/`SignalDark` objects (bg, surface, ink, ink2, ink3, btext, line, line2, accent, accentSoft, green, greenBg, amber, amberBg, danger, dangerBg, skel, skelHi, inverse `--inv`/`--inv-ink`). Note the dark `accentSoft`/`*-bg` values carry alpha — use `Color(0x29..)` form.
- Map onto Material 3 `lightColorScheme`/`darkColorScheme`: `primary=accent`, `onPrimary=white`, `background=bg`, `surface=surface`, `onSurface=ink`, `onSurfaceVariant=ink2`, `outline=line`/`outlineVariant=line`, `error=danger`, `surfaceVariant=line2`, `inverseSurface=inv`, `inverseOnSurface=invInk`.
- Add an **extended-tokens** holder for what M3 has no slot for: `data class SignalExtended(green, greenBg, accentSoft, ink3, line2, amber, amberBg, skel, skelHi, dangerBg, btext)` + `val LocalSignalColors = staticCompositionLocalOf { … }`, provided in `AppTheme` via `CompositionLocalProvider`. Expose `MaterialTheme.signal` convenience accessor.
- Repoint `selectionContainerColor()` to **accentSoft** (was amber soft). Keep the `isLight` helper or derive from `darkTheme`.

### 2. Typography, shapes, spacing (+ fonts)
**Files:** new `ui/theme/Type.kt`, `ui/theme/Shape.kt`, `ui/theme/Spacing.kt`; update `AppTheme.kt` to pass `typography`/`shapes`.
- Build `SignalType` from the type scale (`title 23/700/-0.02em`, `heading 19/700`, `bodyStrong 14/600`, `body 13/400 lh21`, `label 11/500 mono`, `meta 10.5/600 mono`, `eyebrow 10/600 +0.1em mono`). Map into a Material 3 `Typography` (titleLarge=title, titleMedium=heading, bodyMedium=bodyStrong, bodySmall=body, labelMedium=label, labelSmall=meta) so existing `MaterialTheme.typography.*` call-sites inherit the new look with minimal edits.
- `SignalShapes`: small=`radius-xs/sm`, medium=`radius-md`(8–12), large=`radius-xl`(14) → into `MaterialTheme.shapes`. Add `Space`/`Radius`/`Stroke` constant objects (4/8/12/16/20/24; 5/6/8/11/14; 1/1.5/3) for direct use.
- **Fonts — DONE (fetched 2026-06-01).** OFL-1.1 TTFs are bundled in `shared/src/commonMain/composeResources/font/` with their `OFL.txt`:
  - `space_grotesk_regular.ttf` (400), `space_grotesk_medium.ttf` (500), `space_grotesk_bold.ttf` (700) — from `floriankarsten/space-grotesk@master/fonts/ttf/static`.
  - `jetbrains_mono_regular.ttf` (400), `jetbrains_mono_medium.ttf` (500), `jetbrains_mono_semibold.ttf` (600) — from `JetBrains/JetBrainsMono@master/fonts/ttf`.
  - **Implementation:** build `spaceGrotesk`/`jetBrainsMono` `FontFamily` via `org.jetbrains.compose.resources.Font(Res.font.space_grotesk_regular, FontWeight.Normal)` etc. (the `Res.font.*` accessors are generated on the first Gradle build).
  - **Caveat:** Space Grotesk ships **no static SemiBold (600)** — only Light/Regular/Medium/Bold. Map the type scale's SemiBold UI usages (`bodyStrong`) to **`FontWeight.Medium` (500)**; keep titles/headings at **Bold (700)**. JetBrains Mono has a real 600 for `meta`/`eyebrow`. Note this minor mapping in the PR.
  - Fallback (only if fonts are ever removed): `FontFamily.Default` (UI) / `FontFamily.Monospace` (meta/email) still carries the scale.

### 3. Motion tokens
**File:** new `ui/theme/Motion.kt`.
- `SignalMotion`: easings `emphDecel = CubicBezierEasing(0.05,0.7,0.1,1.0)`, `emphAccel = (0.3,0.0,0.8,0.15)`, `standard = (0.2,0.0,0.0,1.0)`; durations `shimmer=1400`, `contentFade=200`, `sheetEnter=280`, `sheetExit=220`, `rowInsert=240`, `highlight=1200`, `longPress=450`, `dialogEnter=260`, `rowRemove=240`, `reflow=200`, `undoWindow=5000`. Consumed by Steps 5/7/10.

### 4. Restyle shared components
**Files:** `ui/components/StatusChip.kt`, `InitialsAvatar.kt`, `Skeleton.kt`, `StateMessage.kt`, `AppTopBar.kt`; new `ui/components/SignalBanner.kt`, `ui/components/UndoSnackbar.kt`.
- **StatusChip:** squared `radius-xs`, mono uppercase label, dot = `currentColor`; Active → `green` text on `greenBg`; Inactive → `ink3` on `line2`; neutral variant (gender chip) → `ink2` on `line2`. Remove amber/selection coupling.
- **InitialsAvatar:** `RoundedCornerShape(11.dp)`, mono initials; add `accent: Boolean` param → accent fill + white initials (for added/restored row and tablet hero). Default = `bg` fill, `line` border, `ink` initials.
- **Skeleton:** replace alpha-pulse with a horizontal gradient sweep (`transparent→skelHi→transparent`, translateX -1→1, `tween(1400, linear)` infinite) drawn over `skel`-coloured blocks; match row geometry to the real row (42dp rounded-square avatar block, 52% + 84% lines, chip 52×18 + time 36×9). Keep it a generic block + row.
- **StateMessage:** 64dp `radius-xl` icon container (`surface`, `line` border; accent variant = `accentSoft`/accent; error = danger border/tint); accept a composable/vector icon slot instead of a glyph string; optional mono `code` line (e.g. `ERR_500 · /v2/users`, `NO_NETWORK · cache empty`). Primary button = filled accent; secondary = ghost/outline.
- **AppTopBar:** add mono `// directory` kicker line, `title`(titleLarge), accent count "pill" (mono on `accentSoft`, `radius-sm`), and convert add/refresh to **36dp icon buttons** (`radius-md`, `line` border; active state `accent`/`accentSoft`). Provide a refresh "spinning" flag. Keep ≥48dp touch target via inner click padding.
- **New `SignalBanner`** (extract + extend inline `FeedStatusBanner`): `variant = Offline | Refreshing | Error`; offline = `line2` fill + cloud-off icon + trailing mono "updated N ago"; refreshing = `accentSoft` + spinning refresh; error = `dangerBg` + alert icon. `radius-md`, ≈11.5px text.
- **Snackbar styling (decision #1 = option a):** restyle only — do not build a countdown bar. Provide custom content via `SnackbarHost { Snackbar(...) }` in `AppRoot.kt`: inverse-surface `radius` ~13dp, message + **accent** "Undo" action; error/"Retry" variant = `danger` bg with underlined action. No timing change.

### 5. Feed screen + row polish
**Files:** `ui/users/UserFeedScreen.kt`, `ui/users/UserFeedRow.kt`.
- Swap inline `FeedStatusBanner` for `SignalBanner` variants (offline / refreshing / error precedence already correct per spec §01 "cache beats error").
- FAB → accent container, white **plus icon** (small inline vector or `Text` with proper weight), 56dp, `radius-xl`, `elevation 6dp`; `contentDescription = "Add user"`.
- Row: `radius-md`(12dp) `surface` card with `line` border; rounded-square mono avatar; name `bodyStrong`; email mono `ink2`; right column = green/neutral chip + mono `ago` (`ink3`). Selected row → `accentSoft` bg + `inset 3px accent` keyline (via left border/`drawBehind`). Compact (tablet) variant keeps small avatar + short time.
- Animation: keep `AnimatedVisibility`; switch enter to `fadeIn(tween(contentFade)) + expandVertically(emphDecel)`, exit to `shrinkVertically(emphAccel) + fadeOut`; add ~30ms/row stagger on first load (index-based `initialDelay`); brief amber/accent highlight decay on the highlighted/added id (`highlightedUserId` already in state) via `animateColorAsState` over `highlight` ms.

### 6. Add-user form polish
**File:** `ui/users/AddUserForm.kt` (+ confirm sheet styling in `CompactAppShell.kt` if it sets sheet shape/scrim).
- Style `OutlinedTextField` colours: default `line`, focus `accent` + soft ring, valid `green` + trailing check icon, error `danger` + helper; email value uses mono. Keep validation wiring/state untouched (`AddUserFormState`, validator).
- Segmented groups: single rounded `line`-bordered container, dividers between segments, selected = `accentSoft`/`accent` (M3 segmented look). Keep Male/Female + Active/Inactive (Conflict #2).
- Submit button: filled accent; while `isSubmitting` show a rotating spinner + "Adding…", form locked (already locked via `enabled`). Disabled state = `line`/`ink3`.
- Error banner → `SignalBanner(Error)` styling (`dangerBg`), input preserved (already the case).
- Modal sheet (phone): top corners `radius-xl` (24dp), drag handle, scrim ~46% — set via `ModalBottomSheet` params in `CompactAppShell.kt`.

### 7. Delete confirm, undo snackbar & long-press polish
**Files:** `ui/shell/AppRoot.kt`, `ui/users/UserFeedRow.kt`, `ui/users/UserActionPanel.kt`.
- `DeleteUserConfirmationDialog`: add leading destructive icon (trash, `dangerBg` container), `shape = radius`(~20dp); Cancel = text button (left), Delete = `danger` filled (right). Copy unchanged.
- Snackbar: restyle via custom `Snackbar` content in `SnackbarHost` (inverse surface, accent "Undo"/underlined "Retry"). **No countdown bar / no timing change** (decision #1 = a).
- Long-press affordance: wrap row press with a `scale → 0.98` (`animateFloatAsState`) + the existing ripple; best-effort haptic via `LocalHapticFeedback` (`performHapticFeedback(LongPress)`); add **`Modifier.semantics { customActions = [Delete] }`** so long-press has a screen-reader-reachable alternative (spec §09). First-run one-time "Hold a user to manage" hint is optional/out-of-scope unless cheap.
- Tablet action panel Delete button already visible — keep as the discoverable alternative.

### 8. Adaptive / tablet panel polish
**Files:** `ui/users/UserActionPanel.kt`, `ui/shell/ExpandedAppShell.kt`, optionally `ui/shell/AppLayout.kt`.
- Hero: 66dp accent rounded-square avatar with mono initials, name `titleLarge`/heading, mono email, status + gender chips row.
- Replace `DetailGrid` column with a 2-col `kv-grid` (mono uppercase `ink3` keys, `ink` values; "Last active", "User ID #n", "Status", "Gender"). Keep a mono section divider ("Actions").
- Actions row: Delete = `danger` filled w/ trash icon; secondary = ghost. (Keep "Add user" as the secondary; do **not** introduce an Edit feature — out of scope.)
- Selected row keyline = accent (Step 5). Empty panel → restyled `StateMessage`/`ContentStateContainer` ("Select a user" copy from spec §08).
- Optionally bump `masterPaneWidthFor` toward 300–340 / 280 narrow to match the reference (minor; keep 700dp breakpoint).

### 9. Accessibility & touch targets
**Files:** across `AppTopBar.kt`, `UserFeedScreen.kt` (FAB), `UserFeedRow.kt`, `InitialsAvatar.kt`, `AppRoot.kt`.
- `contentDescription`: FAB ("Add user"), refresh, add, the dialog/sheet close affordance, avatars (decorative → null or name). Status conveyed by chip **label** not colour alone (already true — preserve).
- Touch targets ≥48dp: ensure icon buttons use `Modifier.minimumInteractiveComponentSize()` or sized click area even when visual is 36dp; row is already one large target.
- Snackbar Undo announced via `liveRegion` (M3 `SnackbarHost` already announces; verify) and given enough time.
- **Reduce-motion:** gate the slide/collapse/scale on a `reduceMotion` flag (best-effort; if no platform signal, expose a theme/composition flag) → collapse to instant cross-fades, keep the highlight, drop slides.

### 10. Motion refinements (sweep through with `SignalMotion`)
**Files:** Skeleton (Step 4), feed list (Step 5), sheet/dialog (Steps 6/7), refresh banner.
- Apply the documented durations/easings: shimmer 1400 linear; shimmer→content cross-fade 200 emphDecel + 30ms/row stagger; sheet enter 280 emphDecel / exit 220 emphAccel; row insert 240 + 1200 highlight; delete collapse 240 emphAccel + 200 reflow; undo restore 240 emphDecel + 1000 highlight; refresh icon spin 900/turn. Keep subtle.

### 11. Tests & verification
**Files:** `shared/src/commonTest/...` (add small, behaviour-level tests only).
- Add focused tests where a pure function is introduced/extracted: e.g. status→semantic mapping, initials, `compactRelativeTime`, and (if extracted) a token-selection helper. Avoid Compose pixel/UI tests (per testing skill — visuals are last priority).
- Decision #1 = option (a): snackbar timing is unchanged, so **leave `UserFeedViewModelTest` undo/commit tests as-is** — they must keep passing.
- Re-run existing suite (`RelativeTimeFormatterTest`, `UserFeedViewModelTest`, `AppLayoutTest`, etc.) to confirm no behavioural regression.

---

## Verification

Run from `SliidePeek/`:

```bash
./gradlew :shared:compileKotlinMetadata          # shared compiles
./gradlew :shared:allTests                        # shared unit tests (all targets)
./gradlew :androidApp:assembleDebug               # Android build
./gradlew :shared:linkDebugFrameworkIosSimulatorArm64   # iOS framework links
```

Manual visual checks (light **and** dark; phone **and** tablet width):
- Feed: loading shimmer (sweep, no layout jump), populated rows, empty, API error (with code line), no-internet/no-cache, offline+cached banner ("updated N ago"), refreshing banner (spinning icon, rows stay).
- Add: empty form, real-time validation (valid tick / error ring + helper), submitting spinner, success (new row highlight + count +1 + snackbar), submit error (banner, inputs preserved).
- Delete: long-press (scale/ripple), confirm dialog (destructive icon, Cancel left/Delete right), removal + Undo snackbar (restyled, no countdown), Undo restores at original index, delete-failed Retry snackbar.
- Tablet: feed | action panel (selected user hero + kv-grid + Delete), inline Add form, empty "Select a user", portrait narrow list, accent selected-row keyline.
- A11y: TalkBack/VoiceOver reaches FAB/refresh/Delete; long-press has a custom action; status read by label.

## Risks

- **Palette migration is broad-touch.** Almost every component reads `colorScheme`/`primary`; doing Step 1 first and relying on `MaterialTheme` indirection keeps call-site churn low, but the amber→blue + circle→square + green-status changes will visibly alter every screen. Review against the hi-fi reference per state.
- **Custom fonts need binary assets** ([Human], Step 2). Without them the type *scale* applies but the exact Space Grotesk / JetBrains Mono look won't. Provide the fallback so the build never blocks on missing fonts.
- **Undo countdown — resolved (a):** not built; snackbar is restyle-only, so no timing-regression risk. (5s window discrepancy deferred to design team.)
- **Haptics & reduce-motion** have no clean common-only API; keep them best-effort (`LocalHapticFeedback`, a theme flag) and don't block the pass on platform plumbing.
- **Material3 is on an alpha** (`1.11.0-alpha07`); confirm `SingleChoiceSegmentedButtonRow`/`ModalBottomSheet` APIs match before relying on them — the existing custom segmented control already works, so prefer restyling it over swapping in the M3 component if the alpha API is unstable.
- Do **not** add Edit-user, search/filter, offline mutation queue, or other §4.5/§10 features — out of 12.8 scope.
