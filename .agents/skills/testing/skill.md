---
name: lightweight-testing-for-kmp
description: |-
  Apply when writing or reviewing tests for mappers, repositories, API clients, ViewModels, and simple Compose UI behaviour.
---

## When to Use This Skill

Use this skill when adding or modifying:

- mapper tests
- API client tests
- repository tests
- ViewModel/state tests
- JSON fixtures
- test utilities

## Goal

Add enough tests to show professional discipline without overbuilding a large enterprise test suite.

## Preferred Testing Stack

- `kotlin.test` for shared tests
- `kotlinx.coroutines.test` for coroutine tests
- Ktor MockEngine for API/client tests
- JSON fixtures for realistic API responses

Avoid heavy mocking frameworks unless they are already part of the project.

## What To Test First

Prioritise:

1. DTO-to-domain mappers
2. API client parsing
3. Repository success/error handling
4. ViewModel loading/error state transitions
5. Any non-trivial date/status formatting

UI tests are nice-to-have, not the first priority.

## Fixture Pattern

Place realistic API response examples under:

```text
shared/src/commonTest/resources/
  users/users_page_1.json
  users/user_detail.json
  posts/user_posts.json
  comments/post_comments.json
  todos/user_todos.json
```

Use fixtures copied or adapted from the real GoREST response shape.

## Mapper Test Example

```kotlin
@Test
fun `maps user dto to domain user`() {
    val dto = UserDTO(
        id = 1,
        name = "Alice Example",
        email = "alice@example.com",
        gender = "female",
        status = "active",
    )

    val user = dto.toDomain()

    assertEquals(1, user.id)
    assertEquals("Alice Example", user.name)
    assertEquals(UserStatus.Active, user.status)
}
```

## ViewModel Test Targets

Test that:

- initial state is sensible
- loading state appears while fetching
- success populates data
- failure shows an error state
- refresh clears previous error where appropriate

## Guidelines

- Keep tests short and readable.
- Use fixtures over large inline JSON strings.
- Test behaviour, not implementation details.
- Prefer small fake repositories/use cases over mocking frameworks.
- Do not chase high coverage for its own sake.

## Checklist

When adding tests, check:

1. Is there a mapper test for new DTO mapping?
2. Is realistic fixture JSON used where helpful?
3. Are repository/API success and failure paths covered if non-trivial?
4. Does ViewModel state transition correctly from loading to success/failure?
5. Are tests fast and readable?
6. Can the tests run in shared/common test where practical?

## Output Expected

Testing changes should produce:

- small focused tests
- realistic fixtures
- minimal fake dependencies
- no brittle implementation-detail assertions

## Common Mistakes

Avoid:

- large inline JSON blobs when fixtures would be clearer
- mocking everything by default
- chasing coverage percentages instead of meaningful tests
- testing Compose visuals before core behaviour is stable

## Key Rules

- Shared business logic should have shared tests.
- Mappers should be tested.
- Network parsing should be tested with realistic JSON.
- Keep the test suite fast enough to run often.
