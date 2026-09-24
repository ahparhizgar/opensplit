# Feature Specification: Group Last Interaction At

**Feature Branch**: `001-group-last-interaction`

**Created**: 2026-09-24

**Status**: Draft

**Input**: User description: "we want to backend returns Group.lastInteractionAt so that in group list @app/shared/src/commonMain/kotlin/com/opensplit/features/group/my/MyGroupListScreen.kt we can always show groups with interaction recently, even if they are settled. /caveman"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - View Recently Active Settled Groups in Main List (Priority: P1)

As a user with settled groups that had recent activity (such as a recently settled expense or new transaction within the last 7 days), I want those groups to appear directly in my active group list rather than tucked away in the settled group section, so I can easily inspect recent activity and follow up without expanding settled groups.

**Why this priority**: Core value of feature. Prevents groups that just settled from immediately disappearing from main user view.

**Independent Test**:
Can be fully tested by creating a group, recording an activity/settlement today that leaves the balance at 0.0, and verifying the group stays in the active group section because its last interaction timestamp is within the active threshold (e.g. past 7 days).

**Acceptance Scenarios**:

1. **Given** a group has a zero balance (settled) and an interaction (expense, payment, or settlement) occurred within the past 7 days, **When** the user views the group list, **Then** the group is displayed in the active groups section.
2. **Given** a group has a zero balance (settled) and its last interaction occurred more than 7 days ago, **When** the user views the group list, **Then** the group is placed in the settled groups section.
3. **Given** a group has a non-zero balance, **When** the user views the group list, **Then** the group is always displayed in the active groups section regardless of `lastInteractionAt`.

---

### User Story 2 - Accurate Interaction Timestamp on Group Events (Priority: P2)

As a group participant, whenever a new activity occurs in the group (expense created, edited, deleted, settlement made, or group created), I want the group's last interaction timestamp to update to the moment of that event, so that interaction freshness reflects reality across all member devices.

**Why this priority**: Ensures interaction freshness stays accurate and synchronized across all group participants.

**Independent Test**:
Record an expense or payment in a group and check the group metadata to confirm `lastInteractionAt` reflects the event timestamp.

**Acceptance Scenarios**:

1. **Given** an existing group, **When** a user creates an expense in the group, **Then** the group's `lastInteractionAt` is updated to the current event timestamp.
2. **Given** an existing group with no expenses yet, **When** the group is first created, **Then** its `lastInteractionAt` defaults to group creation time.
3. **Given** a member fetches their group list from the server, **Then** each group includes `lastInteractionAt` indicating the most recent group activity.

---

### User Story 3 - Offline Caching of Group Interaction Timestamp (Priority: P3)

As a mobile or desktop app user who goes offline, I want the locally stored groups to remember their `lastInteractionAt` timestamp, so that the group list partitioning (active vs. settled) remains consistent even when offline.

**Why this priority**: Preserves offline-first user experience and avoids layout jumps when reconnecting or opening the app offline.

**Independent Test**:
Sync groups while connected, switch to offline mode, reopen the group list, and verify groups partition into active/settled identically based on cached `lastInteractionAt`.

**Acceptance Scenarios**:

1. **Given** groups synced from server with `lastInteractionAt`, **When** the application restarts offline, **Then** cached `lastInteractionAt` values are preserved and used for partitioning.

---

### Edge Cases

- **Group with zero transactions**: `lastInteractionAt` should match group creation timestamp so newly created groups remain in active view during initial setup.
- **Interaction deletion**: If the sole recent expense in a group is deleted, `lastInteractionAt` remains at deletion event timestamp (deletion itself is an interaction).
- **Settlement payment**: A settlement brings group balance to zero; `lastInteractionAt` updates to the settlement timestamp, keeping the group visible in active list for the freshness window (7 days). currently we don't have settlement feature. and we don't want to implement it in this feature.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST track and persist `lastInteractionAt` timestamp for every group.
- **FR-002**: System MUST update `lastInteractionAt` whenever an expense is added, updated, or removed in the group, or when a payment/settlement is made.
- **FR-003**: System MUST set `lastInteractionAt` to the group creation time upon group creation.
- **FR-004**: Backend group responses (list groups and get group) MUST return `lastInteractionAt` in group DTOs.
- **FR-005**: Client MUST store and observe `lastInteractionAt` in local persistent storage.
- **FR-006**: Client group list MUST partition groups into the active section if either:
  - The group is not settled (`!isSettled`), OR
  - The group's `lastInteractionAt` is within the active threshold (within the last 7 days).
- **FR-007**: Client group list MUST partition groups into the settled section only when the group is settled (`isSettled`) AND its `lastInteractionAt` is older than the active threshold (more than 7 days ago).

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of newly settled groups with activity within 7 days remain directly visible in the main active group list without requiring users to tap "Settled groups".
- **SC-002**: Group list screen correctly categorizes groups with 0 balance into active vs. settled according to `lastInteractionAt >= (now - 7 days)`.
- **SC-003**: All group retrieval API responses provide valid ISO-8601 for `lastInteractionAt`.
- **SC-004**: Offline cache preserves `lastInteractionAt` with zero loss of partitioning accuracy across app restarts.

## Assumptions

- The active freshness window threshold remains 7 days, consistent with current UI logic in `MyGroupListScreen.kt`.
- Deleting an expense or modifying a group counts as an interaction event, updating `lastInteractionAt` or reflecting the latest remaining transaction.
- If existing historical groups have null/unrecorded interaction timestamps during migration, they default to group creation time or earliest available record timestamp.
