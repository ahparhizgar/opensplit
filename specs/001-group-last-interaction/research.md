# Phase 0 Research: Group Last Interaction At

## Decision 1: Timestamp Representation & Column Storage
- **Decision**: Store `last_interaction_at` as milliseconds since epoch (`Long` in Exposed / SQLite) on backend `Groups` table and Room `GroupEntity`, exposed across DTO and domain models as `kotlin.time.Instant`.
- **Rationale**: 
  - Consistent with `Expenses.createdAt` which uses `long("created_at")` and `Clock.System.now().toEpochMilliseconds()`.
  - Serializes cleanly in ISO-8601 or epoch representation via kotlinx.datetime/kotlin.time `Instant`.
  - Follows Constitution rule: "Time come only from `Clock.System.now()`" and "If need millis use `Clock.System.now().toEpochMilliseconds()`".
- **Alternatives considered**:
  - `varchar` ISO-8601 string column in SQL: Inefficient for querying, sorting, and indexing compared to 64-bit integer timestamp.
  - Compute dynamically on fetch (`MAX(expenses.created_at)`): Doesn't account for group creation when zero expenses exist, doesn't easily capture expense deletion events, and requires expensive aggregation across all expenses every time groups are listed.

## Decision 2: Backend Table Schema & Migration
- **Decision**: Add `val lastInteractionAt = long("last_interaction_at").default(0L)` (or initial default) to `Groups` table in `server/.../Tables.kt`. When creating a group, set `lastInteractionAt = Clock.System.now().toEpochMilliseconds()`.
- **Rationale**:
  - Setting default ensures backwards compatibility for existing group rows.
  - Zero/0L or creation timestamp falls back cleanly.
- **Alternatives considered**:
  - Nullable column: Requires null checks and handling `null` on clients. Non-null with fallback to creation time or 0L is safer and cleaner.

## Decision 3: Triggering Interaction Updates on Expense Modifications
- **Decision**: 
  - In `ExpenseRepositoryImpl`:
    - `createExpense`: Update `Groups.update({ Groups.id eq expense.groupId }) { it[lastInteractionAt] = expense.createdAt.toEpochMilliseconds() }` and `syncRepository.recordChange("HOUSEHOLD", expense.groupId, "UPDATE")`.
    - `updateExpense`: Update `Groups.update({ Groups.id eq groupId }) { it[lastInteractionAt] = Clock.System.now().toEpochMilliseconds() }` and `syncRepository.recordChange("HOUSEHOLD", groupId, "UPDATE")`.
    - `deleteExpense`: Update `Groups.update({ Groups.id eq groupId }) { it[lastInteractionAt] = Clock.System.now().toEpochMilliseconds() }` and `syncRepository.recordChange("HOUSEHOLD", groupId, "UPDATE")`.
- **Rationale**:
  - Guarantees `Groups` table in backend always has up-to-date timestamp whenever group activity happens.
  - Recording change on `HOUSEHOLD` notifies clients during sync to refresh group metadata.
- **Alternatives considered**:
  - Update group timestamp only on client: Flawed because multi-user group activity from other members wouldn't reflect on backend until sync, and would fail if someone else adds an expense. Backend must be source of truth for group metadata.

## Decision 4: Client Offline Caching & Local Interaction Update
- **Decision**:
  - In `app/shared`:
    - Update `GroupEntity` with `val lastInteractionAtEpochMillis: Long`.
    - Room DB migration / bump version from 3 to 4 (or recreate for test db / migration).
    - In `GroupDao`: add query `UPDATE groups SET lastInteractionAtEpochMillis = :timestamp WHERE id = :groupId`.
    - In `ExpenseRepository` (client): when user locally creates, updates, or deletes an expense, update the local group's `lastInteractionAtEpochMillis` so that optimistic UI instantly keeps the group in active section even before sync completes.
    - In `Mappers.kt`: Map between `GroupDto.lastInteractionAt`, `GroupEntity.lastInteractionAtEpochMillis`, and `Group.lastInteractionAt`.
- **Rationale**:
  - Satisfies FR-005, FR-006, FR-007, and offline-first requirement (SC-004).
  - Client immediately reflects activity even while offline or waiting for sync.
- **Alternatives considered**:
  - Client only updates timestamp from server sync: Fails offline scenario where user creates expense in a settled group while offline—the group would not immediately remain active if timestamp wasn't updated locally.

## Decision 5: DTO and Domain Contracts
- **Decision**:
  - Update `GroupDto` in `core/.../GroupDto.kt`: add `val lastInteractionAt: Instant = Instant.DISTANT_PAST`.
  - `Group` domain model in `core/.../Group.kt` already has `val lastInteractionAt: Instant = Instant.DISTANT_PAST`.
  - Update `FakeGroupDtoFactory` and `FakeGroupFactory` to provide default and parameter for `lastInteractionAt`.
- **Rationale**:
  - Follows Constitution rule: "Fake factories live in same file right after real class... Fake factory MUST have `create()` with smart defaults."
  - Non-breaking default value for existing tests and call sites.
