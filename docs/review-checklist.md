# Code Review Checklist

Use this checklist during every code review. Each item must be verified before marking a story `done`.

## Security

- [ ] Passwords and secrets never logged or stored in plaintext
- [ ] Auth tokens are signed JWTs with expiry (not fake/placeholder tokens)
- [ ] Input validation happens server-side (client-side is convenience, not trust)
- [ ] Environment variables used for secrets (JWT_SECRET, JDBC_DATABASE_URL, API keys)
- [ ] SQL/query parameters use parameterized queries or ORM escape (no string concatenation)

## API & Contracts

- [ ] DTOs match the planned contract (no extra fields, no missing fields)
- [ ] Error responses follow the shared `ErrorResponse` format
- [ ] Endpoints return appropriate HTTP status codes (200, 201, 400, 401, 403, 404)

## UI & UX

- [ ] Loading, empty, and error states are handled (not just the happy path)
- [ ] Data that is created is also displayable (e.g., invite code shown after creation)
- [ ] Forms have inline validation with clear messages
- [ ] Password fields use `PasswordVisualTransformation` and `KeyboardType.Password`

## Testing

- [ ] `./gradlew jvmTest` passes before merge
- [ ] New endpoints have server-side route tests
- [ ] Shared logic has unit tests in the shared module
- [ ] UI state changes have component tests (StateFlow assertions)
- [ ] Edge cases tested (empty lists, null values, invalid input, expired tokens)

## Code Quality

- [ ] No generated IDE/build artifacts committed
- [ ] Feature follows the established module structure (not a new flat package)
- [ ] Immutable state pattern used (StateFlow / MutableStateFlow)
- [ ] No hardcoded URLs or config values

## Story Definition Standards

Before a story is marked `ready-for-dev`, verify:

- [ ] ACs cover the happy path and the display/feedback side (not just creation)
- [ ] Error, empty, and edge-case states are described in ACs
- [ ] Relevant UX-DRs from the UX spec are referenced
- [ ] Dependencies on previous stories are documented
- [ ] The `## Dev Notes` section includes testing expectations
