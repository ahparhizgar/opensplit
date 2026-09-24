# Sync Contracts & Internal Interfaces

## 1. Repository Interface Contract

### `SyncRepository`

Location: `server/src/main/kotlin/com/opensplit/features/sync/SyncRepository.kt`

```kotlin
package com.opensplit.features.sync

import com.opensplit.dto.sync.SyncEntityType
import com.opensplit.dto.sync.SyncOperation
import com.opensplit.dto.sync.SyncResponse

interface SyncRepository {
  /**
   * Records a mutation for an entity into the synchronization change log.
   *
   * @param entityType The strongly-typed type of entity (e.g. EXPENSE, MEMBERSHIP).
   * @param entityId The unique identifier of the entity.
   * @param operation The mutation operation performed (INSERT, UPDATE, DELETE).
   * @return The change log ID representing the new global sync version for this change.
   */
  fun recordChange(
      entityType: SyncEntityType,
      entityId: String,
      operation: SyncOperation,
  ): Long

  /**
   * Retrieves incremental changes across entities accessible to the specified user since [sinceVersion].
   */
  fun getChanges(sinceVersion: Long, userId: String): SyncResponse
}
```

---

## 2. HTTP Endpoint Contract

### `GET /sync`

Unchanged endpoint contract for client consumption:

- **Query Parameters**:
  - `sinceVersion` (Long, default: 0): The client's last observed version.
- **Headers**:
  - `Authorization: Bearer <jwt_token>`
- **Response** (`200 OK`):
  ```json
  {
    "latestVersion": 12,
    "changedEntities": {
      "expenses": [ ... ]
    },
    "deletedEntities": {
      "expenses": [ ... ]
    }
  }
  ```
- **Guarantees**:
  - Deletions are queried from `ChangeLog` where `entityType == SyncEntityType.EXPENSE` and `operation == SyncOperation.DELETE`.
  - No `HOUSEHOLD` entries are logged or queried.
