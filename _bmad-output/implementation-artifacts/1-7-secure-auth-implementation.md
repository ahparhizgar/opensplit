# Story 1.7: Secure Auth Implementation

Status: done

## Story

As a user,
I want my account creation and sign-in to use real cryptographic security,
so that my credentials and session are protected from compromise.

## Acceptance Criteria

1. Given a user signs up or signs in, when the server processes the request, then passwords are hashed with a slow, salted algorithm (bcrypt/Argon2) — not SHA-256.
2. Given a user authenticates successfully, when the server issues a token, then it is a real signed JWT with a cryptographically secure signature, a configurable expiry claim, and standard JWT structure.
3. Given a user is on the sign-in or sign-up screen, when they type their password, then the password field obscures input using `PasswordVisualTransformation`.
4. Given the backend service starts, when the JWT secret and database credentials are loaded, then they come from environment variables or a config file — not hardcoded default values.
5. Given a session token has expired, when the user makes an authenticated request, then the server rejects the request with 401 and the client can prompt re-authentication.
6. Given the existing auth tests pass before changes, when the security improvements are applied, then all existing auth route, validation, and component tests still pass.

## Tasks / Subtasks

- [x] Replace password hashing with bcrypt/Argon2 (AC: 1)
  - [x] Add a `PasswordHasher` interface in `server/features/auth/` with a bcrypt or Argon2 implementation
  - [x] Update `AuthService.signUp()` and `AuthService.signIn()` to use the new hasher
  - [x] Remove the raw `MessageDigest("SHA-256")` call from `AuthModels.kt`
- [x] Replace fake JWT with a real signed JWT (AC: 2)
  - [x] Add a `JwtService` that creates signed JWTs with `exp`, `iat`, `sub` claims using `auth0/java-jwt`
  - [x] Set a configurable expiry (e.g. 24h default) via `JWT_EXPIRY_MS` environment variable
  - [x] `JwtTokenService.issue()` now produces a verifiable, signed JWT
  - [x] `JwtService.verify(token)` implemented and used in `resolveUserIdFromToken` and `/household-context` route
- [x] Obscure password field in the auth UI (AC: 3)
  - [x] Add `PasswordVisualTransformation` and `keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)` to the password `OutlinedTextField` in `AuthUi.kt`
- [x] Externalize configuration (AC: 4)
  - [x] JWT secret (`JWT_SECRET`), token expiry (`JWT_EXPIRY_MS`), and database URL (`JDBC_DATABASE_URL`) read from environment variables
  - [x] Base URL externalized via `expect/actual getApiBaseUrl()` — env var on JVM, `10.0.2.2` on Android, default `127.0.0.1` on others
- [x] Add token expiry handling (AC: 5)
  - [x] On 401 response, clear stored token and throw `RemoteException` to trigger re-auth
  - [x] Pre-request JWT expiry check via `isJwtExpired()` utility that decodes JWT payload client-side
- [x] Verify existing test suite (AC: 6)
  - [x] Run `./gradlew jvmTest` and confirm all auth-related tests pass with the new implementation

## Dev Notes

- The existing auth flow and UX are complete and functional — this story only replaces the security primitives (hashing, JWT, password field, config) without changing the UI layout, validation, or navigation logic.
- Current `AuthModels.kt` has `JwtTokenService` and `AuthService` — do not delete these files, only replace their internal implementations.
- The `resolveUserIdFromToken` function in `HouseholdRoutes.kt:279-284` currently parses the fake JWT pattern `"jwt-$userId-$email"`. It must be updated to verify a real signed JWT.
- JWT secret should default to a known dev value but never default to a production-grade value — fail loud if not set in production.
- Use the existing auth test patterns: `AuthRoutesTest.kt`, `AuthValidationTest.kt`, `AuthComponentTest`, and `AuthScenarios`.

### Project Structure Notes

- Touch points:
  - `server/src/main/kotlin/com/opensplit/features/auth/AuthModels.kt` — replace hashing and JWT logic
  - `server/src/main/kotlin/com/opensplit/routes/HouseholdRoutes.kt` — update token resolution
  - `app/shared/src/commonMain/kotlin/com/opensplit/features/auth/AuthUi.kt` — obscure password field
  - `app/shared/src/commonMain/kotlin/com/opensplit/features/auth/AuthGateway.kt` — configurable URL
  - `app/shared/src/commonMain/kotlin/com/opensplit/features/household/HouseholdGateway.kt` — configurable URL
- No new module or package needed — all changes are within existing files.
- Do not change the auth DTOs (`AuthDtos.kt`) or validation (`AuthValidation.kt`).

### References

- [Source: `_bmad-output/implementation-artifacts/1-2-create-and-sign-in-securely.md`] — Original story with security gaps
- [Source: `server/src/main/kotlin/com/opensplit/features/auth/AuthModels.kt`] — Fake JWT and SHA-256 hashing
- [Source: `app/shared/src/commonMain/kotlin/com/opensplit/features/auth/AuthUi.kt:187-196`] — Unobscured password field
- [Source: `app/shared/src/commonMain/kotlin/com/opensplit/features/auth/AuthGateway.kt:73`] — Hardcoded backend URL
- [Source: `server/src/main/kotlin/com/opensplit/routes/HouseholdRoutes.kt:279-284`] — Fake JWT token resolution
- [Source: `_bmad-output/planning-artifacts/prd.md#Success Criteria`] — NFR6: Account access securely handled
- [Source: `_bmad-output/planning-artifacts/epics.md`] — FR32: Sign in securely, FR33: Data private by default

## Dev Agent Record

### Agent Model Used

gpt-5.4-mini

### Debug Log References

- Gap found during code review of Story 1.2 (marked done but security primitives are placeholders)

### Completion Notes List

- Replaced SHA-256 password hashing with bcrypt (at.favre.lib:bcrypt) via new `PasswordHasher` interface and `BcryptPasswordHasher` implementation
- Replaced fake JWT (`"jwt-$userId-$email"` string interpolation) with real signed JWTs using auth0/java-jwt library (`JwtService`)
- Updated `resolveUserIdFromToken` in `HouseholdRoutes.kt` and `/household-context` route in `AuthRoutes.kt` to use `JwtTokenService.verify()`
- JWT secret and expiry configurable via `JWT_SECRET` and `JWT_EXPIRY_MS` environment variables
- Added `PasswordVisualTransformation` and password `KeyboardType` to password field in `AuthUi.kt`
- Externalized base URL via `expect/actual getApiBaseUrl()` pattern — reads `API_BASE_URL` env var on JVM, defaults to `127.0.0.1:8080` (or `10.0.2.2:8080` for Android emulator)
- Added token expiry handling: `isJwtExpired()` utility decodes JWT payload client-side to check expiry before requests; 401 responses clear stored token
- Added `clearAccessToken()` to `TokenStorage` interface and implementations
- All existing and new tests pass (29 tests total across server and shared modules)

### File List

- `gradle/libs.versions.toml` — added `bcrypt` version and library entry
- `server/build.gradle.kts` — added bcrypt dependency
- `server/src/main/kotlin/com/opensplit/features/auth/AuthModels.kt` — added `PasswordHasher` interface, `BcryptPasswordHasher`, `JwtService` class; updated `AuthService` to use bcrypt; replaced `JwtTokenService.issue()` with real signed JWT
- `server/src/main/kotlin/com/opensplit/features/auth/AuthModule.kt` — simplified to use `factory { AuthService() }` with default constructor
- `server/src/main/kotlin/com/opensplit/features/auth/AuthRoutes.kt` — replaced regex-based token parsing with `JwtTokenService.verify()`
- `server/src/main/kotlin/com/opensplit/routes/HouseholdRoutes.kt` — replaced regex-based `resolveUserIdFromToken` with `JwtTokenService.verify()`
- `server/src/test/kotlin/com/opensplit/features/auth/PasswordHasherTest.kt` — NEW
- `server/src/test/kotlin/com/opensplit/features/auth/JwtServiceTest.kt` — NEW
- `app/shared/src/commonMain/kotlin/com/opensplit/features/auth/AuthUi.kt` — added `PasswordVisualTransformation` and password `KeyboardOptions`
- `app/shared/src/commonMain/kotlin/com/opensplit/features/auth/AuthGateway.kt` — added `expect fun getApiBaseUrl()`; factory uses it
- `app/shared/src/commonMain/kotlin/com/opensplit/features/auth/TokenStorage.kt` — added `clearAccessToken()` method
- `app/shared/src/commonMain/kotlin/com/opensplit/features/auth/JwtUtils.kt` — NEW: JWT expiry check utility with platform-specific base64 and time
- `app/shared/src/commonMain/kotlin/com/opensplit/features/household/HouseholdGateway.kt` — uses `getApiBaseUrl(); added token expiry check and 401 handling
- `app/shared/src/commonMain/kotlin/com/opensplit/datastore/TokenStorage.kt` — added `clearAccessToken()` implementation
- `app/shared/src/jvmMain/kotlin/com/opensplit/features/auth/AuthHttpClient.jvm.kt` — added `actual fun getApiBaseUrl()`, `platformDecodeBase64`, `currentTimeSeconds`
- `app/shared/src/androidMain/kotlin/com/opensplit/features/auth/AuthHttpClient.android.kt` — same additions
- `app/shared/src/iosMain/kotlin/com/opensplit/features/auth/AuthHttpClient.ios.kt` — same additions
- `app/shared/src/jsMain/kotlin/com/opensplit/features/auth/AuthHttpClient.js.kt` — same additions
- `app/shared/src/wasmJsMain/kotlin/com/opensplit/features/auth/AuthHttpClient.wasm.kt` — same additions
- `app/shared/src/commonTest/kotlin/com/opensplit/RootComponentTest.kt` — added `clearAccessToken()` to test TokenStorage

## Change Log

- 2026-06-06: Implemented secure auth — bcrypt hashing, real signed JWTs, obscured password field, externalized config, token expiry handling. All 6 acceptance criteria satisfied. All existing tests pass.
