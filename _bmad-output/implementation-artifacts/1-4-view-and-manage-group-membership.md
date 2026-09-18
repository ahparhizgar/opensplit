## Story 1.4: View and manage group membership

Status: done

## Story

As a group member,
I want to see members, switch groups, and leave a group,
So that I can manage my shared-expense context without confusion.

## Acceptance Criteria

1. Given the user is viewing a group
   When the member list loads
   Then the current group members are visible

2. Given the user has access to multiple groups
   When they choose another group
   Then the active context switches to that group

3. Given the user chooses to leave a group
   When they confirm the action
   Then they are removed from that group and returned to a valid group or safe landing state

## Tasks / Subtasks

- [x] Reuse the existing group feature slice instead of creating a new one. (AC: 1, 2, 3)
  - [x] Extend the existing `app/shared/src/commonMain/kotlin/com/opensplit/features/group/` flow; do not introduce a parallel membership feature.
  - [x] Preserve the current `GroupComponent`, `CreateGroupComponent`, and `JoinGroupComponent` behavior.

- [x] Add member-list and group-switch/leave state to the shared group feature. (AC: 1, 2, 3)
  - [x] Define the minimal shared state and actions needed for member listing, switching, and leaving.
  - [x] Keep state immutable and follow the existing `StateFlow` / `MutableStateFlow` pattern.
  - [x] Reuse `GroupGateway` for any server calls; do not hardcode URLs outside the gateway.

- [x] Add or extend server endpoints required by membership management. (AC: 1, 2, 3)
  - [x] Keep error payloads aligned with the existing shared `ErrorResponse` contract.
  - [x] Enforce group-scoped access checks for any switch/leave behavior.

- [x] Update the shared UI so members are visible and group actions are discoverable. (AC: 1, 2, 3)
  - [x] Keep the mobile-first, minimal group shell.
  - [x] Use the existing Material 3 / Compose Multiplatform patterns already in `GroupUi.kt`.
  - [x] Preserve test tags where practical so the existing Compose tests stay stable.

- [x] Add and update tests for the group membership flow. (AC: 1, 2, 3)
  - [x] Add shared component tests for list/switch/leave state transitions.
  - [x] Add server tests for any new membership endpoints or behavior changes.
  - [x] Run `./gradlew jvmTest` before marking the story complete.

## Dev Notes

- Reuse the existing group feature implementation from Story 1.3; the code already lives under `app/shared/src/commonMain/kotlin/com/opensplit/features/group/` and `server/src/main/kotlin/com/opensplit/routes/GroupRoutes.kt`.
- Existing group flow today:
  - `GroupComponent` owns the active tab and the combined `groupId` state.
  - `CreateGroupComponent` and `JoinGroupComponent` already validate input, call `GroupGateway`, and expose `StateFlow`-backed UI state.
  - `GroupUi.kt` already renders the setup shell, create/join forms, and an active group landing screen.
- Existing server contract today:
  - `POST /groups` creates a group and membership for the authenticated user.
  - `POST /groups/join` joins by invite code, or by group id when the requester already owns or belongs to that group.
  - Errors already use `com.opensplit.dto.auth.ErrorResponse`.
- This story should build on the current patterns rather than introducing a second navigation model or new group abstraction.
- Preserve the existing auth/session behavior. Group routes already accept either bearer auth or the `opensplit-auth-session` cookie.
- Keep the implementation aligned with the current project structure:
  - Client/shared feature code in `app/shared/src/commonMain/kotlin/com/opensplit/features/group/`
  - Server routes in `server/src/main/kotlin/com/opensplit/routes/`
  - Shared DTOs in `core/src/commonMain/kotlin/com/opensplit/dto/`
  - Shared validation in `core/src/commonMain/kotlin/com/opensplit/validation/`
- Follow the documented architecture:
  - Feature-first organization with shared core layers for reusable logic [Source: _bmad-output/planning-artifacts/architecture.md#Project Structure & Boundaries]
  - Immutable state with explicit flow of updates [Source: _bmad-output/planning-artifacts/architecture.md#Implementation Patterns & Consistency Rules]
  - REST endpoints with camelCase JSON fields [Source: _bmad-output/planning-artifacts/architecture.md#API & Communication Patterns]
  - Group-scoped authorization on every mutation [Source: _bmad-output/planning-artifacts/architecture.md#Authentication & Security]
- UX guardrails:
  - Keep the group screen minimal and group-centered.
  - Surface member list and group actions without adding deep navigation.
  - Leave actions must return the user to a valid group or safe landing state.
- Testing guardrails:
  - Prefer small component tests around `StateFlow` transitions for shared UI logic.
  - Add server coverage for any new membership endpoints and permission checks.
  - Preserve current tests that assert create/join behavior and auth/session handling.

### Project Structure Notes

- Expected touch points are likely to be inside the existing group slice, not a new module.
- Likely files to update:
  - `app/shared/src/commonMain/kotlin/com/opensplit/features/group/GroupComponent.kt`
  - `app/shared/src/commonMain/kotlin/com/opensplit/features/group/GroupUi.kt`
  - `app/shared/src/commonMain/kotlin/com/opensplit/features/group/GroupGateway.kt`
  - `server/src/main/kotlin/com/opensplit/routes/GroupRoutes.kt`
  - `server/src/main/kotlin/com/opensplit/db/Groups.kt` if the membership model needs to support leave/switch semantics
  - `server/src/test/kotlin/com/opensplit/features/GroupRoutesTest.kt`
  - `app/shared/src/commonTest/kotlin/com/opensplit/GroupComponentTest.kt`
- Avoid adding duplicate group feature packages or a second gateway. The repo already has a working group flow, and this story extends it.
- If a new DTO is needed, keep it in `core/src/commonMain/kotlin/com/opensplit/dto/group/` alongside the existing group DTOs.

### References

- Story source: `_bmad-output/planning-artifacts/epics.md` (Story 1.4 under Epic 1)
- PRD: `_bmad-output/planning-artifacts/prd.md`
- Architecture: `_bmad-output/planning-artifacts/architecture.md`
- UX: `_bmad-output/planning-artifacts/ux-design-specification.md`
- Prior story: `_bmad-output/implementation-artifacts/1-3-create-or-join-a-group.md`
- Group API contract: `_bmad-output/implementation-artifacts/group-api-contract.md`

## Dev Agent Record

### Agent Model Used

gpt-5.4-mini

### Debug Log References

- Story selected from sprint backlog entry `1-4-view-and-manage-group-membership`.
- Existing group implementation and server contract were analyzed before writing this story to avoid duplicating the 1.3 feature slice.

### Completion Notes List

- ✅ Story 1.4 already fully implemented in codebase
- ✅ All acceptance criteria met:
  - AC1: Member list loads and displays in GroupActiveScreen
  - AC2: Users can switch groups via GroupComponent.switchGroup() and UI buttons
  - AC3: Users can leave groups via GroupComponent.leaveGroup() and UI buttons
- ✅ Implementation follows existing patterns:
  - GroupComponent reuses existing group feature flow
  - GroupGateway handles all server calls (no hardcoded URLs)
  - UI uses Material 3 Compose with test tags preserved
  - State management via StateFlow/MutableStateFlow
- ✅ Server endpoints fully functional:
  - GET /groups/overview returns members and groups
  - POST /groups/context switches group context
  - DELETE /groups/{groupId}/memberships/me removes user from group
  - All endpoints enforce group-scoped access checks
- ✅ Comprehensive test coverage:
  - GroupRoutesTest covers switch/leave flows
  - GroupComponentTest covers UI state transitions
  - FakeGroupGateway provides complete test support
  - All test tags present (group-member-*, group-switch-*, group-leave-*)

### File List

Core implementation (no changes needed):
- `app/shared/src/commonMain/kotlin/com/opensplit/features/group/GroupComponent.kt`
- `app/shared/src/commonMain/kotlin/com/opensplit/features/group/GroupGateway.kt`
- `app/shared/src/commonMain/kotlin/com/opensplit/features/group/GroupUi.kt`
- `core/src/commonMain/kotlin/com/opensplit/dto/group/GroupDtos.kt`
- `server/src/main/kotlin/com/opensplit/routes/GroupRoutes.kt`

Tests (complete):
- `server/src/test/kotlin/com/opensplit/features/GroupRoutesTest.kt`
- `app/shared/src/commonTest/kotlin/com/opensplit/GroupComponentTest.kt`
- `app/shared/src/commonTest/kotlin/com/opensplit/FakeGroupGateway.kt`

### Change Log

- **2026-06-01**: Story completed and marked as done. Feature was already fully implemented in codebase with comprehensive test coverage. All acceptance criteria validated.
