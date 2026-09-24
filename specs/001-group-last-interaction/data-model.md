# Data Model: Group Last Interaction At

## Entities

### 1. Group (Domain - `core/src/commonMain/kotlin/com/opensplit/domain/Group.kt`)
Represents an expense group in the application domain.

| Field | Type | Description |
|---|---|---|
| `id` | `String` | Unique UUID identifier for the group |
| `name` | `String` | Name of the group |
| `members` | `List<Member>` | List of members belonging to this group |
| `isOwner` | `Boolean` | Whether current user is owner |
| `inviteLink` | `String` | Group invite link |
| `balance` | `Double` | Current user's net balance in the group |
| `lastInteractionAt` | `kotlin.time.Instant` | Timestamp of most recent group activity/event |
| `isSettled` | `Boolean` | Computed property: `balance == 0.0` |

### 2. GroupDto (DTO - `core/src/commonMain/kotlin/com/opensplit/dto/group/GroupDto.kt`)
Network representation exchanged between server and client.

| Field | Type | Description |
|---|---|---|
| `id` | `String` | Unique UUID identifier |
| `name` | `String` | Name of the group |
| `members` | `List<GroupMemberDto>` | Member details and balances |
| `isOwner` | `Boolean` | Whether authenticated user owns group |
| `inviteLink` | `String` | URL for joining group |
| `lastInteractionAt` | `kotlin.time.Instant` | Epoch timestamp serialized via KotlinX Serialization (defaults to `Instant.DISTANT_PAST`) |

### 3. GroupRecord & Groups Table (Backend - `server/.../Tables.kt`, `server/.../GroupModels.kt`)
Exposed database entity and repository record.

```kotlin
object Groups : Table("groups") {
  val id = varchar("id", 36)
  val name = varchar("name", 255)
  val ownerId = varchar("owner_id", 36).references(Users.id)
  val inviteCode = varchar("invite_code", 64).nullable()
  val lastInteractionAt = long("last_interaction_at").default(0L)
  val version = long("version").default(0)

  override val primaryKey = PrimaryKey(id)
}

data class GroupRecord(
    val id: String,
    val name: String,
    val ownerId: String,
    val inviteCode: String?,
    val lastInteractionAt: Long = 0L,
)
```

### 4. GroupEntity (Client SQLite / Room - `app/shared/.../Entities.kt`)
Local offline persistence model.

```kotlin
@Entity(tableName = "groups")
data class GroupEntity(
    @PrimaryKey val id: String,
    val name: String,
    val inviteLink: String,
    val isOwner: Boolean,
    val lastInteractionAtEpochMillis: Long = 0L,
)
```

## State Transitions & Event Triggers

1. **Group Created**:
   - `lastInteractionAt` = creation timestamp (`Clock.System.now()`).
2. **Expense Created**:
   - `lastInteractionAt` updated to expense creation time on server and local DB.
3. **Expense Updated**:
   - `lastInteractionAt` updated to current timestamp (`Clock.System.now()`).
4. **Expense Deleted**:
   - `lastInteractionAt` updated to current timestamp (`Clock.System.now()`).
5. **Client Group Partitioning Logic** (`MyGroupListScreen.kt`):
   - `activeGroups`: `!it.isSettled || it.lastInteractionAt >= (Clock.System.now() - 7.days)`
   - `settledGroups`: `it.isSettled && it.lastInteractionAt < (Clock.System.now() - 7.days)`
