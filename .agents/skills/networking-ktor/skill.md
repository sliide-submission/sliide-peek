---
name: networking-ktor
description: |-
  Apply when creating or modifying the GoREST API client, DTOs, mappers, repositories, or network error handling. Keep the implementation simple, testable, and suitable for a small Kotlin Multiplatform Compose app.
---

## When to Use This Skill

Use this skill when adding or modifying:

- GoREST API calls
- Ktor client setup
- DTOs
- DTO-to-domain mappers
- repositories/data sources
- API error handling
- API-related tests

## Goal

Use a lightweight Ktor-based networking layer for the GoREST API without introducing unnecessary production-SDK complexity.

## Stack

- HTTP client: Ktor Client
- JSON: kotlinx.serialization
- Async: suspend functions and coroutines
- API base URL: `https://gorest.co.in/public/v2`

Do not introduce Retrofit for this project.

## Suggested Structure

```text
shared/data/remote/
  dto/
    UserDTO.kt
    PostDTO.kt
    CommentDTO.kt
    TodoDTO.kt
  mapper/
    UserMapper.kt
  GorestApiClient.kt

shared/data/repository/
  UserRepositoryImpl.kt

shared/domain/
  model/
  repository/
```

## DTO Rules

- DTOs should use the `DTO` suffix: `UserDTO`, `PostDTO`, `TodoDTO`.
- DTOs should be `@Serializable`.
- Keep DTOs separate from domain models.
- Prefer nullable fields only where the API can genuinely omit values.

Example:

```kotlin
@Serializable
internal data class UserDTO(
    val id: Long,
    val name: String,
    val email: String,
    val gender: String,
    val status: String,
)
```

## Mapping Rules

- Use mapper extension functions named `toDomain()`.
- Put mappers in small `Mapper.kt` files.
- Keep mapping boring and explicit.
- Do not expose DTOs to UI code.

Example:

```kotlin
internal fun UserDTO.toDomain(): User = User(
    id = id,
    name = name,
    email = email,
    gender = gender,
    status = status,
)
```

## API Client Rules

- Keep the API client focused on HTTP calls only.
- Repositories should decide how API data is used by the rest of the app.
- Add auth headers only for write operations that require a token.
- Handle pagination in a simple explicit way.

Example endpoints:

```text
GET /users
GET /users/{id}
GET /users/{id}/posts
GET /posts/{id}/comments
GET /users/{id}/todos
```

## Error Handling

For this small app, avoid complicated error hierarchies.

Use a simple app-level result type or sealed error model, for example:

```kotlin
sealed interface AppError {
    data object Network : AppError
    data object Unauthorized : AppError
    data object NotFound : AppError
    data class Validation(val message: String) : AppError
    data class Unknown(val message: String? = null) : AppError
}
```

ViewModels should receive domain/repository results, not raw Ktor exceptions.

## Checklist

When adding an endpoint, usually do this:

1. Add or update a `DTO`.
2. Add or update a `toDomain()` mapper.
3. Add the Ktor API call.
4. Expose it through a small repository method.
5. Make sure UI/ViewModels receive domain models, not DTOs.
6. Add a small fixture-based test if the response shape is non-trivial.

## Output Expected

Networking changes should produce simple, readable files with clear boundaries between:

- remote DTOs
- mapping
- API calls
- repository/domain-facing methods

## Common Mistakes

Avoid:

- leaking DTOs into UI state
- adding Retrofit
- building a large networking framework
- over-designing pagination before it is needed
- mixing auth/token logic through unrelated UI code

## Key Rules

- Ktor only, no Retrofit.
- DTOs stay in data layer.
- UI works with domain models only.
- Use `toDomain()` mappers.
- Keep auth/token handling isolated.
- Prefer simple readable code over abstraction-heavy networking frameworks.
