# Tasks: Remove HOUSEHOLD Sync Logging and Adopt Type-Safe Enums

**Feature**: Remove HOUSEHOLD Sync Logging and Adopt Type-Safe Enums  
**Spec**: [spec.md](file:///Users/snapp/AndroidStudioProjects/opensplit/specs/002-delete-household-log/spec.md)  
**Plan**: [plan.md](file:///Users/snapp/AndroidStudioProjects/opensplit/specs/002-delete-household-log/plan.md)  
**Status**: Ready for Implementation  

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Define common data structures, enums, and fake factories.

- [X] T001 Create sync enums `SyncEntityType` (with values `EXPENSE`, `MEMBERSHIP`) and `SyncOperation` (with values `INSERT`, `UPDATE`, `DELETE`) in [core/src/commonMain/kotlin/com/opensplit/dto/sync/SyncEnums.kt](file:///Users/snapp/AndroidStudioProjects/opensplit/core/src/commonMain/kotlin/com/opensplit/dto/sync/SyncEnums.kt)

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Core interface and schema changes that must be in place before updating repository and callers.

- [X] T002 Update `ChangeLog` table in [server/src/main/kotlin/com/opensplit/database/Tables.kt](file:///Users/snapp/AndroidStudioProjects/opensplit/server/src/main/kotlin/com/opensplit/database/Tables.kt) to use `enumerationByName("entity_type", 50, SyncEntityType::class)` and `enumerationByName("operation", 20, SyncOperation::class)`
- [X] T003 Update `SyncRepository` interface in [server/src/main/kotlin/com/opensplit/features/sync/SyncRepository.kt](file:///Users/snapp/AndroidStudioProjects/opensplit/server/src/main/kotlin/com/opensplit/features/sync/SyncRepository.kt) to accept `entityType: SyncEntityType` and `operation: SyncOperation` instead of strings

**Checkpoint**: Foundation ready - compiler will guide updates in `SyncRepositoryImpl` and all calling repositories.

---

## Phase 3: User Story 1 - Type-Safe Server Change Recording and Household Log Removal (Priority: P1) 🎯 MVP

**Goal**: Update `SyncRepositoryImpl`, `ExpenseRepositoryImpl`, and `GroupRepositoryImpl` to use `SyncEntityType` and `SyncOperation` while completely removing calls to `recordChange("HOUSEHOLD")`.

**Independent Test**: Run `./gradlew :server:test` and verify that group creation and expense operations persist rows to `ChangeLog` with valid enum values and emit zero rows for `HOUSEHOLD`.

### Implementation for User Story 1

- [X] T004 [US1] Update `SyncRepositoryImpl` in [server/src/main/kotlin/com/opensplit/features/sync/SyncRepositoryImpl.kt](file:///Users/snapp/AndroidStudioProjects/opensplit/server/src/main/kotlin/com/opensplit/features/sync/SyncRepositoryImpl.kt) to record changes using `SyncEntityType` and `SyncOperation`, update corresponding version tables (`EXPENSE` -> `Expenses`, `MEMBERSHIP` -> `Memberships`), and remove the obsolete `"HOUSEHOLD"` branch
- [X] T005 [P] [US1] Update `ExpenseRepositoryImpl` in [server/src/main/kotlin/com/opensplit/features/expense/ExpenseRepositoryImpl.kt](file:///Users/snapp/AndroidStudioProjects/opensplit/server/src/main/kotlin/com/opensplit/features/expense/ExpenseRepositoryImpl.kt) to remove all `syncRepository.recordChange("HOUSEHOLD", ...)` invocations while preserving `Groups.lastInteractionAt` updates, and use `SyncEntityType.EXPENSE` with `SyncOperation.INSERT`, `SyncOperation.UPDATE`, and `SyncOperation.DELETE`
- [X] T006 [P] [US1] Update `GroupRepositoryImpl` in [server/src/main/kotlin/com/opensplit/features/group/GroupRepositoryImpl.kt](file:///Users/snapp/AndroidStudioProjects/opensplit/server/src/main/kotlin/com/opensplit/features/group/GroupRepositoryImpl.kt) to remove `syncRepository.recordChange("HOUSEHOLD", targetGroupId, "INSERT")` while preserving `Groups.lastInteractionAt` setting, and use `SyncEntityType.MEMBERSHIP` with `SyncOperation.INSERT` and `SyncOperation.DELETE`

**Checkpoint**: User Story 1 is complete. No `HOUSEHOLD` change log records are generated anywhere, and all change log entries use type-safe enums.

---

## Phase 4: User Story 2 - Accurate Incremental Client Synchronization (Priority: P2)

**Goal**: Ensure the sync query logic in `SyncRepositoryImpl` filters deleted and changed entities cleanly using the new `SyncEntityType` and `SyncOperation` enums, and verify that sync routes pass tests.

**Independent Test**: Run `./gradlew :server:test --tests "com.opensplit.features.SyncRoutesTest"` to verify end-to-end sync flows between clients.

### Implementation for User Story 2

- [X] T007 [US2] Update `getChanges` query in [server/src/main/kotlin/com/opensplit/features/sync/SyncRepositoryImpl.kt](file:///Users/snapp/AndroidStudioProjects/opensplit/server/src/main/kotlin/com/opensplit/features/sync/SyncRepositoryImpl.kt) to query deleted expenses using `ChangeLog.entityType eq SyncEntityType.EXPENSE` and `ChangeLog.operation eq SyncOperation.DELETE`
- [X] T008 [US2] Verify and run sync integration tests in [server/src/test/kotlin/com/opensplit/features/SyncRoutesTest.kt](file:///Users/snapp/AndroidStudioProjects/opensplit/server/src/test/kotlin/com/opensplit/features/SyncRoutesTest.kt) to ensure multi-user sync flows (create, update, delete) function accurately without regressions

**Checkpoint**: Incremental synchronization is fully operational and passes all end-to-end tests.

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Documentation updates, cleanup, and repository verification gate.

- [X] T009 Update docs in [docs/expense-sync-spec.md](file:///Users/snapp/AndroidStudioProjects/opensplit/docs/expense-sync-spec.md) and [todo.md](file:///Users/snapp/AndroidStudioProjects/opensplit/todo.md) marking item 2 ("delete recordChange(\"HOUSEHOLD\") and use enum instead of string") as done
- [X] T010 Run repository-wide verification gate: `./gradlew jvmTest test ktfmtFormat --offline`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Can start immediately.
- **Foundational (Phase 2)**: Depends on Phase 1 (requires `SyncEntityType` & `SyncOperation`).
- **User Story 1 (Phase 3)**: Depends on Phase 2.
- **User Story 2 (Phase 4)**: Depends on Phase 3.
- **Polish (Phase 5)**: Depends on all user stories being complete.

### Parallel Opportunities

- Within Phase 3: T005 (`ExpenseRepositoryImpl`) and T006 (`GroupRepositoryImpl`) can be executed in parallel after T004 (`SyncRepositoryImpl`).

---

## Implementation Strategy

### MVP First (User Story 1)
1. Implement Phase 1 (`SyncEnums.kt`).
2. Implement Phase 2 (`Tables.kt` and `SyncRepository.kt`).
3. Implement Phase 3 (`SyncRepositoryImpl.kt`, `ExpenseRepositoryImpl.kt`, `GroupRepositoryImpl.kt`).
4. Validate that compiling and running repository tests records enums and does not produce `HOUSEHOLD` logs.

### Incremental Delivery
1. Foundation & MVP: Change logging is type-safe and household log records are completely removed.
2. US2 Sync endpoint query: `/sync?sinceVersion=N` reliably filters on `SyncEntityType.EXPENSE` and `SyncOperation.DELETE`.
3. Polish: Update docs and pass the repository-wide gate `./gradlew jvmTest test ktfmtFormat --offline`.
