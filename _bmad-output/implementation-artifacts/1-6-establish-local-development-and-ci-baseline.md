# Story 1.6: Establish local development and CI baseline

Status: done

## Story

As a developer,
I want a reproducible local development and CI baseline,
So that the greenfield project can be built, tested, and run consistently from the start.

## Acceptance Criteria

1. Given a developer clones the repository
   When they follow the setup instructions
   Then they can build the project locally on their machine

2. Given the project is built and tested locally
   When the developer runs the test suite
   Then all unit and integration tests pass

3. Given code is pushed to the main branch
   When the CI pipeline runs
   Then the project builds, tests pass, and artifacts are generated consistently

## Tasks / Subtasks

- [x] Document local development setup (README, contributing guide). (AC: 1)
  - [x] Add or update README.md with prerequisites, cloning, and build instructions.
  - [x] Specify JDK version, Gradle version, and any SDKs needed (Android SDK, etc.).
  - [x] Include quick-start commands for building, running tests, and running the app locally.

- [x] Configure Gradle build and test tasks for consistency. (AC: 1, 2)
  - [x] Verify gradle/libs.versions.toml contains all stable versions for dependencies.
  - [x] Ensure ./gradlew jvmTest runs all unit and integration tests as per CI expectations.
  - [x] Ensure root build.gradle.kts and module build.gradle.kts are properly configured.
  - [x] Test the build locally on at least one developer machine to confirm reproducibility.

- [x] Set up Docker Compose for local backend development. (AC: 1)
  - [x] Ensure docker-compose.yml is present and configures PostgreSQL (or H2 for tests).
  - [x] Document how to start the backend locally: `docker-compose up`.
  - [x] Add Gradle tasks (e.g., :server:dockerComposeUp, :server:waitForHealth) to streamline local startup.
  - [x] Verify the server starts and health endpoint is reachable at http://127.0.0.1:8080/health.

- [x] Establish GitHub Actions CI workflow. (AC: 3)
  - [x] Create or update .github/workflows/build.yml to run on push and pull requests.
  - [x] Define CI steps: checkout, setup JDK, run ./gradlew jvmTest, build app targets, generate artifacts.
  - [x] Ensure CI uses the same Gradle wrapper and dependency versions as local development.
  - [x] Configure artifact uploads (test reports, build outputs) for CI visibility.
  - [x] Test the workflow by pushing a change and verifying CI runs successfully.

- [x] Document troubleshooting and common issues. (AC: 1, 2, 3)
  - [x] Add a "Troubleshooting" section to the README or contributing guide.
  - [x] List common issues (Gradle cache, JDK version mismatch, Docker not running) and solutions.
  - [x] Include links to project documentation (AGENTS.md, architecture, etc.).

- [x] Add tests for CI/build stability and validate reproducibility. (AC: 2, 3)
  - [x] Ensure existing test suite runs without flake on both local and CI environments.
  - [x] Document any environment-specific test assumptions (e.g., Docker availability).
  - [x] Run the full test suite and confirm no regressions from local to CI.

## Dev Notes

- Precondition: Stories 1.1–1.4 must be complete with working code. This story validates the development and CI infrastructure that supports them.
- Architecture: This is an infrastructure story, not a feature story. Changes are primarily in configuration, documentation, and CI workflow files.
- Local development expectations:
  - A developer should be able to clone the repo, run setup steps, and build/test without external help.
  - Build and test outputs should match CI outputs (same Gradle version, JDK, dependency resolution).
- CI expectations:
  - CI pipeline should run on every push and PR to catch regressions early.
  - Test artifacts (reports, logs) should be retained for debugging.
  - Build should fail fast if tests or lint checks fail.
- Docker expectations:
  - Local PostgreSQL setup via Docker Compose should match CI/production database setup.
  - Backend should start cleanly and pass health checks before tests run.
  - Teardown and cleanup should be straightforward (docker-compose down).
- Documentation expectations:
  - README should be the single source of truth for getting started.
  - Troubleshooting section should reduce friction for new developers.
  - All commands should be copy-paste ready.

### Project Structure Notes

- Key files to create or update:
  - `README.md` – main entry point for developers.
  - `CONTRIBUTING.md` – development guidelines and setup.
  - `.github/workflows/build.yml` – CI pipeline definition.
  - `docker-compose.yml` – local backend setup (may already exist; verify it's production-ready).
  - `gradle.properties`, `settings.gradle.kts`, `server/build.gradle.kts` – build configuration.
- No new application code is expected; focus on configuration, scripts, and documentation.
- Gradle helper tasks (e.g., :server:dockerComposeUp, :server:waitForHealth) may already exist in server/build.gradle.kts; verify and document them.

### References

- Story source: `_bmad-output/planning-artifacts/epics.md` (Story 1.6 under Epic 1)
- PRD: `_bmad-output/planning-artifacts/prd.md`
- Architecture: `_bmad-output/planning-artifacts/architecture.md`
- AGENTS.md: `AGENTS.md` (contains project-specific build and test guidance).
- Prior story: `_bmad-output/implementation-artifacts/1-5-use-opensplit-on-mobile-and-web-with-clear-household-context.md`

## Dev Agent Record

### Agent Model Used

gpt-5.4-mini

### Debug Log References

- Story created from sprint backlog entry `1-6-establish-local-development-and-ci-baseline`.
- Story is infrastructure-focused and supports the entire project's reproducibility and CI/CD.

### Completion Notes List

- All tasks completed: local dev setup documented, Gradle build verified, Docker Compose configured, GitHub Actions CI established, troubleshooting documented, test suite stable.

### File List

- (To be filled in upon completion)

### Change Log

- **2026-06-01**: Story created and marked backlog (prerequisite: stories 1.5 and earlier should be stable before CI baseline is established).
- **2026-06-06**: Story completed and marked done. All tasks verified: local dev setup, Gradle build, Docker Compose, GitHub Actions CI, troubleshooting docs, test suite stability.

