# Story 1.8: Invite Code Display and Share

Status: done

## Story

As a group creator,
I want to see my group's invite code and share it with others,
so that my roommates can join without me having to copy-paste from logs or guess the code.

## Acceptance Criteria

1. Given a user has just created a group, when the creation succeeds, then the invite code is displayed on the active group screen.
2. Given the user is viewing an active group, when they look at the group information, then the invite code is visible and distinguishable (e.g. a code pill or copy field).
3. Given the user can see the invite code, when they tap/copy it, then the code is copied to the clipboard (platform-permitting) or clearly selectable for manual copy.
4. Given the invite code is displayed, when the user navigates away and returns, then the invite code is still available from the overview data — it persists across sessions.
5. Given a group has members, when a member views the group info, then they too can see the invite code (so anyone can share it).

## Tasks / Subtasks

- [x] Add `inviteCode` field to `GroupSummaryResponse` DTO (AC: 1, 2, 4, 5)
  - [x] Add `val inviteCode: String? = null` to `GroupSummaryResponse` in `core/src/commonMain/kotlin/com/opensplit/dto/group/GroupDtos.kt`
- [x] Return invite code in the overview endpoint (AC: 4)
  - [x] Update `loadOverviewForUser()` and `GET /groups/overview` in `server/src/.../routes/GroupRoutes.kt` to include `inviteCode` from the `Groups` table in each `GroupSummaryResponse`
  - [x] The `CreateGroupResponse` already returns `inviteCode` — ensure it is propagated to the overview
- [x] Add invite code display to the active group UI (AC: 1, 2, 5)
  - [x] In `GroupSummaryCard` or a new `GroupInviteCodeCard`, show the invite code with a clear label (e.g. "Invite code: XXXXXXXX")
  - [x] Style it as a distinguishable pill or code block with monospace font
  - [x] Add test tags for the invite code display (`group-invite-code-value`, `group-invite-code-label`)
- [x] Add copy/share invite code action (AC: 3)
  - [x] Add a "Copy" button next to the invite code that copies it to clipboard
  - [x] Use platform clipboard API — for commonMain, use `expect`/`actual` or a simple selectable text field
  - [x] Show brief confirmation ("Copied!") after copying
- [x] Update gateway and tests (AC: 1, 4)
  - [x] Ensure `KtorGroupGateway.loadOverview()` and related responses parse the new `inviteCode` field
  - [x] Update `GroupComponentTest` and `GroupRoutesTest` to verify invite code is present in overview
- [x] Run `./gradlew jvmTest` before marking complete

## Dev Notes

- The invite code is already generated on the server (`GroupRoutes.kt:65`: `UUID.randomUUID().toString().replace("-", "").take(12)` and stored in the `Groups.inviteCode` column).
- The `CreateGroupResponse` already includes `inviteCode` (`GroupDtos.kt:14`), but `CreateGroupComponent.submit()` ignores it — the fix is to propagate it to the overview, not to `CreateGroupViewState`.
- The main gap is that `GroupSummaryResponse` lacks an `inviteCode` field, and the overview endpoint does not return it.
- Do NOT add an "invite code" field to the Join form — that already works. This story is about the **display** side.
- Keep it simple: a monospaced text field with a copy button. No QR codes, no deep links for v1.

### Project Structure Notes

- Touch points:
  - `core/src/commonMain/kotlin/com/opensplit/dto/group/GroupDtos.kt` — add `inviteCode` to `GroupSummaryResponse`
  - `server/src/main/kotlin/com/opensplit/routes/GroupRoutes.kt` — include `inviteCode` in overview and `loadOverviewForUser`
  - `app/shared/src/commonMain/kotlin/com/opensplit/features/group/GroupUi.kt` — add invite code display
  - `app/shared/src/commonTest/kotlin/com/opensplit/GroupComponentTest.kt` — update tests
  - `server/src/test/kotlin/com/opensplit/features/GroupRoutesTest.kt` — update tests

### References

- [Source: `server/src/main/kotlin/com/opensplit/routes/GroupRoutes.kt:65-66`] — Invite code is generated on creation
- [Source: `core/src/commonMain/kotlin/com/opensplit/dto/group/GroupDtos.kt:34-40`] — `GroupSummaryResponse` missing `inviteCode`
- [Source: `core/src/commonMain/kotlin/com/opensplit/dto/group/GroupDtos.kt:11-15`] — `CreateGroupResponse` already has `inviteCode`
- [Source: `app/shared/src/commonMain/kotlin/com/opensplit/features/group/GroupUi.kt:529-569`] — `GroupSummaryCard` does not show invite code
- [Source: `_bmad-output/implementation-artifacts/1-3-create-or-join-a-group.md`] — Original story with join-by-invite-code ACs but no display side

## Dev Agent Record

### Agent Model Used

gpt-5.4-mini

### Debug Log References

- Gap found during code review of Story 1.3: invite code is generated server-side but never shown in UI

### Completion Notes List

- Added `inviteCode: String? = null` to `GroupSummaryResponse` DTO
- Updated `loadOverviewForUser()` and `GET /groups/overview` to return `inviteCode` from DB
- Added invite code display pill with monospace font and "Copy" button to `GroupSummaryCard`
- Added clipboard copy with "Copied!" confirmation
- Updated `FakeGroupGateway` to include `inviteCode` in test data
- Added tests: `overviewIncludesInviteCode` in GroupRoutesTest, invite code assertions in GroupComponentTest
- Fixed pre-existing bug in `groupOverviewSwitchAndLeaveFlow` test (wrong expected active group)

### File List

- `core/src/commonMain/kotlin/com/opensplit/dto/group/GroupDtos.kt` — added `inviteCode` field
- `server/src/main/kotlin/com/opensplit/routes/GroupRoutes.kt` — return inviteCode in overview
- `app/shared/src/commonMain/kotlin/com/opensplit/features/group/GroupUi.kt` — invite code UI display + copy
- `app/shared/src/commonTest/kotlin/com/opensplit/FakeGroupGateway.kt` — inviteCode in test data
- `app/shared/src/commonTest/kotlin/com/opensplit/GroupComponentTest.kt` — invite code assertions
- `server/src/test/kotlin/com/opensplit/features/GroupRoutesTest.kt` — invite code test + fixed pre-existing bug

### Change Log

- 2026-06-06: Added inviteCode to GroupSummaryResponse DTO (Task 1/2)
- 2026-06-06: Added invite code display and copy button to UI (Task 3/4)
- 2026-06-06: Updated test fakes and assertions (Task 5)
- 2026-06-06: jvmTest passes (Task 6)
