# Phase 0 Research: Type-Safe Enums and HOUSEHOLD Sync Log Removal

## Technical Decisions & Findings

### Decision 1: Sync Entity Types and Operation Types Modeling

- **Decision**: Define domain enums `SyncEntityType` and `SyncOperation` in `core` (e.g. `com.opensplit.model.sync` or `com.opensplit.dto.sync`) and accompanied with fake factory methods.
  - `enum class SyncEntityType { EXPENSE, MEMBERSHIP }`
  - `enum class SyncOperation { INSERT, UPDATE, DELETE }`
- **Rationale**:
  - Eliminates typo-prone string literals across the sync engine and repositories (`"EXPENSE"`, `"MEMBERSHIP"`, `"HOUSEHOLD"`, `"INSERT"`, `"UPDATE"`, `"DELETE"`).
  - Complies with OpenSplit Constitution ("Prefer enums over strings. Use enums in DTOs and DAOs and domain layer where possible").
  - Placing in `core` allows both server repository/sync services and future client components to share the standard sync definitions without duplication.
- **Alternatives Considered**:
  - Keeping strings in repository signatures and only checking at runtime: Rejected because it does not provide compile-time safety and violates project rules.
  - Keeping `HOUSEHOLD` in `SyncEntityType`: Rejected because the explicit requirement is to delete `recordChange("HOUSEHOLD")` and remove household change tracking from the sync log.

---

### Decision 2: JetBrains Exposed Table Column Enum Support

- **Decision**: Use `enumerationByName<T>(name, length, klass)` in Exposed `Table("change_log")`.
  Specifically:
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
- **Rationale**:
  - Exposed natively supports `enumerationByName` (and `enumeration<T>` for ordinal).
  - Storing as names (strings like `"EXPENSE"`, `"INSERT"`) rather than ordinals (0, 1, ...) in the DB column ensures human-readable queries, maintains backward compatibility with existing databases/tests, and avoids breakage if enum constant ordering changes.
  - Exposed 1.5.0 seamlessly maps `ResultRow[ChangeLog.entityType]` to `SyncEntityType` and handles inserts/updates directly using the enum values.
- **Alternatives Considered**:
  - `enumeration<T>` (ordinal): Saves a few bytes in storage, but makes SQL queries cryptic, brittle to enum declaration reordering, and breaks compatibility with existing tables having string values.
  - Native Postgres `customEnumeration`: Creates DB-specific enum types which fail in H2 in-memory test databases unless custom dialect scripts are run. `enumerationByName` works consistently across PostgreSQL and H2.

---

### Decision 3: Removal of HOUSEHOLD Sync Logging

- **Decision**: Remove all calls to `syncRepository.recordChange("HOUSEHOLD", ...)`:
  - In `ExpenseRepositoryImpl.kt`:
    - In `createExpense`: remove `syncRepository.recordChange("HOUSEHOLD", expense.groupId, "UPDATE")`
    - In `updateExpense`: remove `syncRepository.recordChange("HOUSEHOLD", groupId, "UPDATE")`
    - In `deleteExpense`: remove `syncRepository.recordChange("HOUSEHOLD", groupId, "UPDATE")`
  - In `GroupRepositoryImpl.kt`:
    - In `createGroup`: remove `syncRepository.recordChange("HOUSEHOLD", targetGroupId, "INSERT")`
  - In `SyncRepositoryImpl.kt`:
    - Remove `"HOUSEHOLD"` branch in `recordChange`.
    - `Groups.lastInteractionAt` updates remain in `ExpenseRepositoryImpl` and `GroupRepositoryImpl` to guarantee accurate group activity tracking and recent group ordering in the UI.
- **Rationale**:
  - Group details and metadata are not synchronized via incremental `ChangeLog` polling (`/sync` only returns changed/deleted expenses).
  - Logging `HOUSEHOLD` changes wrote unused rows to the DB and could cause unhandled enum mapping errors once `ChangeLog.entityType` is mapped to `SyncEntityType`.
- **Alternatives Considered**:
  - Keeping a no-op handler for `HOUSEHOLD`: Unnecessary dead code since nothing queries or syncs household change records.

---

### Decision 4: Updating `SyncRepository` Signature

- **Decision**:
  ```kotlin
  interface SyncRepository {
    fun recordChange(entityType: SyncEntityType, entityId: String, operation: SyncOperation): Long
    fun getChanges(sinceVersion: Long, userId: String): SyncResponse
  }
  ```
- **Rationale**:
  - Strongly types all callers (`ExpenseRepositoryImpl`, `GroupRepositoryImpl`, `SyncRepositoryImpl`).
  - Compiler immediately highlights any remaining raw string invocations.
