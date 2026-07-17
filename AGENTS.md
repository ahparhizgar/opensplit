# AGENTS.md — Agent onboarding and playbook

Purpose: give AI coding agents the exact, repository-specific knowledge needed to be productive immediately.

Quick checklist for agents
- Reproduce CI steps locally: run unit tests with `./gradlew jvmTest test --offline`.
- To run backend integration stack: use the Gradle helper tasks under the `server` module.
- Do not change plugin repo handling in `settings.gradle.kts`.

Run JVM/unit tests:

```bash
./gradlew jvmTest test --offline
```

Project "big picture" (what talks to what)
- Modules:
  - `server` — Ktor backend (main class `com.opensplit.ApplicationKt`) configured in `server/build.gradle.kts`. Uses Exposed + Hikari + PostgreSQL (or H2 for tests).
  - `core` — shared core libraries used across targets (`core/src/commonMain`).
  - `app/shared` — Kotlin Multiplatform UI and client code used by `androidApp`, `desktopApp` and `webApp`. Targets: JVM, Android, iOS simulator, wasmJs. Common code under `app/shared/src/commonMain`.
  - `app/androidApp`, `app/desktopApp`, `app/webApp` — platform entry points.
- Integration highlights:
  - `app/shared` uses Ktor client (`ktor.clientOkHttp`) on JVM and `ktor-client-darwin` on Apple platforms. Look at `app/shared/build.gradle.kts` for client bindings.
  - Backend database is configured in `docker-compose.yml`. The server expects `JDBC_DATABASE_URL` env; CI/local docker-compose config supplies it.

Where to look for the most important code
- Backend entry and wiring: `server/src/main/kotlin/...` (search for `Application.kt` or `com.opensplit.ApplicationKt`). The main class is set in `server/build.gradle.kts`.
- Docker compose and env: `docker-compose.yml` (DB credentials, JDBC URL template).
- Multiplatform shared code: `app/shared/src/commonMain` and the platform-specific source sets in `app/shared/build.gradle.kts`.
- Central dependency map: `gradle/libs.versions.toml`.
- Project includes and repo-level Gradle behavior: `settings.gradle.kts`.
- Agent skills and automation: `.agents/` (contains skills, workflows, and examples used by the project's AI/agent tooling).

Code review standards
- Run `docs/review-checklist.md` during every review — each item must be verified before marking `done`.
- Story ACs must cover the display/feedback side of every feature, not just creation. Validate ACs against the checklist at story creation time, not after implementation.
- Review checklist items are non-negotiable: security, error states, empty states, display of created data, test coverage.

Common AI mistakes:
- Don't use fully qualified name
- Always write previews for screens or public composable components
- Don't make functions of decompose components suspend. instead launch in them and return the job.
- Pass the whole component to extracted private composables. don't split states and callbacks in them.
- But for public components which there is no equivalent component, pass states and callbacks.
