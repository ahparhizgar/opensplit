# Tasks: Group Last Interaction At

**Feature Branch**: `001-group-last-interaction`
**Spec**: [spec.md](file:///Users/snapp/AndroidStudioProjects/opensplit/specs/001-group-last-interaction/spec.md)
**Plan**: [plan.md](file:///Users/snapp/AndroidStudioProjects/opensplit/specs/001-group-last-interaction/plan.md)
**Status**: Ready for Implementation

---

## Phase 1: Setup (Shared Infrastructure & Core Contracts)

**Purpose**: Update shared core contracts, DTOs, domain models, and fake factories across modules.

- [X] T001 [P] Add `lastInteractionAt: Instant = Instant.DISTANT_PAST` and update `FakeGroupDtoFactory.create` with default `lastInteractionAt` in [core/src/commonMain/kotlin/com/opensplit/dto/group/GroupDto.kt](file:///Users/snapp/AndroidStudioProjects/opensplit/core/src/commonMain/kotlin/com/opensplit/dto/group/GroupDto.kt)
- [X] T002 [P] Update `FakeGroupFactory.create` in [core/src/commonMain/kotlin/com/opensplit/domain/Group.kt](file:///Users/snapp/AndroidStudioProjects/opensplit/core/src/commonMain/kotlin/com/opensplit/domain/Group.kt) to accept `lastInteractionAt: Instant = Instant.DISTANT_PAST` parameter defaulting to `Instant.DISTANT_PAST`

---

## Phase 2: Foundational (Backend & Local DB Schema Prerequisites)

**Purpose**: Database schema, persistence models, and DAO prerequisites required before story implementation.

**⚠️ CRITICAL**: Must complete before user story logic can be wired up.

- [X] T003 Add `val lastInteractionAt = long("last_interaction_at").default(0L)` column with default `0L` to `Groups` table in [server/src/main/kotlin/com/opensplit/database/Tables.kt](file:///Users/snapp/AndroidStudioProjects/opensplit/server/src/main/kotlin/com/opensplit/database/Tables.kt)
- [X] T004 [P] Add `lastInteractionAt: Long = 0L` property to `GroupRecord` in [server/src/main/kotlin/com/opensplit/features/group/GroupModels.kt](file:///Users/snapp/AndroidStudioProjects/opensplit/server/src/main/kotlin/com/opensplit/features/group/GroupModels.kt)
- [X] T005 [P] Add `lastInteractionAtEpochMillis: Long = 0L` to `GroupEntity` in [app/shared/src/commonMain/kotlin/com/opensplit/db/Entities.kt](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/db/Entities.kt)
- [X] T006 Bump Room database version from `3` to `4` in [app/shared/src/commonMain/kotlin/com/opensplit/db/AppDatabase.kt](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/db/AppDatabase.kt)
- [X] T007 Add `updateLastInteraction(groupId: String, timestamp: Long)` query to `GroupDao` in [app/shared/src/commonMain/kotlin/com/opensplit/db/DAOs.kt](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/db/DAOs.kt)
- [X] T008 [P] Update `toEntity()`, `toDto()`, and `toDomain()` mappers in [app/shared/src/commonMain/kotlin/com/opensplit/db/Mappers.kt](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/db/Mappers.kt) to map between `GroupDto.lastInteractionAt`, `GroupEntity.lastInteractionAtEpochMillis`, and `Group.lastInteractionAt`

**Checkpoint**: Core models and DB schemas updated across server and client.

---

## Phase 3: User Story 1 - Show Recently Active Settled Groups in Main List (Priority: P1) 🎯 MVP

**Goal**: Settled groups (net balance = 0.0) with recent activity (`lastInteractionAt >= now - 7.days`) stay in the active group section instead of hiding under the settled section.

**Independent Test**:
Run Compose UI test or component test: A settled group (`balance = 0.0`) with `lastInteractionAt = Clock.System.now() - 2.days` renders in the active groups list, while a settled group with `lastInteractionAt = Clock.System.now() - 10.days` renders under the settled section.

### Tests for User Story 1 ⚠️

- [X] T009 [P] [US1] Write UI test for active vs settled group partitioning with recent activity in [app/shared/src/commonTest/kotlin/com/opensplit/features/group/my/MyGroupsListScreenTest.kt](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonTest/kotlin/com/opensplit/features/group/my/MyGroupsListScreenTest.kt)

### Implementation for User Story 1

- [X] T010 [US1] Verify and ensure `MyGroupsListScreen.kt` partition logic (`!it.isSettled || it.lastInteractionAt >= (Clock.System.now() - 7.days)`) in [app/shared/src/commonMain/kotlin/com/opensplit/features/group/my/MyGroupListScreen.kt](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/features/group/my/MyGroupListScreen.kt) correctly handles preview and UI states
- [X] T011 [US1] Add a preview state in [app/shared/src/commonMain/kotlin/com/opensplit/features/group/my/MyGroupListScreen.kt](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/features/group/my/MyGroupListScreen.kt) showcasing a settled group appearing in active list due to recent `lastInteractionAt`

**Checkpoint**: User Story 1 UI partition behavior verified independently.

---

## Phase 4: User Story 2 - Update Interaction Timestamp on Group Events (Priority: P2)

**Goal**: Backend sets `lastInteractionAt` on group creation and updates `lastInteractionAt` when expenses are created, updated, or deleted. Backend endpoints return `lastInteractionAt` in `GroupDto`.

**Independent Test**:
Run backend routes tests:
- `POST /groups` returns `lastInteractionAt` approximately equal to `Clock.System.now()`.
- `POST /groups/{groupId}/expenses`, `PUT /groups/{groupId}/expenses/{expenseId}`, and `DELETE /groups/{groupId}/expenses/{expenseId}` update `Groups.lastInteractionAt` and return it via `GET /groups` or `GET /groups/{groupId}`.

### Tests for User Story 2 ⚠️

- [X] T012 [P] [US2] Add integration tests in [server/src/test/kotlin/com/opensplit/features/GroupRoutesTest.kt](file:///Users/snapp/AndroidStudioProjects/opensplit/server/src/test/kotlin/com/opensplit/features/GroupRoutesTest.kt) verifying `lastInteractionAt` is returned on group creation and fetch
- [X] T013 [P] [US2] Add integration tests in [server/src/test/kotlin/com/opensplit/features/ExpenseRoutesTest.kt](file:///Users/snapp/AndroidStudioProjects/opensplit/server/src/test/kotlin/com/opensplit/features/ExpenseRoutesTest.kt) verifying expense creation, update, and deletion update group `lastInteractionAt`

### Implementation for User Story 2

- [X] T014 [US2] Update `GroupRepositoryImpl.createGroup` and `loadGroups` / `loadGroupDetail` in [server/src/main/kotlin/com/opensplit/features/group/GroupRepositoryImpl.kt](file:///Users/snapp/AndroidStudioProjects/opensplit/server/src/main/kotlin/com/opensplit/features/group/GroupRepositoryImpl.kt) to populate `Groups.lastInteractionAt` with `Clock.System.now().toEpochMilliseconds()` on insert and read it back
- [X] T015 [US2] Update `GroupService.kt` in [server/src/main/kotlin/com/opensplit/features/group/GroupService.kt](file:///Users/snapp/AndroidStudioProjects/opensplit/server/src/main/kotlin/com/opensplit/features/group/GroupService.kt) to map `lastInteractionAt` from `GroupRecord` into `GroupDto` using `Instant.fromEpochMilliseconds(record.lastInteractionAt)`
- [X] T016 [US2] Update `ExpenseRepositoryImpl.createExpense` in [server/src/main/kotlin/com/opensplit/features/expense/ExpenseRepositoryImpl.kt](file:///Users/snapp/AndroidStudioProjects/opensplit/server/src/main/kotlin/com/opensplit/features/expense/ExpenseRepositoryImpl.kt) to update `Groups.lastInteractionAt` with expense `createdAt.toEpochMilliseconds()` and emit sync change for `"HOUSEHOLD"`
- [X] T017 [US2] Update `ExpenseRepositoryImpl.updateExpense` and `deleteExpense` in [server/src/main/kotlin/com/opensplit/features/expense/ExpenseRepositoryImpl.kt](file:///Users/snapp/AndroidStudioProjects/opensplit/server/src/main/kotlin/com/opensplit/features/expense/ExpenseRepositoryImpl.kt) to update `Groups.lastInteractionAt` with `Clock.System.now().toEpochMilliseconds()` and emit sync change for `"HOUSEHOLD"`

**Checkpoint**: Backend guarantees accurate `lastInteractionAt` tracking across all group and expense mutations.

---

## Phase 5: User Story 3 - Offline Caching of Group Interaction Timestamp (Priority: P3)

**Goal**: Local client Room database persists and syncs `lastInteractionAt`, and updates local interaction timestamp optimistically when expenses are created, edited, or deleted offline.

**Independent Test**:
Run client component/flow test: Create an expense locally in a settled group while offline, verify local `GroupEntity` has updated `lastInteractionAtEpochMillis` and the group remains in active section.

### Tests for User Story 3 ⚠️

- [X] T018 [P] [US3] Add test in [app/shared/src/commonTest/kotlin/com/opensplit/GroupFlowComponentTest.kt](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonTest/kotlin/com/opensplit/GroupFlowComponentTest.kt) verifying offline persistence of `lastInteractionAt` and group partitioning behavior

### Implementation for User Story 3

- [X] T019 [US3] Update `GroupRepository.saveGroupWithPendingAdjustment` in [app/shared/src/commonMain/kotlin/com/opensplit/repository/GroupRepository.kt](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/repository/GroupRepository.kt) to persist `lastInteractionAtEpochMillis` from `GroupDto` to `GroupEntity`
- [X] T020 [US3] Update `ExpenseRepository.createExpense`, `updateExpense`, and `deleteExpense` in [app/shared/src/commonMain/kotlin/com/opensplit/repository/ExpenseRepository.kt](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonMain/kotlin/com/opensplit/repository/ExpenseRepository.kt) to update `groupDao.updateLastInteraction(groupId, Clock.System.now().toEpochMilliseconds())` optimistically

**Checkpoint**: Offline-first caching and optimistic UI updates for `lastInteractionAt` fully functional.

---

## Phase 6: Polish & Cross-Cutting Concerns

**Purpose**: Format checking, end-to-end regression validation, and quality gates.

- [X] T021 Update `FakeGroupApi` in [app/shared/src/commonTest/kotlin/com/opensplit/fake/FakeGroupApi.kt](file:///Users/snapp/AndroidStudioProjects/opensplit/app/shared/src/commonTest/kotlin/com/opensplit/fake/FakeGroupApi.kt) if needed to supply `lastInteractionAt`
- [X] T022 [P] Execute server and client test suites: `./gradlew :server:test :app:shared:jvmTest`
- [X] T023 Run full verification gate: `./gradlew jvmTest test ktfmtFormat --offline`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Independent, can start immediately.
- **Foundational (Phase 2)**: Depends on Phase 1 models. Blocks all user stories.
- **User Story 1 (Phase 3)**: Depends on Phase 1 & 2. Focuses on client UI partitioning logic.
- **User Story 2 (Phase 4)**: Depends on Phase 1 & 2. Implements backend tracking & event updates.
- **User Story 3 (Phase 5)**: Depends on Phase 1 & 2. Completes client local DB caching and optimistic updates.
- **Polish (Phase 6)**: Depends on all user stories being completed.

### User Story Dependencies

- **US1 (P1)**: Independent of US2/US3 backend implementation; can be verified with domain/UI mocks.
- **US2 (P2)**: Independent backend implementation of timestamps and event triggers.
- **US3 (P3)**: Depends on DB foundational tasks; integrates client offline store with US2 contract.

### Parallel Opportunities

- **Phase 1**: T001 and T002 can run in parallel.
- **Phase 2**: T004, T005, and T008 can run in parallel after T003/T005 schema definitions.
- **Phase 3 (US1)**: Test T009 can be written concurrently with preview task T011.
- **Phase 4 (US2)**: Tests T012 and T013 can run in parallel before server implementation.
- **Phase 5 (US3)**: Test T018 can run in parallel with repository updates.

---

## Parallel Example: User Story 2

```bash
# Launch test creation in parallel:
Task: T012 [P] [US2] Add integration tests in server/src/test/kotlin/com/opensplit/features/GroupRoutesTest.kt
Task: T013 [P] [US2] Add integration tests in server/src/test/kotlin/com/opensplit/features/ExpenseRoutesTest.kt
```

---

## Implementation Strategy

### MVP First (User Story 1 Only)

1. Complete Phase 1: Setup (T001, T002)
2. Complete Phase 2: Foundational (T003 - T008)
3. Complete Phase 3: User Story 1 (T009 - T011)
4. Validate that settled groups with recent interactions appear in the active section.

### Incremental Delivery

1. Phase 1 & 2: Foundation established across core, backend, and Room DB.
2. Phase 3: Deliver User Story 1 (UI displays recent settled groups).
3. Phase 4: Deliver User Story 2 (Backend persists and returns accurate timestamps across expense events).
4. Phase 5: Deliver User Story 3 (Offline caching and optimistic local updates).
5. Phase 6: Run full verification gate (`./gradlew jvmTest test ktfmtFormat --offline`).
