# Story 1.2: Create and sign in securely

Status: ready-for-dev

## Story

As a new or returning user,
I want to create an account and sign in securely,
so that I can access my households and expenses on mobile or web.

## Acceptance Criteria

1. Given a new user is on the sign-up screen, when they submit a valid email and password, then the account is created and the user is signed in.
2. Given a new user signs up, then only the minimum access information is required.
3. Given a returning user is on the sign-in screen, when they submit valid credentials, then the user is authenticated and routed to their household context.
4. Given the user enters invalid credentials or incomplete data, when they submit the form, then inline validation or an error message is shown.
5. Given the user submits invalid data, then no session is created.

## Tasks / Subtasks

- [x] Build the client auth entry flow. (AC: 1, 2, 3, 4, 5)
  - [x] Replace the placeholder `App()` experience with an auth-aware root flow.
  - [x] Add sign-up and sign-in screens with inline validation and clear error states.
  - [x] Keep the form minimal: email + password only for v1.
  - [x] Route successful auth into the protected post-auth household context shell, not into household creation logic.
- [x] Implement shared auth DTOs and validation. (AC: 1, 2, 4, 5)
  - [x] Add request/response models for sign-up, sign-in, and auth session state in `shared`.
  - [x] Reuse shared validation rules for email and password shape so client and server stay aligned.
  - [x] Keep the shared contract camelCase and JSON-friendly.
- [x] Implement server auth endpoints and session handling. (AC: 1, 3, 5)
  - [x] Add auth routes and service logic for account creation and sign-in.
  - [x] Create and persist secure sessions on successful auth.
  - [x] Reject invalid credentials without creating a session.
- [x] Add verification coverage. (AC: 1, 2, 3, 4, 5)
  - [x] Cover shared validation and auth DTO behavior.
  - [x] Cover server auth route success and failure cases.
  - [x] Cover the client validation and post-auth routing behavior.

## Dev Notes

- This story is only about secure account creation and sign-in. Do not implement household creation/join flows here; those belong to Story 1.3.
- Treat the post-auth destination as a protected household-context shell/safe landing state. If no household exists yet, show the authenticated landing state rather than inventing household setup in this story.
- Keep the UX minimal and fast. The product goal is the smallest viable auth form with clear inline validation and obvious success/failure feedback.
- Do not add unrelated auth providers, password reset, MFA, or guest access. The architecture explicitly defers expanded auth options.
- Reuse the existing Kotlin Multiplatform starter shape and extend it feature-first. Do not introduce a second flat root-package auth implementation.

### Technical Requirements

- Use the pinned project stack already in the repo: Kotlin 2.3.21, Compose Multiplatform 1.10.3, Ktor 3.4.3.
- Use the server-side Ktor auth/session facilities already supported by the chosen stack; do not upgrade libraries inside this story.
- Keep credentials and session secrets out of UI state and logs.
- Preserve household-scoped authorization assumptions from the architecture: auth establishes identity, later stories enforce household membership.
- Validation must happen both client-side for immediate feedback and server-side for trust.

### Architecture Compliance

- Follow the feature-first layout from the architecture:
  - `client/features/auth`
  - `server/features/auth`
  - `shared/dto`
  - `shared/validation`
- Keep client responsibilities to UI, local presentation state, and routing.
- Keep shared responsibilities to DTOs, validation, and cross-platform contract types.
- Keep server responsibilities to auth routes, session handling, and persistence.
- Use REST-style request/response shapes and camelCase JSON fields.
- Preserve immutable, explicit state flow.

### Library / Framework Requirements

- Compose Multiplatform for the auth screens and app shell.
- Ktor server routing for auth endpoints.
- Ktor authentication/session support for secure sign-in state.
- Shared Kotlin validation for consistent client/server checks.

### File Structure Requirements

- Update the existing root app entry at `client/src/commonMain/kotlin/com/opensplit/App.kt` or replace it with a thin auth-aware shell, but keep the app bootstrap path stable.
- Prefer new feature folders over expanding the placeholder starter package:
  - `client/src/commonMain/kotlin/com/opensplit/features/auth/...`
  - `server/src/main/kotlin/com/opensplit/features/auth/...`
  - `shared/src/commonMain/kotlin/com/opensplit/dto/...`
  - `shared/src/commonMain/kotlin/com/opensplit/validation/...`
- Keep tests close to the code they verify, with shared tests in `shared/src/commonTest`, client JVM/common tests in `client/src/*Test`, and server tests in `server/src/test`.

### Testing Requirements

- Verify valid sign-up creates an account and establishes a session.
- Verify valid sign-in authenticates and routes to the protected post-auth shell.
- Verify invalid credentials and incomplete input show inline validation or an error and do not create a session.
- Verify shared validation rejects malformed email/password input consistently on both sides.
- Use the existing Ktor test host pattern for server coverage.

### Previous Story Intelligence

- Story 1.1 established the KMP starter and the `client` / `server` / `shared` split.
- The prior story’s review notes flagged package-structure drift and generated artifacts as risks; keep this story aligned with the intended feature-first layout and avoid widening the starter scaffold unnecessarily.
- The current starter code still contains placeholder UI and root-package examples, so auth should be the first real feature slice to replace them.

### Git Intelligence

- Recent work pattern: starter scaffold, tests, then code review. Treat this story as a feature slice layered onto the starter rather than a framework reset.
- Recent commits show the repo already expects incremental KMP work; keep changes small and targeted.

### Latest Technical Information

- Ktor 3.4.3 is the pinned server version in this repo and is also the latest release surfaced during research.
- The Ktor project documents auth and sessions as first-class server plugins in the 3.4.x line; use those plugin conventions instead of ad hoc session plumbing.
- If docs and examples differ across versions, trust the repo-pinned Ktor 3.4.3 dependency and update only within the story’s scope.

### Project Structure Notes

- Existing code is still mostly starter scaffolding under `com.opensplit`.
- This story should introduce the first feature-first auth slice without disturbing the working starter boundaries.
- Do not rename the whole project structure just to satisfy auth; localize the change to the auth path and the root app entry.

### References

- [Source: `_bmad-output/planning-artifacts/epics.md`#Story 1.2]
- [Source: `_bmad-output/planning-artifacts/prd.md`#Executive Summary]
- [Source: `_bmad-output/planning-artifacts/prd.md`#Success Criteria]
- [Source: `_bmad-output/planning-artifacts/prd.md`#Functional Requirements]
- [Source: `_bmad-output/planning-artifacts/architecture.md`#Authentication & Security]
- [Source: `_bmad-output/planning-artifacts/architecture.md`#API & Communication Patterns]
- [Source: `_bmad-output/planning-artifacts/architecture.md`#Project Structure & Boundaries]
- [Source: `_bmad-output/planning-artifacts/architecture.md`#Implementation Patterns & Consistency Rules]
- [Source: `_bmad-output/planning-artifacts/ux-design-specification.md`#Core User Experience]
- [Source: `_bmad-output/planning-artifacts/ux-design-specification.md`#Effortless Interactions]
- [Source: `_bmad-output/planning-artifacts/ux-design-specification.md`#Component Strategy]
- [Source: `_bmad-output/planning-artifacts/ux-design-specification.md`#Form Patterns]
- [Source: `_bmad-output/implementation-artifacts/1-1-initialize-the-kotlin-multiplatform-starter-project.md`#Dev Notes]

## Dev Agent Record

### Agent Model Used

gpt-5.4-mini

### Debug Log References

- Story selected from `sprint-status.yaml` as the first backlog story in epic 1: `1-2-create-and-sign-in-securely`.
- Workflow customization resolved successfully; no prepend/append hooks were configured.
- No `project-context.md` file was present in the workspace, so the story was built from planning artifacts and current source files.
- Current codebase still contains starter placeholders in `client`, `server`, and `shared`; auth should be added as the first real feature slice.
- Implemented shared auth DTOs and validation, server auth routes with session handling, and a client auth shell/controller.
- Added a real client-side Ktor auth gateway with HTTP sign-up/sign-in calls.
- Added platform-specific Ktor HTTP client factories and a JVM network integration test.
- Added shared validation tests, server auth route tests, and client auth state tests.
- Verified the full regression suite with `./gradlew test`.

### Completion Notes List

- Created a comprehensive implementation guide for secure account creation and sign-in.
- Scoped the story to auth only and explicitly deferred household creation/joining to Story 1.3.
- Anchored the implementation to the existing KMP starter, the pinned Ktor 3.4.3 stack, and the feature-first architecture.
- Added `client/features/auth`, `server/features/auth`, `shared/dto/auth`, and `shared/validation/auth`.
- Added session-backed sign-up, sign-in, and household-context server endpoints.
- Added client-side real HTTP auth calls with Ktor and platform client factories.
- Added a JVM integration test covering the auth gateway network path.
- Verified implementation with `./gradlew :shared:jvmTest :client:jvmTest :server:test` and `./gradlew test`.

### File List

- `_bmad-output/implementation-artifacts/1-2-create-and-sign-in-securely.md`
- `client/src/commonMain/kotlin/com/opensplit/App.kt`
- `client/src/commonMain/kotlin/com/opensplit/features/auth/AuthController.kt`
- `client/src/commonMain/kotlin/com/opensplit/features/auth/AuthGateway.kt`
- `client/src/commonMain/kotlin/com/opensplit/features/auth/AuthUiLabels.kt`
- `client/src/commonMain/kotlin/com/opensplit/features/auth/AuthUi.kt`
- `client/src/commonTest/kotlin/com/opensplit/AppUiTest.kt`
- `client/src/commonTest/kotlin/com/opensplit/ComposeAppCommonTest.kt`
- `client/src/jvmMain/kotlin/com/opensplit/features/auth/AuthHttpClient.jvm.kt`
- `client/src/androidMain/kotlin/com/opensplit/features/auth/AuthHttpClient.android.kt`
- `client/src/iosMain/kotlin/com/opensplit/features/auth/AuthHttpClient.ios.kt`
- `client/src/jsMain/kotlin/com/opensplit/features/auth/AuthHttpClient.js.kt`
- `client/src/wasmJsMain/kotlin/com/opensplit/features/auth/AuthHttpClient.wasm.kt`
- `client/src/jvmTest/kotlin/com/opensplit/features/auth/AuthGatewayIntegrationTest.kt`
- `gradle/libs.versions.toml`
- `server/build.gradle.kts`
- `server/src/main/kotlin/com/opensplit/Application.kt`
- `server/src/main/kotlin/com/opensplit/features/auth/AuthModels.kt`
- `server/src/main/kotlin/com/opensplit/features/auth/AuthRoutes.kt`
- `server/src/test/kotlin/com/opensplit/features/auth/AuthRoutesTest.kt`
- `shared/build.gradle.kts`
- `shared/src/commonMain/kotlin/com/opensplit/dto/auth/AuthDtos.kt`
- `shared/src/commonMain/kotlin/com/opensplit/validation/auth/AuthValidation.kt`
- `shared/src/commonTest/kotlin/com/opensplit/dto/auth/AuthDtosTest.kt`
- `shared/src/commonTest/kotlin/com/opensplit/validation/auth/AuthValidationTest.kt`

### Change Log

- 2026-05-13: Added the initial KMP-style client/server/shared starter scaffold and shared module verification test.
- 2026-05-13: Verified the starter scaffold with the Gradle wrapper and marked the project ready for review.
- 2026-05-13: Converted the server module to a minimal Ktor JVM backend with health routing.
- 2026-05-13: Moved plugin and server dependency versions into `gradle/libs.versions.toml`.
- 2026-05-13: Added JVM client and server tests covering starter behavior and the Ktor health endpoint.
- 2026-05-13: Implemented secure auth flow, shared DTOs/validation, and session-backed server endpoints.
- 2026-05-13: Added auth validation, server route, and client state tests; full regression suite passed.
- 2026-05-13: Added a real client auth gateway with platform HTTP client factories and a JVM network integration test.

### Status

- review
