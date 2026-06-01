# Designer Brief: Sliide KMP UX Innovator Challenge

## Project Overview

Design a high-fidelity, polished **User Management System** for the Sliide KMP "UX Innovator" challenge.

The app will be built with **Kotlin Multiplatform** and **100% shared Compose Multiplatform UI** for:

- Android phones
- iPhone
- iPad/tablet layouts

The official challenge emphasises that AI will be used to accelerate development, so the design should help the implementation team focus on architecture, UX polish, and quality rather than broad feature sprawl.

---

## Product Goal

Create a premium-feeling user directory app that allows someone to:

1. Browse a smart feed of users.
2. Add a new user through a polished form.
3. Delete a user with confirmation and Undo.
4. Continue using the app gracefully when offline.

The app should feel modern, responsive, and "expensive" despite being a focused coding challenge.

---

## Official Core Features To Design

## 1. Smart User Feed

The main screen is a user feed sourced from the GoREST `/users` API.

Each user row/card must show:

- Name
- Email
- Relative timestamp, e.g. `5 minutes ago`

The timestamp is calculated in shared app logic. Since the API may not provide a real creation timestamp, the UI should work for a local timestamp concept such as `fetched just now`, `cached 5 minutes ago`, or `created 2 minutes ago`.

Feed states to design:

- Initial loading with shimmer
- Populated feed
- Pull-to-refresh or explicit refresh
- Empty state
- Error state
- No Internet / Offline state
- Cached/offline feed state

---

## 2. Add User Flow

The app must include a floating action button or similarly prominent add action.

The add flow should include a polished form with:

- Name field
- Email field
- Gender input
- Status input
- Submit action
- Cancel/dismiss action

Validation states:

- Empty name
- Invalid name
- Empty email
- Invalid email
- Valid form
- Submit loading
- Submit error
- Submit success

UX requirement:

- Validation should feel real-time and helpful.
- Error copy should be clear and human.
- After successful creation, the user appears immediately at the top of the feed.

Adaptive presentation ideas:

- Phone: modal sheet or pushed form screen.
- Tablet/iPad: right-side panel, dialog, or split-view detail panel.

---

## 3. Delete with Confirmation and Undo

The app must support deleting users.

Flow:

1. Long-press a user.
2. Show delete confirmation.
3. Confirm delete.
4. User item disappears with animation.
5. Snackbar appears with Undo action.
6. Undo restores the item locally.

Design states:

- Long-press affordance or contextual hint
- Delete confirmation dialog/sheet
- Destructive action styling
- Removal animation direction/feel
- Snackbar with Undo
- Restored item feedback, if useful
- Delete failure state

Important UX note:

- Undo is primarily a local-state restore interaction. If the remote API cannot actually restore the deleted server record, the UI should still feel coherent and the implementation should document the behaviour.

---

## 4. Offline Support

The official challenge requires the app to work offline using local caching.

Design should include:

- Cached feed display when offline
- No Internet state when no cache exists
- Offline banner/chip/indicator where appropriate
- Retry action
- Last updated / relative cached time where useful
- Clear but non-alarming offline messaging

Offline should feel graceful, not like a crash state.

---

## Adaptive Layout Requirements

The app must adapt between:

- Portrait phone: single-column feed
- Landscape/tablet/iPad: master-detail or two-column layout

Possible tablet layouts:

```text
User Feed | Add/Edit/Delete Action Panel
```

or:

```text
Two-column User Feed
```

or:

```text
User Feed | Selected User / Contextual Actions
```

Design priorities:

- Do not simply stretch the phone UI.
- Keep touch targets comfortable.
- Preserve readable line lengths.
- Make add/delete flows feel natural on larger screens.
- Support portrait and landscape.

---

## Visual Style Direction

The official evaluation asks whether the app feels "expensive".

Target feel:

- polished
- modern
- premium
- calm
- focused
- high-fidelity
- Material 3 influenced
- not cluttered

Suggested design language:

- refined cards or list rows
- subtle elevation/surfaces
- well-considered typography
- high-quality spacing
- expressive but restrained accent colour
- polished chips/badges
- refined text-field states
- tasteful motion guidance

Avoid:

- generic scaffold-only UI
- dense admin dashboard feel
- excessive colours
- toy-like visual treatment
- overcomplicated navigation
- features outside the official scope taking visual priority

---

## Light and Dark Mode

Design both light and dark modes.

Requirements:

- Both themes should feel intentional.
- Dark mode should not be a simple inversion.
- Shimmer should look good in both themes.
- Offline/error/destructive states should be accessible.
- Validation states must be readable and not overly harsh.

---

## Motion and Loading UX

Design guidance is needed for:

- shimmer loading rows/cards
- add form opening/closing
- validation feedback
- item deletion animation
- Snackbar entrance/exit
- Undo restore animation
- screen/panel transitions for adaptive layouts

Motion should feel premium but not slow.

---

## Primary Screens / States To Design

Minimum requested design coverage:

1. User feed — loading shimmer
2. User feed — populated
3. User feed — offline with cached users
4. User feed — no internet and no cache
5. User feed — API error with retry
6. Add user form — empty
7. Add user form — validation errors
8. Add user form — submit loading
9. Add user success state / inserted-at-top behaviour
10. Delete confirmation
11. Delete animation + Undo Snackbar
12. Tablet/iPad adaptive layout
13. Light mode
14. Dark mode

Optional if time allows:

15. API token/settings treatment, if needed for authenticated GoREST writes
16. User detail view
17. Edit user flow
18. Search/filtering

---

## Key Components

Reusable components likely needed:

- user row/card
- relative timestamp label
- offline banner/chip
- shimmer user row/card
- empty state block
- no-internet state block
- error/retry block
- floating action button
- add-user form fields
- validation messages
- gender/status selectors
- destructive confirmation dialog/sheet
- Snackbar with Undo
- adaptive feed/action panel container

---

## Deliverables Requested

Please provide:

- UX flow for the official core features
- high-fidelity phone designs
- high-fidelity iPad/tablet designs
- light and dark theme examples
- component states for validation, loading, offline, error, delete, and undo
- motion notes for shimmer/add/delete/undo interactions
- implementation-friendly specs for spacing, colours, typography, and component behaviour

The design should be practical for Compose Multiplatform implementation within a coding challenge timeframe.
