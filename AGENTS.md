# AGENTS.md — Agent onboarding and playbook

Purpose: give AI coding agents the exact, repository-specific knowledge needed to be productive immediately.

Quick checklist for agents
- Read these files first: `settings.gradle.kts`, `gradle/libs.versions.toml`, `server/build.gradle.kts`, `docker-compose.yml`, `app/shared/build.gradle.kts`, `core/build.gradle.kts`, `.agents/`.
- Reproduce CI steps locally: run unit tests with `./gradlew jvmTest` (CI uses this exact command).
- To run backend integration stack: use the Gradle helper tasks under the `server` module (see examples below).
- Do not change plugin repo handling in `settings.gradle.kts` (there is an explicit warning in the file).

Quick-start commands (examples)

- Run JVM/unit tests (same as CI):

```bash
./gradlew jvmTest
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
  - Health endpoint: `http://localhost:8080/health` (polled by `:server:waitForHealth`).

Project-specific conventions & gotchas
- Centralized versions & plugin aliases: `gradle/libs.versions.toml` defines dependency versions and plugin alias names. Root `build.gradle.kts` uses `alias(libs.plugins.*)`; follow these aliases when inspecting builds.
- GitHub Actions manipulates Gradle wrapper properties: `.github/workflows/build.yml` contains a step that replaces `maven.myket.ir/gradle` with `services.gradle.org`. CI expects that replacement; locally you might need to mirror the same change if your wrapper points to custom hosts.
- Docker tasks are implemented in `server/build.gradle.kts`: `dockerComposeUp`, `dockerComposeDown`, `waitForHealth`, `startBackend`. Prefer these tasks to reproduce CI/local development workflows instead of ad-hoc docker commands.
- Shadow JAR naming: the fat JAR is explicitly set to `server-fat.jar` by the `ShadowJar` config in `server/build.gradle.kts`.
- Tests and CI artifacts: CI uploads `**/build/reports/tests/` (see workflow). Use that path when locating reports.

Where to look for the most important code
- Backend entry and wiring: `server/src/main/kotlin/...` (search for `Application.kt` or `com.opensplit.ApplicationKt`). The main class is set in `server/build.gradle.kts`.
- Docker compose and env: `docker-compose.yml` (DB credentials, JDBC URL template).
- Multiplatform shared code: `app/shared/src/commonMain` and the platform-specific source sets in `app/shared/build.gradle.kts`.
- Central dependency map: `gradle/libs.versions.toml`.
- Project includes and repo-level Gradle behavior: `settings.gradle.kts`.
- Agent skills and automation: `.agents/` (contains skills, workflows, and examples used by the project's AI/agent tooling).

Useful examples found in the repo (copy-paste safe)
- Health check loop (server Gradle task): see `server/build.gradle.kts` — roughtly polls `http://localhost:8080/health` and times out after 2 minutes.

Agent operational rules (short)
- NEVER change `settings.gradle.kts` pluginManagement entries or remove the `IS_LOCAL` guard without human consent.
- Prefer repo-provided Gradle tasks for lifecycle actions (build, docker, start, waitForHealth, shadowJar) to match CI behavior.
- When adding or upgrading dependencies, update `gradle/libs.versions.toml` and the plugin aliases in the root `build.gradle.kts`.
- When running tests, use `./gradlew jvmTest` to match CI filtering and report generation.

Pointers for follow-up actions an agent may take
- If adding an integration test that requires DB: use `:server:dockerComposeUp` + `:server:waitForHealth` in your test setup and `:server:dockerComposeDown` in teardown.
- If you need to run a local migration or schema setup step, modify `server` tasks or add a dedicated Gradle task under `server/build.gradle.kts` so it integrates with the existing `dockerComposeUp` flow.

Last note
- The repository is a Kotlin Multiplatform project named `OpenSplit` (see `settings.gradle.kts`). Expect platform-specific build complexity; inspect `app/shared` and `core` first to understand shared business logic.

References (explicit files)
- `settings.gradle.kts`
- `gradle/libs.versions.toml`
- `server/build.gradle.kts`
- `docker-compose.yml`
- `app/shared/build.gradle.kts`
- `.github/workflows/build.yml`
- `.agents/`


