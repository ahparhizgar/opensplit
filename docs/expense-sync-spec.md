# Expense Sync Technical Specification

## Overview

Bidirectional offline-first sync engine. Outbox pattern for upstream (Client -> Server). Delta sync via versioned changelog for downstream (Server -> Client).

```
[Client Local DB] --(Outbox Queue)--> [Ktor Server] --(Exposed DB)--> [ChangeLog]
       ^                                                                   |
       +------------------- GET /sync?sinceVersion=N ---------------------+
```

---

## Database Schemas

### Server DB (Exposed / PostgreSQL)

* **`change_log`**
  * `id`: Long (PK, auto-increment) -> global sync sequence.
  * `entity_type`: `SyncEntityType` (VARCHAR(50)) -> `EXPENSE`, `MEMBERSHIP`.
  * `entity_id`: VARCHAR(36) -> target entity UUID.
  * `operation`: `SyncOperation` (VARCHAR(20)) -> `INSERT`, `UPDATE`, `DELETE`.
  * `timestamp`: Long -> epoch millis.
* **`expenses`**
  * `id`: VARCHAR(36) (PK)
  * `group_id`: VARCHAR(36) (FK -> groups.id)
  * `title`: VARCHAR(255)
  * `amount`: Double
  * `creator`: VARCHAR(36) (FK -> users.id)
  * `created_at`: Long
  * `split_method`: TEXT (JSON serialized)
  * `version`: Long -> FK to `change_log.id`.
* **`expense_participants`**
  * `id`: VARCHAR(36) (PK)
  * `expense_id`: VARCHAR(36) (FK -> expenses.id)
  * `user_id`: VARCHAR(36) (FK -> users.id)
  * `paid_amount`: Double
  * `owed_amount`: Double
  * `version`: Long
* **`groups`** / **`memberships`**
  * Contain `version`: Long, tracked via `change_log`.

### Client Local DB (Room)

* **`expenses`**
  * `id`: String (PK)
  * `groupId`: String
  * `title`: String
  * `amount`: Double
  * `creator`: String
  * `createdAtEpochMillis`: Long
  * `splitMethodJson`: String
  * `syncStatus`: `SyncStatus` (`SYNCED` | `PENDING`)
* **`participants`**
  * `expenseId`: String, `userId`: String (Composite PK)
  * `paidShare`: Double, `consumedShare`: Double
* **`sync_queue`** (Outbox)
  * `id`: Long (PK, auto-gen)
  * `operation`: `OperationType` (`CREATE` | `UPDATE` | `DELETE`)
  * `entityType`: String (`"EXPENSE"`)
  * `entityId`: String (UUID)
  * `metadata`: String? (e.g. `groupId` for deletes)
  * `createdAt`: Long
* **`sync_metadata`**
  * `key`: String (PK) -> e.g. `"last_sync_version"`
  * `value`: String -> version number string

---

## DTOs

Location: `core/src/commonMain/kotlin/com/opensplit/dto/`

```kotlin
@Serializable
data class SyncResponse(
    val latestVersion: Long,
    val changedEntities: ChangedEntitiesDto,
    val deletedEntities: DeletedEntitiesDto,
)

@Serializable
data class ChangedEntitiesDto(
    val expenses: List<ExpenseDto> = emptyList(),
)

@Serializable
data class DeletedEntitiesDto(
    val expenses: List<String> = emptyList(), // Entity IDs
)

@Serializable
data class ExpenseDto(
    val id: String,
    val groupId: String,
    val title: String,
    val amount: Double,
    val creator: String,
    val createdAt: Instant,
    val shares: List<ParticipantShareDto> = emptyList(),
    val splitMethod: SplitMethod,
    val syncStatus: SyncStatus = SyncStatus.SYNCED,
)

@Serializable
data class ParticipantShareDto(
    val userId: String,
    val paidShare: Double,
    val consumedShare: Double,
)
```

---

## Upstream Sync Flow (Client -> Server)

Trigger: `SyncDaemon` interval (5s) or `SyncManager.triggerSync()`. Wrapped in Mutex -> single sync execution.

```
[Local Op] -> Insert to sync_queue -> processOutbox() -> API call -> Reconcile & Dequeue
```

1. **Read Outbox**: Query `sync_queue` FIFO.
2. **Process `EXPENSE` Ops**:
   * **`CREATE`**:
     * Fetch local `ExpenseEntity` + `ParticipantEntity`.
     * Call `POST /groups/{id}/expenses`.
     * Server creates expense -> calls `syncRepository.recordChange("EXPENSE", id, "INSERT")` -> returns server `ExpenseDto`.
     * In Room transaction:
       * Reconcile balances (`updateBalances`).
       * Replace temporary local record with server entity (`syncStatus = SYNCED`).
       * Dequeue item from `sync_queue`.
   * **`DELETE`**:
     * Extract `groupId` from `entry.metadata`.
     * Call `DELETE /groups/{groupId}/expenses/{id}`.
     * Server deletes expense -> calls `recordChange("EXPENSE", id, "DELETE")`.
     * Dequeue item from `sync_queue`.
   * **`UPDATE`**: Dequeues (future feature).

---

## Downstream Sync Flow (Server -> Client)

Trigger: Immediately after `processOutbox()`.

```
Fetch last_sync_version -> GET /sync?sinceVersion=N -> Server filters ChangeLog -> Apply to Room
```

1. **Client Request**:
   * Read `last_sync_version` from `sync_metadata` (default `0L`).
   * `GET /sync?sinceVersion={last_sync_version}` with bearer auth.
2. **Server Processing** (`SyncRepositoryImpl.getChanges`):
   * `latestVersion` = max `id` in `change_log`.
   * `userGroupIds` = memberships for requesting user.
   * `changedExpenses` = `Expenses` where `version > sinceVersion` AND `groupId IN (userGroupIds)`.
   * `deletedExpenses` = `ChangeLog` entry IDs where `id > sinceVersion` AND `entityType == "EXPENSE"` AND `operation == "DELETE"`.
   * Responds `SyncResponse(latestVersion, changedEntities, deletedEntities)`.
3. **Client Apply** (`applyChanges`):
   * Room transaction:
     * **Upsert Changed**: For each `ExpenseDto`, update member balances delta (`paidShare - consumedShare`), write `ExpenseEntity` (`SYNCED`) + `ParticipantEntity`.
     * **Delete Entities**: For each deleted ID, update member balances delta, delete from `expenses` and `participants`.
     * **Update Version**: Store `latestVersion` in `sync_metadata` under `"last_sync_version"`.

---

## Balance Reconciliation Logic

Member balance delta calculation on expense add/update/delete:

```kotlin
// net impact per member = paidShare - consumedShare
memberDelta = (newPaid - newConsumed) - (oldPaid - oldConsumed)
```

Executed via `updateBalances()` -> applies SQL delta directly to `group_members.balance`.
