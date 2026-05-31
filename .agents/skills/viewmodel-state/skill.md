---
name: viewmodel-state
description: |-
  Apply when creating or modifying ViewModels, UI state, loading/error state, and one-shot events for the small KMP Compose app.
---

## When to Use This Skill

Use this skill when adding or modifying:

- screen ViewModels/state holders
- `StateFlow` UI state
- loading/error/refresh state
- one-shot events
- user interaction handlers
- shared presentation logic

## Goal

Use a small, predictable state-management pattern that is easy to review and works in shared KMP code.

## Pattern

Each screen-level ViewModel/state holder should expose:

- immutable `StateFlow<State>` for persistent UI state
- optional `Flow<Event>` for one-shot effects such as snackbars or navigation
- simple public functions for user actions

## State Example

```kotlin
data class UserListState(
    val isLoading: Boolean = false,
    val users: List<User> = emptyList(),
    val errorMessage: String? = null,
    val isRefreshing: Boolean = false,
)
```

## ViewModel Example

```kotlin
class UserListViewModel(
    private val getUsers: GetUsersUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(UserListState())
    val state: StateFlow<UserListState> = _state.asStateFlow()

    fun loadUsers() {
        viewModelScope.launch {
            _state.update { it.copy(isLoading = true, errorMessage = null) }

            when (val result = getUsers()) {
                is Result.Success -> _state.update {
                    it.copy(isLoading = false, users = result.value)
                }
                is Result.Failure -> _state.update {
                    it.copy(isLoading = false, errorMessage = result.message)
                }
            }
        }
    }
}
```

Adapt the exact base `ViewModel` depending on the KMP lifecycle library chosen.

## Events

Use events only for one-shot effects:

```kotlin
sealed interface UserListEvent {
    data class ShowMessage(val message: String) : UserListEvent
}
```

Prefer not to use events for data that should survive recomposition or rotation; put that in `State`.

## Guidelines

- Keep ViewModels in shared/common code where possible.
- Do not reference Android framework types from shared ViewModels.
- Use `StateFlow` for screen state.
- Use `data class` state objects.
- Keep state names obvious: `isLoading`, `errorMessage`, `selectedUserId`.
- Avoid complex state machines unless the feature genuinely needs them.
- One ViewModel per major screen is enough for this app.
- Keep actions simple: `loadUsers()`, `refresh()`, `selectUser(id)`.

## Checklist

When adding a ViewModel/state holder, check:

1. Is the public state immutable?
2. Is mutable state private?
3. Are loading/error/empty states represented clearly?
4. Are one-shot events used only for one-shot effects?
5. Are public functions named after user/app actions?
6. Is the code free of Android-only dependencies?
7. Is the state model simple enough to understand quickly?

## Output Expected

State changes should produce:

- one obvious state object per screen
- small action functions
- predictable loading/error handling
- no direct UI framework dependencies in business logic

## Common Mistakes

Avoid:

- making composables call repositories directly
- using events for persistent screen data
- exposing `MutableStateFlow`
- using `GlobalScope`
- creating a complex state machine for simple screens

## Key Rules

- Immutable state exposed publicly.
- Mutable state stays private.
- No `GlobalScope`.
- No direct API calls from composables.
- Minimal, readable state beats clever abstractions.
