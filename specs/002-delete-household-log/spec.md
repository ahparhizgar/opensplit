# Feature Specification: Remove HOUSEHOLD Sync Logging and Adopt Type-Safe Enums

**Feature Branch**: `specs/002-delete-household-log`

**Created**: 2026-09-24

**Status**: Draft

**Input**: User description: "delete-household-log delete recordChange(\"HOUSEHOLD\") and use enum instead of string for record change and use enums in Exposed tables if it supports it."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Type-Safe Server Change Recording and Household Log Removal (Priority: P1)

As a backend system and synchronization service,
I want entity modifications (such as Expense and Membership events) to be recorded using strongly-typed enums rather than loose strings, and redundant "HOUSEHOLD" entity change records removed from sync logging,
So that synchronization change recording is type-safe, database schema is consistent, and unnecessary change logs are not propagated to clients.

**Why this priority**: Core architectural consistency and bug prevention. Removing redundant household sync logging avoids unnecessary sync notifications, and strongly-typed enums prevent typos or invalid states in sync tracking and database tables.

**Independent Test**: Can be tested by running group/expense mutation integration tests (e.g. creating, updating, or deleting expenses and memberships) and verifying that change logs contain valid enum entries (e.g. EXPENSE, MEMBERSHIP; INSERT, UPDATE, DELETE) and no HOUSEHOLD change log rows are created.

**Acceptance Scenarios**:

1. **Given** a group with expenses, **When** a user creates, updates, or deletes an expense, **Then** an EXPENSE change record with the corresponding operation enum is logged, and NO HOUSEHOLD change log record is created.
2. **Given** a group lifecycle event (e.g., creating a group or managing memberships), **When** memberships are created or deleted, **Then** MEMBERSHIP change records are logged with strongly-typed enums, and NO HOUSEHOLD change log record is created.
3. **Given** a change log table in the database, **When** rows are inserted or queried, **Then** entity types and operations are stored and represented as strongly-typed enums.

---

### User Story 2 - Accurate Incremental Client Synchronization (Priority: P2)

As an OpenSplit client application syncing with the server,
I want to fetch changed and deleted entities based on the updated sync change log,
So that my local database accurately reflects expense and group membership changes without failing on or relying on obsolete household sync notifications.

**Why this priority**: Sync clients rely on the server change log to compute deltas; removing HOUSEHOLD sync logging must maintain complete data integrity for clients fetching updates.

**Independent Test**: Can be fully tested using sync route tests (`GET /sync?sinceVersion=...`) verifying that expenses and versions continue to sync cleanly across multi-user sessions without errors.

**Acceptance Scenarios**:

1. **Given** multiple clients syncing expenses across versions, **When** a client queries `/sync?sinceVersion=N`, **Then** the server returns the latest version and the exact changed and deleted expenses.
2. **Given** group metadata changes (e.g., group creation or expense updates modifying `lastInteractionAt`), **When** expenses are synced, **Then** all expense updates and version tracking function as expected without requiring HOUSEHOLD change records.

---

### Edge Cases

- **Handling existing historical change records**: If existing database rows contain string values (e.g., `"EXPENSE"`, `"MEMBERSHIP"`, `"HOUSEHOLD"`), reading or filtering on the change log must either ignore obsolete `"HOUSEHOLD"` records or safely map known entity types without throwing deserialization/mapping exceptions.
- **Group metadata updates**: Verifying that `Groups.lastInteractionAt` continues to be updated in the database when expenses are created, edited, or deleted, even though a `ChangeLog` entry for `HOUSEHOLD` is no longer emitted.
- **Invalid enum values**: Any unexpected string in the database or API layer must fail gracefully with appropriate validation errors rather than undefined behavior.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST remove all calls to record changes for the `"HOUSEHOLD"` entity type across all repository layers (including ExpenseRepository and GroupRepository).
- **FR-002**: System MUST define strongly-typed enums for sync entity types (e.g., `SyncEntityType` including `EXPENSE`, `MEMBERSHIP`) and sync operation types (e.g., `SyncOperation` including `INSERT`, `UPDATE`, `DELETE`).
- **FR-003**: The change recording interface and implementation (`SyncRepository.recordChange`) MUST accept strongly-typed enums for entity type and operation instead of generic strings.
- **FR-004**: Database table definitions (specifically `ChangeLog`) MUST use Exposed's enum column mapping (`enumeration` or `enumerationByName`) for entity type and operation columns if supported by Exposed.
- **FR-005**: Group last interaction timestamp (`Groups.lastInteractionAt`) updates MUST continue to execute when expenses are created, updated, or deleted, ensuring group ordering remains correct without logging a HOUSEHOLD sync change.
- **FR-006**: Existing synchronization endpoint (`GET /sync`) MUST continue to correctly retrieve and filter changed and deleted entities using the strongly-typed enum representations.

### Key Entities *(include if feature involves data)*

- **SyncEntityType**: Enum representing the types of entities tracked in sync history (e.g., `EXPENSE`, `MEMBERSHIP`). Note that `HOUSEHOLD` is removed from actively logged entity types.
- **SyncOperation**: Enum representing mutation operations recorded in sync history (e.g., `INSERT`, `UPDATE`, `DELETE`).
- **ChangeLog**: Persistent table recording incremental entity mutations with version/id, entity type enum, entity ID, operation enum, and timestamp.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of sync change log insertions and queries use strongly-typed enums with zero raw string literals for entity types and operations in the sync domain.
- **SC-002**: 0 records with entity type `HOUSEHOLD` are generated in the sync change log during expense creation, updating, deletion, or group creation.
- **SC-003**: All multi-user sync integration tests pass without regressions, correctly reflecting expense creations, modifications, and deletions.
- **SC-004**: Verification gate (`./gradlew jvmTest test ktfmtFormat --offline`) passes with zero compiler warnings or test failures.

## Assumptions

- Obsolete `"HOUSEHOLD"` sync change records are not consumed by the mobile/desktop clients because the client sync response (`SyncResponse`) only tracks `expenses` (and in the future memberships/groups directly), so removing `HOUSEHOLD` from `recordChange` does not break client sync parsing.
- Exposed's `enumerationByName` is preferred for readable and backward-compatible database column storage in both H2 and PostgreSQL.
- Legacy `"HOUSEHOLD"` rows in existing databases (if any) can be safely ignored by sync queries.
