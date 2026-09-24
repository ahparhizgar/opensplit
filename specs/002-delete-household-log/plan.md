# Implementation Plan: Remove HOUSEHOLD Sync Logging and Adopt Type-Safe Enums

**Branch**: `002-delete-household-log` | **Date**: 2026-09-24 | **Spec**: [spec.md](file:///Users/snapp/AndroidStudioProjects/opensplit/specs/002-delete-household-log/spec.md)

**Input**: Feature specification from `/specs/002-delete-household-log/spec.md`

## Summary

Refactor the synchronization change tracking mechanism to use strongly-typed enums (`SyncEntityType` and `SyncOperation`) instead of raw strings, configure Exposed `ChangeLog` table using `enumerationByName`, and eliminate obsolete `recordChange("HOUSEHOLD", ...)` calls across repositories while preserving group last interaction timestamp updates.

## Technical Context

**Language/Version**: Kotlin 2.4.20 / Multiplatform (JVM, Android, iOS, Wasm)  
**Primary Dependencies**: Exposed 1.5.0 (`exposed-core`, `exposed-jdbc`), Ktor 3.6.0, HikariCP, Kotlinx Serialization  
**Storage**: PostgreSQL (Server production) / H2 (Server tests)  
**Testing**: Kotest, Ktor Server Test Host, JUnit 4  
**Target Platform**: Ktor Server (JVM)  
**Project Type**: Multi-module KMP application (`core`, `server`, `app/shared`, platform entrypoints)  
**Performance Goals**: Zero runtime overhead compared to string queries, type-safe database column mapping  
**Constraints**: Compatible with both PostgreSQL and H2 databases; maintain continuous passing verification gate  
**Scale/Scope**: Server change recording in `ExpenseRepositoryImpl`, `GroupRepositoryImpl`, and `SyncRepositoryImpl`  

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] UI - JetBrains Compose & Keyboard Rule: No Compose UI modifications required in this backend task.
- [x] Decompose Component Rules: No Decompose components affected.
- [x] Kotlin Time and Import Discipline: Uses `Clock.System.now().toEpochMilliseconds()`. No `Instant.now()`. No FQCN.
- [x] Dependency Injection: Koin bindings unchanged and intact.
- [x] Testing & Fake Factory Discipline: Fake factories will be updated/added for sync enums and DTOs if applicable.
- [x] Data Modeling & DTOs: Enums are strictly used instead of raw strings in data models and Exposed tables.
- [x] Verification Gate: Must pass `./gradlew jvmTest test ktfmtFormat --offline`.

## Project Structure

### Documentation (this feature)

```text
specs/002-delete-household-log/
├── spec.md              # Feature specification
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
└── contracts/           # Phase 1 output (/speckit-plan command)
    └── sync-interface.md
```

### Source Code (repository root)

```text
core/src/commonMain/kotlin/com/opensplit/
└── dto/sync/
    └── SyncEnums.kt                    # SyncEntityType and SyncOperation enums

server/src/main/kotlin/com/opensplit/
├── database/Tables.kt                  # ChangeLog table using enumerationByName
├── features/sync/
│   ├── SyncRepository.kt               # Updated to accept enums
│   └── SyncRepositoryImpl.kt           # Using enums, removed HOUSEHOLD branch
├── features/expense/
│   └── ExpenseRepositoryImpl.kt        # Removed HOUSEHOLD recordChange, use enums for EXPENSE
└── features/group/
    └── GroupRepositoryImpl.kt          # Removed HOUSEHOLD recordChange, use enums for MEMBERSHIP

server/src/test/kotlin/com/opensplit/
└── features/SyncRoutesTest.kt          # Route tests verifying sync flow
```

**Structure Decision**: Modify existing files in `core` and `server` modules without introducing redundant layers or architectural drift.

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|---|---|---|
| None | N/A | N/A |
