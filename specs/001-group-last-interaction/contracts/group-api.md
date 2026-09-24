# API Contract: Group Endpoints

## DTO Schema Change

### `GroupDto`
Shared DTO in `com.opensplit.dto.group.GroupDto`:

```kotlin
@Serializable
data class GroupDto(
    val id: String,
    val name: String,
    val members: List<GroupMemberDto>,
    val isOwner: Boolean = false,
    val inviteLink: String,
    val lastInteractionAt: Instant = Instant.DISTANT_PAST,
)
```

JSON representation:
```json
{
  "id": "e305e46c-2f63-460d-83b6-2001ad28ea4e",
  "name": "Trip to Spain",
  "members": [
    {
      "userId": "user-1",
      "name": "Alice",
      "email": "alice@example.com",
      "isOwner": true,
      "isCurrentUser": true,
      "balance": 0.0
    }
  ],
  "isOwner": true,
  "inviteLink": "https://opensplit.com/join/abc123456789",
  "lastInteractionAt": "2026-09-24T12:00:00Z"
}
```

## Endpoints

### 1. `GET /groups`
Returns all groups for the authenticated user, now populated with `lastInteractionAt`.

- **Headers**: `Authorization: Bearer <token>`
- **Response**: `200 OK`
- **Body**: `List<GroupDto>`

### 2. `POST /groups`
Creates a new group, setting `lastInteractionAt` to current creation time.

- **Headers**: `Authorization: Bearer <token>`
- **Request Body**:
  ```json
  {
    "name": "Ski Cabin"
  }
  ```
- **Response**: `201 Created`
- **Body**: `GroupDto`

### 3. `GET /groups/{groupId}`
Returns detailed group information including `lastInteractionAt`.

- **Headers**: `Authorization: Bearer <token>`
- **Response**: `200 OK`
- **Body**: `GroupDto`

### 4. `POST /groups/{groupId}/expenses`
Creating an expense causes the group's `lastInteractionAt` on the backend to be updated to the expense timestamp.

- **Headers**: `Authorization: Bearer <token>`
- **Response**: `201 Created`
- **Body**: `ExpenseDto`
- **Side Effect**: `Groups.lastInteractionAt` updated, change recorded for `HOUSEHOLD` sync.
