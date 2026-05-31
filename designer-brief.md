# Designer Brief: User Activity Manager Mobile App

## Project Overview

Design a clean, modern mobile/tablet application called **User Activity Manager**.

The app uses a public REST API to display users and their related activity, including posts, comments, and todos. The application is intended as a small but polished coding-task app, so the design should feel production-quality while remaining simple and easy to implement.

The app will be built using **Kotlin Multiplatform Compose** and should support:

- Android phones
- iPhone
- iPad/tablet layouts
- Light mode
- Dark mode

---

## Product Goal

Allow users to browse a list of people, open a person’s profile, and view their related activity in a clear and pleasant way.

The app should demonstrate:

- good mobile UX
- clean information hierarchy
- elegant loading states
- responsive/adaptive layouts
- light and dark theme polish
- simple user-based interactions

---

## Core User Flow

### 1. User List

The user lands on a list of users.

Each user card/row should show:

- name
- email
- active/inactive status
- gender, if useful visually

Actions:

- tap a user to open detail
- refresh list
- optionally filter by active/inactive

States to design:

- loading shimmer
- populated list
- empty state
- error state

---

### 2. User Detail

The user detail screen should show:

- name
- email
- status
- gender
- related posts
- related todos

Possible layout:

- profile/header section at top
- segmented tabs or sections for:
  - Posts
  - Todos

Actions:

- back to user list on phone
- select post
- view todos

---

### 3. Posts

Posts can be shown either inside user detail or as a child screen.

Each post preview should show:

- title
- short body preview

Tapping a post opens post detail.

---

### 4. Post Detail and Comments

Post detail should show:

- title
- full body
- comments list

Each comment should show:

- commenter name
- email
- comment body

States:

- comments loading shimmer
- no comments
- error loading comments

---

### 5. Todos

Todos should show:

- title
- due date
- status

Todo status should be visually clear, for example:

- completed chip
- pending chip
- subtle icon or colour treatment

---

## iPad / Tablet UX Requirement

The app must not simply stretch the phone layout on iPad/tablet.

Use an adaptive master-detail style layout.

Recommended iPad layout:

```text
User List | User Detail
```

If space allows, a deeper layout could be:

```text
User List | User Detail / Posts | Post Detail / Comments
```

Tablet behaviour:

- selecting a user updates the detail panel instead of pushing a full-screen page
- selected item should be visually highlighted
- navigation should feel efficient in landscape
- portrait should still work gracefully
- keep readable line lengths and comfortable spacing

---

## Visual Style Direction

The desired feel is:

- clean
- minimalist
- calm
- modern
- professional
- not enterprise-heavy
- not overly playful

Suggested visual language:

- soft cards
- rounded corners
- clear spacing
- readable typography
- subtle dividers
- restrained accent colour
- small status chips
- simple icons where useful

Avoid:

- dense dashboard UI
- excessive gradients
- too many colours
- complex illustrations
- overly bespoke components that are hard to implement

---

## Light and Dark Mode

Design both light and dark themes.

Requirements:

- both themes should feel intentional
- status colours must be accessible in both modes
- cards and surfaces should have clear contrast
- shimmer loading should work in both modes
- error states should not be harsh or visually jarring

---

## Loading and Animation UX

The app should include subtle, polished loading and motion.

Design for:

- shimmer/skeleton list rows
- smooth transition from loading to content
- subtle screen transitions
- animated empty/error state appearance
- no jarring layout jumps

Shimmer placeholders should roughly match the final content layout so the UI feels stable.

---

## Primary Screens to Design

Minimum required screens/states:

1. User list — loading
2. User list — populated
3. User list — error
4. User detail — phone
5. User detail — iPad split view
6. Posts section/list
7. Post detail with comments
8. Todos section/list
9. Empty state
10. Dark mode examples

Optional screens:

11. Create/edit user form
12. API token/settings screen
13. Filter/search state

---

## Key Components

Useful reusable components:

- user row/card
- status chip
- todo row/card
- post preview card
- comment card
- shimmer placeholder card
- empty state block
- error/retry block
- adaptive split-view container

---

## Deliverables Requested

Ideally provide:

- phone flow wireframes
- iPad/tablet adaptive layout wireframes
- light theme visual design
- dark theme visual design
- loading shimmer examples
- key component states
- simple clickable prototype if time allows

The design should be implementation-friendly for Compose Multiplatform.
