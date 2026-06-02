# GoREST API and Domain Layer Implementation Plan

## Goal

Create a small, testable GoREST data/domain layer for users, posts, comments, and todos using Ktor Client and kotlinx.serialization. This roadmap item should prepare clean domain-facing repositories for later ViewModel/UI work without implementing high-fidelity UI, design-system polish, or authenticated write flows.

## Codebase findings

- `PROJECT_SPEC.md:433-443` requires Ktor Client, kotlinx.serialization, strongly typed parsing, graceful HTTP/network error handling, non-blocking calls, and testability through repository interfaces or mock clients.
- `PROJECT_SPEC.md:445-452` lists expected error cases: no internet, timeout, unauthorized, not found, validation failures, and server errors.
- `PROJECT_SPEC.md:704-720` recommends Ktor + kotlinx.serialization, internal `DTO` suffix models, `toDomain()` mapper functions, separated domain models, fixture-based tests, Ktor mock-client style tests, `kotlin.test`, `kotlinx.coroutines.test`, and version-catalog dependencies.
- `PROJECT_SPEC.md:822-839` defines roadmap 12.3 scope and the expected plan file: Ktor client, serialization DTOs, domain models, mappers, repositories/data sources, basic error handling, and fixture-based tests.
- `SliidePeek/settings.gradle.kts:31-32` contains only `:androidApp` and `:shared`; the data/domain work should stay in the existing `shared` module.
- `SliidePeek/gradle/libs.versions.toml:1-41` currently has Compose, AndroidX, Kotlin, and Material3 entries, but no Ktor, kotlinx.serialization JSON, serialization Gradle plugin, or coroutines-test aliases yet.
- `SliidePeek/shared/build.gradle.kts:3-8` applies Kotlin Multiplatform, Android KMP library, Compose Multiplatform, and Compose compiler plugins; it does not yet apply `org.jetbrains.kotlin.plugin.serialization`.
- `SliidePeek/shared/build.gradle.kts:43-55` has Compose/lifecycle common dependencies and `kotlin.test`, but no networking or coroutine test dependencies.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/App.kt:8-14` is still a UI-only entry point wrapping `AppRoot()` in `AppTheme`; roadmap 12.3 should not need to change this.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/navigation/AppRoute.kt:3-9` already models users, user detail, posts, todos, and post detail routes; later UI/ViewModels can consume repositories without changing this route shape.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/shell/RoutePlaceholders.kt:45-76` contains private demo users/posts/todos used by the structural shell. These should remain placeholders for 12.2 and not be promoted into the data layer.
- `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/ui/components/ContentState.kt:10-15` has UI loading/empty/error/content state containers. Domain/data errors should be separate and mapped by later presentation code.
- `SliidePeek/shared/src/commonTest/kotlin/com/sliide/useractivity/ui/navigation/AppNavigatorTest.kt:9-63` shows existing shared tests use `kotlin.test`; new data/domain tests should follow this lightweight style.

## Approach summary

Add a minimal layered structure under `shared/src/commonMain/kotlin/com/sliide/useractivity/`:

```text
domain/
  model/
  repository/
  AppError.kt
  AppResult.kt
data/
  remote/
    dto/
    mapper/
    GorestApiClient.kt
    HttpClientFactory.kt
  repository/
```

Keep the Ktor client focused on HTTP and DTO parsing. Repositories should convert DTOs to domain models and return a small `AppResult<T>` with a simple `AppError` model. Use read-only endpoints only in this plan; leave bearer-token writes for a later optional roadmap item.

## Implementation order

1. **Add networking and serialization dependencies**
   - Touch: `SliidePeek/gradle/libs.versions.toml`.
   - Add version aliases for Ktor, kotlinx.serialization JSON, and kotlinx.coroutines test.
   - Add library aliases for:
     - `io.ktor:ktor-client-core`
     - `io.ktor:ktor-client-content-negotiation`
     - `io.ktor:ktor-serialization-kotlinx-json`
     - `io.ktor:ktor-client-okhttp` for Android
     - `io.ktor:ktor-client-darwin` for iOS
     - `io.ktor:ktor-client-mock` for tests
     - `org.jetbrains.kotlinx:kotlinx-serialization-json`
     - `org.jetbrains.kotlinx:kotlinx-coroutines-test`
   - Add plugin alias for `org.jetbrains.kotlin.plugin.serialization` using the Kotlin version.

2. **Wire dependencies into the shared module**
   - Touch: `SliidePeek/shared/build.gradle.kts`.
   - Apply the serialization plugin alongside the existing Kotlin/Compose plugins.
   - Add common networking/JSON dependencies to `commonMain.dependencies`.
   - Add `ktor-client-okhttp` to `androidMain.dependencies`.
   - Add `ktor-client-darwin` to an `iosMain.dependencies` block if the source set exists in Gradle, or configure the existing iOS source set hierarchy in the simplest compatible way.
   - Add `ktor-client-mock` and `kotlinx-coroutines-test` to `commonTest.dependencies`.

3. **Create domain result and error types**
   - Create: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/domain/AppResult.kt`.
   - Create: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/domain/AppError.kt`.
   - Use a small sealed result type, for example `Success<T>` / `Failure(error: AppError)`.
   - Use a simple error model with `Network`, `Timeout`, `Unauthorized`, `NotFound`, `Validation(message)`, `Server(statusCode)`, and `Unknown(message)`.
   - Do not expose Ktor exceptions to presentation code.

4. **Create domain models**
   - Create under `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/domain/model/`:
     - `User.kt`
     - `Post.kt`
     - `Comment.kt`
     - `Todo.kt`
     - small enum files if clearer: `UserGender.kt`, `UserStatus.kt`, `TodoStatus.kt`.
   - Keep fields close to GoREST: IDs as `Long`, titles/bodies/emails as `String`, todo due date as a raw `String?` for now to avoid premature date/time complexity.
   - Use enum `Unknown(raw: String)` only if implementing sealed classes; otherwise use simple enum values plus `Unknown`. Prefer the simpler option unless tests show raw values must be preserved.

5. **Create repository interfaces**
   - Create under `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/domain/repository/`:
     - `UserRepository.kt`
     - `PostRepository.kt`
     - `TodoRepository.kt`
   - Suggested methods:
     - `UserRepository.getUsers(page: Int = 1, perPage: Int = 20): AppResult<Page<User>>`
     - `UserRepository.getUser(id: Long): AppResult<User>`
     - `UserRepository.getUserPosts(userId: Long): AppResult<List<Post>>`
     - `UserRepository.getUserTodos(userId: Long): AppResult<List<Todo>>`
     - `PostRepository.getPost(id: Long): AppResult<Post>`
     - `PostRepository.getPostComments(postId: Long): AppResult<List<Comment>>`
   - If pagination metadata is implemented, create `domain/model/Page.kt` with `items`, `page`, `perPage`, `totalPages`, and `hasNextPage`.

6. **Create serializable DTOs**
   - Create under `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/data/remote/dto/`:
     - `UserDTO.kt`
     - `PostDTO.kt`
     - `CommentDTO.kt`
     - `TodoDTO.kt`
   - Mark DTOs `@Serializable` and `internal`.
   - Match GoREST response names exactly where possible. Use `@SerialName("due_on")` for todo due dates.
   - Keep DTO nullable fields minimal; prefer non-null where GoREST always returns a field.

7. **Create DTO-to-domain mappers**
   - Create under `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/data/remote/mapper/`:
     - `UserMapper.kt`
     - `PostMapper.kt`
     - `CommentMapper.kt`
     - `TodoMapper.kt`
   - Implement internal extension functions named `toDomain()`.
   - Map string status/gender values explicitly and handle unknown values safely.
   - Do not import Compose/UI types or use UI placeholder models.

8. **Create Ktor client factory and GoREST API client**
   - Create: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/data/remote/HttpClientFactory.kt`.
   - Create: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/data/remote/GorestApiClient.kt`.
   - Configure `HttpClient` with `ContentNegotiation { json(Json { ignoreUnknownKeys = true; explicitNulls = false }) }`.
   - Keep `GorestApiClient` methods DTO-oriented and read-only:
     - `getUsers(page, perPage)`
     - `getUser(id)`
     - `getUserPosts(userId)`
     - `getUserTodos(userId)`
     - `getPost(id)`
     - `getPostComments(postId)`
   - Use `https://gorest.co.in/public/v2` as the default base URL and allow it to be overridden for tests.
   - For user pagination, read `x-pagination-page`, `x-pagination-per-page`, and `x-pagination-pages` headers if available; otherwise fall back to the requested page/perPage and `hasNextPage = false`.

9. **Create repository implementations and error mapping**
   - Create under `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/data/repository/`:
     - `UserRepositoryImpl.kt`
     - `PostRepositoryImpl.kt`
     - optionally `RepositoryErrorMapper.kt` if it keeps code tidy.
   - Repositories call `GorestApiClient`, map DTOs with `toDomain()`, and wrap results in `AppResult`.
   - Catch Ktor response exceptions and map status codes:
     - 401/403 → `Unauthorized`
     - 404 → `NotFound`
     - 422 → `Validation(...)`
     - 500+ → `Server(statusCode)`
   - Catch timeout/network exceptions as `Timeout`/`Network` where practical; otherwise fall back to `Unknown` with a safe message.
   - Avoid adding dependency injection frameworks. Later app wiring can manually instantiate repositories or use a tiny app container.

10. **Add fixture JSON files for realistic API shapes**
    - Create under `SliidePeek/shared/src/commonTest/resources/`:
      - `users/users_page_1.json`
      - `users/user_detail.json`
      - `posts/user_posts.json`
      - `posts/post_detail.json`
      - `comments/post_comments.json`
      - `todos/user_todos.json`
    - Keep fixtures short but realistic and shaped like GoREST arrays/objects.
    - Do not include real tokens, personal data, or secrets.

11. **Add mapper and API/repository tests**
    - Create under `SliidePeek/shared/src/commonTest/kotlin/com/sliide/useractivity/data/remote/mapper/`:
      - `UserMapperTest.kt`
      - `PostMapperTest.kt`
      - `CommentMapperTest.kt`
      - `TodoMapperTest.kt`
    - Create: `SliidePeek/shared/src/commonTest/kotlin/com/sliide/useractivity/data/remote/GorestApiClientTest.kt`.
    - Create repository tests if the error wrapping is non-trivial:
      - `SliidePeek/shared/src/commonTest/kotlin/com/sliide/useractivity/data/repository/UserRepositoryImplTest.kt`
      - `SliidePeek/shared/src/commonTest/kotlin/com/sliide/useractivity/data/repository/PostRepositoryImplTest.kt`
    - Use `kotlin.test`, `runTest`, Ktor `MockEngine`, and fixture JSON.
    - Test at minimum: successful users parsing, user detail parsing, posts/comments/todos parsing, 404 mapping, 422 mapping, and network exception mapping.
    - If direct common-test resource loading is awkward in the current KMP setup, add a tiny test fixture helper and keep large JSON in fixture files as the source of truth.

12. **Add a small app data container for later UI integration**
    - Create: `SliidePeek/shared/src/commonMain/kotlin/com/sliide/useractivity/data/AppDataContainer.kt`.
    - Expose `userRepository` and `postRepository` built from one configured `HttpClient`/`GorestApiClient`.
    - Do not connect it to `AppRoot()` yet unless needed for compilation; ViewModel/UI integration belongs to roadmap 12.4/12.5.

13. **Update project README with data-layer status**
    - Touch: `SliidePeek/README.md`.
    - Add a short note that the project now has a read-only GoREST domain/data layer with Ktor, DTOs, mappers, repositories, simple error handling, and fixture-based tests.
    - Keep it factual and brief; do not document UI polish or features not implemented yet.

## Verification

Run from `SliidePeek/`:

- `./gradlew :shared:compileKotlinMetadata` succeeds.
- `./gradlew :shared:allTests` succeeds, or run the available common/host test tasks and note any local native simulator/toolchain limitation.
- `./gradlew :androidApp:assembleDebug` succeeds to confirm Android engine wiring compiles.
- `./gradlew :shared:linkDebugFrameworkIosSimulatorArm64` succeeds on macOS with the iOS toolchain installed to confirm Darwin engine wiring compiles.
- Test review: mapper tests cover every DTO type; API/repository tests use Ktor MockEngine and realistic fixture JSON; no test depends on the live GoREST service.
- Architecture review: DTOs remain internal to the data layer; repository interfaces return domain models and `AppResult`, not Ktor responses/exceptions.
- Scope review: no high-fidelity UI, ViewModel screens, auth-token write operations, SQLDelight cache, or dependency-injection framework has been added.

## Risks

- Ktor/Compose/Kotlin versions must be compatible with Kotlin `2.3.21`; choose a current Ktor version that resolves cleanly with the existing Gradle setup.
- KMP common-test resource loading may vary by target. Keep tests fixture-based where practical, but prefer reliable fast tests over a fragile resource loader.
- Network exception classes differ by platform. Keep error mapping simple and test deterministic cases with MockEngine.
- GoREST response values are strings and may include unexpected enum values. Mappers should avoid crashing on unknown status/gender/todo values.
- Authenticated create/edit/delete flows require a bearer token and are intentionally out of scope for this read-only data/domain plan.
