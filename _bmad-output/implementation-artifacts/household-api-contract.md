# Household API Contract

## POST `/households`

Creates a household for the authenticated user and makes that user a member.

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
  "errors": {
    "name": "Name must not be empty"
  }
}
```

- `401 Unauthorized`

```json
{
  "errors": {
    "token": "Authentication required"
  }
}
```

## POST `/households/join`

Joins an existing household using an invite code, or accesses by household id if requester is already an owner/member.

### Request

```json
{
  "inviteCodeOrId": "a1b2c3d4e5f6"
}
```

### Success Response (`200 OK`)

```json
{
  "householdId": "8d0b8eb8-1e3f-4ba2-bf3c-686f79d17a11",
  "joined": true
}
```

### Error Responses

- `400 Bad Request`

```json
{
  "errors": {
    "inviteCodeOrId": "Invite code is required"
  }
}
```

- `401 Unauthorized`

```json
{
  "errors": {
    "token": "Authentication required"
  }
}
```

- `403 Forbidden`

```json
{
  "errors": {
    "permission": "Missing permission to access this household"
  }
}
```

- `404 Not Found`

```json
{
  "errors": {
    "inviteCodeOrId": "Invalid invite code or household id"
  }
}
```
