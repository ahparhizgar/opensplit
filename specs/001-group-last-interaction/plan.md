# Implementation Plan: Group Last Interaction At

**Branch**: `001-group-last-interaction` | **Date**: 2026-09-24 | **Spec**: [spec.md](file:///Users/snapp/AndroidStudioProjects/opensplit/specs/001-group-last-interaction/spec.md)

**Input**: Feature specification from `/specs/001-group-last-interaction/spec.md`

## Summary

Enable the backend and local offline database to track, persist, and expose `Group.lastInteractionAt` so that the client group list screen displays recently active groups (interaction within 7 days) in the active section even if their net balance is 0.0 (settled).

## Technical Context

**Language/Version**: Kotlin 2.1.10 / Multiplatform (JVM, Android, iOS, Wasm)  
**Primary Dependencies**: Ktor 3.x, Exposed, HikariCP, AndroidX Room 3 (Room KMP), KotlinX Serialization, Decompose 3.x, Compose Multiplatform  
**Storage**: PostgreSQL (Server production) / H2 (Server tests) / SQLite via Room KMP (Client offline)  
**Testing**: Kotest, Ktor Server Test Host, Compose UI Test  
**Target Platform**: Ktor Server (JVM), Android, Desktop (JVM), Web (Wasm)  
**Project Type**: Multi-module KMP application (`server`, `core`, `app/shared`, platform entrypoints)  
**Performance Goals**: Instant offline UI partition (<16ms frame), low-overhead timestamp updates during expense mutations  
**Constraints**: Offline-first (local room persistence matches server sync), backward-compatible schema defaults  
**Scale/Scope**: All group and expense lifecycle operations across server and client  

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

- [x] UI - JetBrains Compose & Keyboard Rule: Uses Surface/Scaffold, previews with OpenSplitTheme, keyboard actions intact.
- [x] Decompose Component Rules: Components expose Value state without suspend functions in decompose public APIs.
- [x] Kotlin Time and Import Discipline: Uses `Clock.System.now()` / `Clock.System.now().toEpochMilliseconds()`. No `Instant.now()` or `System.currentTimeMillis()`. No FQCN.
- [x] Dependency Injection: Koin bindings follow standards.
- [x] Testing & Fake Factory Discipline: `FakeGroupDtoFactory` and `FakeGroupFactory` updated with default `lastInteractionAt`.
- [x] Data Modeling & DTOs: DTOs use proper types; single concept per file discipline maintained.
- [x] Verification Gate: Will pass `./gradlew jvmTest test ktfmtFormat --offline`.

## Project Structure

### Documentation (this feature)

```text
specs/001-group-last-interaction/
├── spec.md              # Feature specification
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md        # Phase 1 output (/speckit-plan command)
├── quickstart.md        # Phase 1 output (/speckit-plan command)
└── contracts/           # Phase 1 output (/speckit-plan command)
    └── group-api.md
```

### Source Code (repository root)

```text
core/src/commonMain/kotlin/com/opensplit/
├── domain/Group.kt                     # Group & FakeGroupFactory with lastInteractionAt
└── dto/group/GroupDto.kt               # GroupDto & FakeGroupDtoFactory with lastInteractionAt

server/src/main/kotlin/com/opensplit/
├── database/Tables.kt                  # Groups table with lastInteractionAt column
├── features/group/
│   ├── GroupModels.kt                  # GroupRecord with lastInteractionAt
│   ├── GroupRepositoryImpl.kt          # Insert/read lastInteractionAt
│   └── GroupService.kt                 # Map lastInteractionAt to GroupDto
└── features/expense/
    └── ExpenseRepositoryImpl.kt        # Update Groups.lastInteractionAt on expense create/update/delete

app/shared/src/commonMain/kotlin/com/opensplit/
├── db/
│   ├── Entities.kt                     # GroupEntity with lastInteractionAtEpochMillis
│   ├── DAOs.kt                         # GroupDao query for updating interaction timestamp
│   ├── Mappers.kt                      # Mapping between Entity, DTO, and Domain
│   └── AppDatabase.kt                  # Version bump / schema update
├── repository/
│   ├── GroupRepository.kt             # Ingest lastInteractionAt from DTO
│   └── ExpenseRepository.kt           # Local optimistic lastInteractionAt update on expense create/update/delete
└── features/group/my/
    └── MyGroupListScreen.kt            # Group partition logic already aligns with 7.days
```

**Structure Decision**: Multiplatform modular architecture using existing modules (`core`, `server`, `app/shared`).

## Complexity Tracking

| Violation | Why Needed | Simpler Alternative Rejected Because |
|---|---|---|
| None | N/A | N/A |
