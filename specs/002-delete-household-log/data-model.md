# Data Model: Sync Entity & Operation Enums

## Enums & Types

### 1. SyncEntityType

Enumeration of entities tracked in synchronization change log.

```kotlin
package com.opensplit.dto.sync

@Serializable
enum class SyncEntityType {
  EXPENSE,
  MEMBERSHIP,
}
```

- Values:
  - `EXPENSE`: An expense record modification.
  - `MEMBERSHIP`: A user's membership within a group.
- Removed:
  - `HOUSEHOLD`: Deprecated and removed; changes to groups are tracked directly via `Groups.lastInteractionAt` rather than incremental sync log entries.

---

### 2. SyncOperation

Enumeration of mutation operations performed on tracked entities.

```kotlin
package com.opensplit.dto.sync

@Serializable
enum class SyncOperation {
  INSERT,
  UPDATE,
  DELETE,
}
```

- Values:
  - `INSERT`: Entity was newly created.
  - `UPDATE`: Entity was modified.
  - `DELETE`: Entity was removed.

---

### 3. ChangeLog Table (Exposed)

Database representation in `com.opensplit.database.Tables`:

```kotlin
object ChangeLog : Table("change_log") {
  val id = long("id").autoIncrement()
  val entityType = enumerationByName("entity_type", 50, SyncEntityType::class)
  val entityId = varchar("entity_id", 36)
  val operation = enumerationByName("operation", 20, SyncOperation::class)
  val timestamp = long("timestamp")

  override val primaryKey = PrimaryKey(id)
}
```

- Invariant: `entityType` must strictly match a valid `SyncEntityType`.
- Invariant: `operation` must strictly match a valid `SyncOperation`.

---

## Entity Relationships and Flow

```
[ExpenseRepository / GroupRepository]
        |
        | recordChange(SyncEntityType, entityId, SyncOperation)
        v
[SyncRepositoryImpl]
        |
        +---> [ChangeLog.insert] (Stores entityType & operation as strings matching enum names)
        |
        +---> Updates corresponding table version:
                - SyncEntityType.EXPENSE    -> Expenses.update { it[version] = logId }
                - SyncEntityType.MEMBERSHIP -> Memberships.update { it[version] = logId }
```
