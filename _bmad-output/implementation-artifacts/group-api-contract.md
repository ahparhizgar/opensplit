# Group API Contract

## Shared Error Contract

All API error payloads use the shared serializable `ErrorResponse` from:

- `core/src/commonMain/kotlin/com/opensplit/dto/auth/AuthDtos.kt`

Shape:

```json
{
  "generalError": "Request failed",
  "errors": {
    "fieldName": "Field-specific message"
  }
}
```

## POST `/groups`

Creates a group for the authenticated user and makes that user a member.

### Request

```json
{
  "name": "Maple House"
}
```

### Success Response (`201 Created`)

```json
{
  "id": "8d0b8eb8-1e3f-4ba2-bf3c-686f79d17a11",
  "name": "Maple House",
  "inviteCode": "a1b2c3d4e5f6"
}
```

### Error Responses

- `400 Bad Request`

```json
{
  "generalError": "Invalid group name",
  "errors": {
    "name": "Name must not be empty"
  }
}
```

- `401 Unauthorized`

```json
{
  "generalError": "Authentication required",
  "errors": {
    "token": "Authentication required"
  }
}
```

## POST `/groups/join`

Joins an existing group using an invite code, or accesses by group id if requester is already an owner/member.

REST note: for membership listing, a REST-friendly read endpoint would be `GET /users/{userId}/groups` (or `GET /users/me/groups` for current user).

### Request

```json
{
  "inviteCodeOrId": "a1b2c3d4e5f6"
}
```

### Success Response (`200 OK`)

```json
{
  "groupId": "8d0b8eb8-1e3f-4ba2-bf3c-686f79d17a11",
  "joined": true
}
```

### Error Responses

- `400 Bad Request`

```json
{
  "generalError": "Invite code is required",
  "errors": {
    "inviteCodeOrId": "Invite code is required"
  }
}
```

- `401 Unauthorized`

```json
{
  "generalError": "Authentication required",
  "errors": {
    "token": "Authentication required"
  }
}
```

- `403 Forbidden`

```json
{
  "generalError": "Missing permission to access this group",
  "errors": {
    "permission": "Missing permission to access this group"
  }
}
```

- `404 Not Found`

```json
{
  "generalError": "Invalid invite code or group id",
  "errors": {
    "inviteCodeOrId": "Invalid invite code or group id"
  }
}
```
