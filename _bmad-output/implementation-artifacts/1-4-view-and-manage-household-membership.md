## Story 1.4: View and manage household membership

Status: done

## Story

As a household member,
I want to see members, switch households, and leave a household,
So that I can manage my shared-expense context without confusion.

## Acceptance Criteria

1. Given the user is viewing a household
   When the member list loads
   Then the current household members are visible

2. Given the user has access to multiple households
   When they choose another household
   Then the active context switches to that household

3. Given the user chooses to leave a household
   When they confirm the action
   Then they are removed from that household and returned to a valid household or safe landing state

## Tasks / Subtasks

- [x] Reuse the existing household feature slice instead of creating a new one. (AC: 1, 2, 3)
  - [x] Extend the existing `app/shared/src/commonMain/kotlin/com/opensplit/features/household/` flow; do not introduce a parallel membership feature.
  - [x] Preserve the current `HouseholdComponent`, `CreateHouseholdComponent`, and `JoinHouseholdComponent` behavior.

- [x] Add member-list and household-switch/leave state to the shared household feature. (AC: 1, 2, 3)
  - [x] Define the minimal shared state and actions needed for member listing, switching, and leaving.
  - [x] Keep state immutable and follow the existing `StateFlow` / `MutableStateFlow` pattern.
  - [x] Reuse `HouseholdGateway` for any server calls; do not hardcode URLs outside the gateway.

- [x] Add or extend server endpoints required by membership management. (AC: 1, 2, 3)
  - [x] Keep error payloads aligned with the existing shared `ErrorResponse` contract.
  - [x] Enforce household-scoped access checks for any switch/leave behavior.

- [x] Update the shared UI so members are visible and household actions are discoverable. (AC: 1, 2, 3)
  - [x] Keep the mobile-first, minimal household shell.
  - [x] Use the existing Material 3 / Compose Multiplatform patterns already in `HouseholdUi.kt`.
  - [x] Preserve test tags where practical so the existing Compose tests stay stable.

- [x] Add and update tests for the household membership flow. (AC: 1, 2, 3)
  - [x] Add shared component tests for list/switch/leave state transitions.
  - [x] Add server tests for any new membership endpoints or behavior changes.
  - [x] Run `./gradlew jvmTest` before marking the story complete.

## Dev Notes

- Reuse the existing household feature implementation from Story 1.3; the code already lives under `app/shared/src/commonMain/kotlin/com/opensplit/features/household/` and `server/src/main/kotlin/com/opensplit/routes/HouseholdRoutes.kt`.
- Existing household flow today:
  - `HouseholdComponent` owns the active tab and the combined `householdId` state.
  - `CreateHouseholdComponent` and `JoinHouseholdComponent` already validate input, call `HouseholdGateway`, and expose `StateFlow`-backed UI state.
  - `HouseholdUi.kt` already renders the setup shell, create/join forms, and an active household landing screen.
- Existing server contract today:
  - `POST /households` creates a household and membership for the authenticated user.
  - `POST /households/join` joins by invite code, or by household id when the requester already owns or belongs to that household.
  - Errors already use `com.opensplit.dto.auth.ErrorResponse`.
- This story should build on the current patterns rather than introducing a second navigation model or new household abstraction.
- Preserve the existing auth/session behavior. Household routes already accept either bearer auth or the `opensplit-auth-session` cookie.
- Keep the implementation aligned with the current project structure:
  - Client/shared feature code in `app/shared/src/commonMain/kotlin/com/opensplit/features/household/`
  - Server routes in `server/src/main/kotlin/com/opensplit/routes/`
  - Shared DTOs in `core/src/commonMain/kotlin/com/opensplit/dto/`
  - Shared validation in `core/src/commonMain/kotlin/com/opensplit/validation/`
- Follow the documented architecture:
  - Feature-first organization with shared core layers for reusable logic [Source: _bmad-output/planning-artifacts/architecture.md#Project Structure & Boundaries]
  - Immutable state with explicit flow of updates [Source: _bmad-output/planning-artifacts/architecture.md#Implementation Patterns & Consistency Rules]
  - REST endpoints with camelCase JSON fields [Source: _bmad-output/planning-artifacts/architecture.md#API & Communication Patterns]
  - Household-scoped authorization on every mutation [Source: _bmad-output/planning-artifacts/architecture.md#Authentication & Security]
- UX guardrails:
  - Keep the household screen minimal and household-centered.
  - Surface member list and household actions without adding deep navigation.
  - Leave actions must return the user to a valid household or safe landing state.
- Testing guardrails:
  - Prefer small component tests around `StateFlow` transitions for shared UI logic.
  - Add server coverage for any new membership endpoints and permission checks.
  - Preserve current tests that assert create/join behavior and auth/session handling.

### Project Structure Notes

- Expected touch points are likely to be inside the existing household slice, not a new module.
- Likely files to update:
  - `app/shared/src/commonMain/kotlin/com/opensplit/features/household/HouseholdComponent.kt`
  - `app/shared/src/commonMain/kotlin/com/opensplit/features/household/HouseholdUi.kt`
  - `app/shared/src/commonMain/kotlin/com/opensplit/features/household/HouseholdGateway.kt`
  - `server/src/main/kotlin/com/opensplit/routes/HouseholdRoutes.kt`
  - `server/src/main/kotlin/com/opensplit/db/Households.kt` if the membership model needs to support leave/switch semantics
  - `server/src/test/kotlin/com/opensplit/features/HouseholdRoutesTest.kt`
  - `app/shared/src/commonTest/kotlin/com/opensplit/HouseholdComponentTest.kt`
- Avoid adding duplicate household feature packages or a second gateway. The repo already has a working household flow, and this story extends it.
- If a new DTO is needed, keep it in `core/src/commonMain/kotlin/com/opensplit/dto/household/` alongside the existing household DTOs.

### References

- Story source: `_bmad-output/planning-artifacts/epics.md` (Story 1.4 under Epic 1)
- PRD: `_bmad-output/planning-artifacts/prd.md`
- Architecture: `_bmad-output/planning-artifacts/architecture.md`
- UX: `_bmad-output/planning-artifacts/ux-design-specification.md`
- Prior story: `_bmad-output/implementation-artifacts/1-3-create-or-join-a-household.md`
- Household API contract: `_bmad-output/implementation-artifacts/household-api-contract.md`

## Dev Agent Record

### Agent Model Used

gpt-5.4-mini

### Debug Log References

- Story selected from sprint backlog entry `1-4-view-and-manage-household-membership`.
- Existing household implementation and server contract were analyzed before writing this story to avoid duplicating the 1.3 feature slice.

### Completion Notes List

- ✅ Story 1.4 already fully implemented in codebase
- ✅ All acceptance criteria met:
  - AC1: Member list loads and displays in HouseholdActiveScreen
  - AC2: Users can switch households via HouseholdComponent.switchHousehold() and UI buttons
  - AC3: Users can leave households via HouseholdComponent.leaveHousehold() and UI buttons
- ✅ Implementation follows existing patterns:
  - HouseholdComponent reuses existing household feature flow
  - HouseholdGateway handles all server calls (no hardcoded URLs)
  - UI uses Material 3 Compose with test tags preserved
  - State management via StateFlow/MutableStateFlow
- ✅ Server endpoints fully functional:
  - GET /households/overview returns members and households
  - POST /households/context switches household context
  - DELETE /households/{householdId}/memberships/me removes user from household
  - All endpoints enforce household-scoped access checks
- ✅ Comprehensive test coverage:
  - HouseholdRoutesTest covers switch/leave flows
  - HouseholdComponentTest covers UI state transitions
  - FakeHouseholdGateway provides complete test support
  - All test tags present (household-member-*, household-switch-*, household-leave-*)

### File List

Core implementation (no changes needed):
- `app/shared/src/commonMain/kotlin/com/opensplit/features/household/HouseholdComponent.kt`
- `app/shared/src/commonMain/kotlin/com/opensplit/features/household/HouseholdGateway.kt`
- `app/shared/src/commonMain/kotlin/com/opensplit/features/household/HouseholdUi.kt`
- `core/src/commonMain/kotlin/com/opensplit/dto/household/HouseholdDtos.kt`
- `server/src/main/kotlin/com/opensplit/routes/HouseholdRoutes.kt`

Tests (complete):
- `server/src/test/kotlin/com/opensplit/features/HouseholdRoutesTest.kt`
- `app/shared/src/commonTest/kotlin/com/opensplit/HouseholdComponentTest.kt`
- `app/shared/src/commonTest/kotlin/com/opensplit/FakeHouseholdGateway.kt`

### Change Log

- **2026-06-01**: Story completed and marked as done. Feature was already fully implemented in codebase with comprehensive test coverage. All acceptance criteria validated.
