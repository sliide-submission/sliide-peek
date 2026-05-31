---
name: compose-ui
description: |-
  Apply when creating or modifying Compose Multiplatform screens, shared UI components, navigation, dark mode, shimmer loading, or adaptive iPad/tablet layouts.
---

## When to Use This Skill

Use this skill when adding or modifying:

- Compose screens
- reusable UI components
- app theme / dark mode
- shimmer or loading states
- navigation
- iPad/tablet adaptive layouts
- empty/error states

## Goal

Build a clean, minimalist Compose Multiplatform UI that works well on Android, iPhone, and iPad without over-engineering.

## UI Principles

- Prefer simple, readable composables.
- Keep screens stateless where practical.
- Hoist state into ViewModels/state holders.
- Pass callbacks instead of coupling composables to repositories or API clients.
- Use Material theme values instead of hardcoded colours.
- Create small reusable components only when duplication is real.

## Suggested Structure

```text
shared/ui/
  theme/
  components/
  navigation/

shared/presentation/
  users/
  posts/
  todos/
```

## Stateless Screen Pattern

```kotlin
@Composable
fun UserListScreen(
    state: UserListState,
    onUserClick: (Long) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Render only. No API calls here.
}
```

## Theme Rules

- Support light and dark mode.
- Respect system theme by default.
- Use `MaterialTheme.colorScheme` and `MaterialTheme.typography`.
- Avoid direct hardcoded colours in screens.
- Keep the design understated: cards, spacing, readable typography.

## Loading UI

Use shimmer/skeleton loading for list screens:

- User list
- Post list
- Todo list
- Comment list

Keep shimmer components generic and lightweight.

## Navigation

Use a simple navigation approach suitable for Compose Multiplatform.

Routes should be typed or centralized where possible, but do not build a large custom navigation framework for this small app.

Example routes:

```kotlin
sealed interface AppRoute {
    data object Users : AppRoute
    data class UserDetail(val userId: Long) : AppRoute
    data class PostDetail(val postId: Long) : AppRoute
}
```

## iPad / Tablet Layout

Support adaptive layouts:

- Compact width: stacked phone navigation.
- Medium/expanded width: master-detail layout.
- Prefer `User List | User Detail` side-by-side on iPad/tablet.
- Avoid stretched phone UIs on large screens.
- Respect safe areas and orientation changes.

## Checklist

When adding a screen, check:

1. Does it accept state and callbacks rather than fetching data directly?
2. Does it support loading, error, and empty states?
3. Does loading use a shimmer/skeleton where appropriate?
4. Does it use `MaterialTheme` colours/typography?
5. Does it work in dark mode?
6. Does it behave sensibly on iPhone/phone widths?
7. Does it behave sensibly on iPad/tablet widths?
8. Is the UI simple enough for a coding-task reviewer to understand quickly?

## Output Expected

UI changes should produce:

- small composables
- obvious state/callback parameters
- minimal reusable components
- adaptive layout behaviour where required
- no hardcoded theme-breaking colours

## Common Mistakes

Avoid:

- putting API calls directly in composables
- overbuilding a design system
- stretching a phone layout across iPad
- hardcoding strings/colours everywhere
- adding complex navigation abstractions too early

## Key Rules

- Stateless composables where practical.
- State and events come from ViewModels/state holders.
- Dark mode is required.
- Shimmer loading is required.
- iPad/tablet master-detail behaviour is required.
- Keep UI clean and minimalist, not enterprise-heavy.
