# Feature Specification: Group Last Interaction At

**Feature Branch**: `001-group-last-interaction`

**Created**: 2026-09-24

**Status**: Draft

**Input**: User description: "we want to backend returns Group.lastInteractionAt so that in group list @app/shared/src/commonMain/kotlin/com/opensplit/features/group/my/MyGroupListScreen.kt we can always show groups with interaction recently, even if they are settled. /caveman"

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Show Recently Active Settled Groups in Main List (Priority: P1)

User has settled group with recent activity (<= 7 days). Group must stay in active group list, not hide under settled section. User inspects recent activity without expand toggle.

**Why this priority**: Core value. Prevent settled group with fresh activity vanish from main view.

**Independent Test**:
Create group. Balance = 0.0 with activity today. Open group list. Group in active section because `lastInteractionAt >= now - 7.days`.

**Acceptance Scenarios**:

1. **Given** group balance = 0.0 and interaction <= 7 days ago, **When** user views group list, **Then** group in active section.
2. **Given** group balance = 0.0 and interaction > 7 days ago, **When** user views group list, **Then** group in settled section.
3. **Given** group balance != 0.0, **When** user views group list, **Then** group in active section regardless of `lastInteractionAt`.

---

### User Story 2 - Update Interaction Timestamp on Group Events (Priority: P2)

Group activity happen (create/edit/delete expense, create group) -> update `lastInteractionAt` to event time. Member fetch group -> get fresh timestamp.

**Why this priority**: Freshness must match reality across devices.

**Independent Test**:
Add expense to group. Verify group metadata `lastInteractionAt` match event time.

**Acceptance Scenarios**:

1. **Given** group exist, **When** user adds expense, **Then** group `lastInteractionAt` update to now.
2. **Given** new group created, **When** group saved, **Then** group `lastInteractionAt` set to creation time.
3. **Given** client fetch groups from server, **Then** response include `lastInteractionAt`.

---

### User Story 3 - Offline Caching of Group Interaction Timestamp (Priority: P3)

User go offline. Local DB persist `lastInteractionAt`. Group partitioning (active vs settled) stay same offline.

**Why this priority**: Offline-first rule. No jump or wrong list state when offline.

**Independent Test**:
Sync groups online. Enable airplane mode. Relaunch app. Group partitioning match online state.

**Acceptance Scenarios**:

1. **Given** groups synced with `lastInteractionAt`, **When** app open offline, **Then** cached `lastInteractionAt` used for active/settled partition.

---

### Edge Cases

- **Group with zero expenses**: `lastInteractionAt` = group creation time. Group stay in active list initially.
- **Delete only expense in group**: Deletion = interaction. `lastInteractionAt` update to deletion time (or fall back to creation time).
- **No settlement feature yet**: Settlement feature out of scope. Expense edits/creates drive balance and interaction time.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: System MUST track and persist `lastInteractionAt` timestamp for each group.
- **FR-002**: System MUST update `lastInteractionAt` when expense created, updated, or deleted.
- **FR-003**: System MUST set `lastInteractionAt` to creation time on group create.
- **FR-004**: Backend group DTOs MUST include `lastInteractionAt` in list and get responses.
- **FR-005**: Client Room/SQLite DB MUST store and observe `lastInteractionAt`.
- **FR-006**: Client group list MUST place group in active section if `!isSettled` OR `lastInteractionAt >= now - 7.days`.
- **FR-007**: Client group list MUST place group in settled section if `isSettled` AND `lastInteractionAt < now - 7.days`.

### Key Entities *(include if feature involves data)*

- **Group**:
  - `id`: Unique identifier.
  - `name`: Display name.
  - `balance`: Current user balance.
  - `isSettled`: Derived flag (`balance == 0.0`).
  - `lastInteractionAt`: Timestamp (`Instant`) of latest activity.
  - `members`: List of group members.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: 100% of settled groups with activity <= 7 days appear in active section without expanding settled list.
- **SC-002**: Group list partition settled groups with 100% accuracy based on `lastInteractionAt >= now - 7.days`.
- **SC-003**: Backend group responses return valid ISO-8601 `lastInteractionAt`.
- **SC-004**: Offline launch maintain identical group partition state from cache.

## Assumptions

- Freshness window = 7 days (`7.days`), matching `MyGroupListScreen.kt`.
- Deletion/modification of expense count as interaction event.
- Legacy groups without timestamp default to creation time.
