# Story 1.9: Fix Membership Edge Cases

Status: done

## Story

As a group member,
I want the member list to correctly identify me and handle edge cases like leaving my last group,
so that the membership experience is accurate and trustworthy.

## Acceptance Criteria

1. Given the user is viewing the group member list, when the overview loads, then the current user is labeled "You" in the member list.
2. Given the user is the last member leaving a group, when they confirm the leave action, then they are returned to the group setup screen (safe landing state).
3. Given the owner leaves a group, when the leave is processed, then the group continues to exist (ownership transfers to another member, or the behavior is documented and a warning is shown).

## Tasks / Subtasks

- [x] Fix `isCurrentUser` flag in the server overview endpoint (AC: 1)
  - [x] In `loadOverviewForUser()` in `GroupRoutes.kt`, compare each member's `userId` with the requesting user's ID
  - [x] Set `isCurrentUser = true` on the matching `GroupMemberResponse`
  - [x] Verify fix: the `"You"` label in `GroupUi.kt:638` should now render for the current user
- [x] Add test for leaving the last group (AC: 2)
  - [x] Add a test in `GroupRoutesTest.kt`: create a group, join as user, leave → assert `activeGroupId` is null and groups list is empty
  - [x] Add a test in `GroupComponentTest.kt`: leave the only group → assert `groupId` becomes null and UI shows setup screen
- [x] Handle ownership transfer when owner leaves (AC: 3)
  - [x] In `DELETE /groups/{groupId}/memberships/me`, check if the leaving user is the owner
  - [x] If owner, transfer ownership to the next member (oldest membership) before deleting the membership row
  - [x] If no other members exist, allow the leave (group becomes ownerless — can be handled later or the group is effectively abandoned)
  - [x] Add tests for: (a) owner leaves with other members → ownership transfers, (b) owner leaves as last member → group becomes ownerless
  - [x] Show a warning in the leave confirmation dialog when the user is the owner: "As the owner, leaving will transfer ownership to another member."
- [x] Run `./gradlew jvmTest` before marking complete

## Dev Notes

- The `isCurrentUser` bug is in `GroupRoutes.kt:303-315` — the `loadOverviewForUser()` function builds `GroupMemberResponse` without comparing `userId` to the requesting user. Compare with the logic in `GET /groups/overview` (lines 200-211) which has the same bug.
- For AC 2, the leave endpoint (lines 248-275) already handles this: it deletes context and if `stillMember` is null, no new context is inserted. The client already handles null `groupId` → shows `GroupSetupScreen`. Just need test coverage.
- For AC 3, ownership transfer is new logic. The `Groups` table has `ownerId`. Before deleting the membership, query for another member and update `Groups.ownerId`.
- Keep changes minimal: only fix the known gaps, do not refactor the group routes.

### Project Structure Notes

- Touch points:
  - `server/src/main/kotlin/com/opensplit/routes/GroupRoutes.kt` — fix `isCurrentUser`, add ownership transfer
  - `server/src/test/kotlin/com/opensplit/features/GroupRoutesTest.kt` — add tests for AC 1, 2, 3
  - `app/shared/src/commonTest/kotlin/com/opensplit/GroupComponentTest.kt` — add leave-last-group test
  - `app/shared/src/commonMain/kotlin/com/opensplit/features/group/GroupUi.kt` — update leave confirm dialog for owner warning

### References

- [Source: `server/src/main/kotlin/com/opensplit/routes/GroupRoutes.kt:303-315`] — `loadOverviewForUser()` missing `isCurrentUser`
- [Source: `server/src/main/kotlin/com/opensplit/routes/GroupRoutes.kt:200-211`] — Same bug in GET /groups/overview
- [Source: `server/src/main/kotlin/com/opensplit/routes/GroupRoutes.kt:248-275`] — Leave endpoint (safe landing works, no ownership transfer)
- [Source: `app/shared/src/commonMain/kotlin/com/opensplit/features/group/GroupUi.kt:820-857`] — Leave confirmation dialog
- [Source: `app/shared/src/commonMain/kotlin/com/opensplit/dto/group/GroupDtos.kt:43-48`] — `GroupMemberResponse` with `isCurrentUser` field

## Dev Agent Record

### Agent Model Used

gpt-5.4-mini

### Debug Log References

- Gaps found during code review of Story 1.4: `isCurrentUser` never set by server, no test for leaving last group, no ownership transfer

### Completion Notes List

- Implemented `isCurrentUser` flag in both GET /groups/overview and loadOverviewForUser() in GroupRoutes.kt
- Added server test: `overviewMarksCurrentUser` verifying isCurrentUser is set
- Added server test: `leavingLastGroupReturnsSafeLandingState` for AC 2
- Added component test: leaving the only group → null activeGroupId, empty groups
- Added ownership transfer logic in DELETE /memberships/me — transfers to another member if owner leaves, allows ownerless if last member
- Added server tests: `ownerLeavesWithOtherMembersTransfersOwnership` and `ownerLeavesAsLastMemberGroupBecomesOwnerless`
- Added `isOwner` flag to GroupSummaryResponse in both overview endpoints
- Added owner warning text to GroupLeaveConfirmDialog
- All tasks complete, all tests pass

### File List

- `server/src/main/kotlin/com/opensplit/routes/GroupRoutes.kt` — fixed isCurrentUser, added isOwner to summary, added ownership transfer
- `server/src/test/kotlin/com/opensplit/features/GroupRoutesTest.kt` — added 4 new tests
- `app/shared/src/commonTest/kotlin/com/opensplit/GroupComponentTest.kt` — added single-group leave test
- `app/shared/src/commonTest/kotlin/com/opensplit/FakeGroupGateway.kt` — added withSingleGroup() helper
- `app/shared/src/commonMain/kotlin/com/opensplit/features/group/GroupUi.kt` — added owner warning to leave dialog

### Change Log

- Added isCurrentUser flag to GroupMemberResponse in both overview endpoints
- Added isOwner flag to GroupSummaryResponse in both overview endpoints
- Added ownership transfer logic in DELETE /memberships/me
- Added owner warning text to leave confirmation dialog
- Added 4 new server-side tests and 1 new component test
