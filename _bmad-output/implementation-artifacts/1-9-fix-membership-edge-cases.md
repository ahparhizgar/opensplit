# Story 1.9: Fix Membership Edge Cases

Status: done

## Story

As a household member,
I want the member list to correctly identify me and handle edge cases like leaving my last household,
so that the membership experience is accurate and trustworthy.

## Acceptance Criteria

1. Given the user is viewing the household member list, when the overview loads, then the current user is labeled "You" in the member list.
2. Given the user is the last member leaving a household, when they confirm the leave action, then they are returned to the household setup screen (safe landing state).
3. Given the owner leaves a household, when the leave is processed, then the household continues to exist (ownership transfers to another member, or the behavior is documented and a warning is shown).

## Tasks / Subtasks

- [x] Fix `isCurrentUser` flag in the server overview endpoint (AC: 1)
  - [x] In `loadOverviewForUser()` in `HouseholdRoutes.kt`, compare each member's `userId` with the requesting user's ID
  - [x] Set `isCurrentUser = true` on the matching `HouseholdMemberResponse`
  - [x] Verify fix: the `"You"` label in `HouseholdUi.kt:638` should now render for the current user
- [x] Add test for leaving the last household (AC: 2)
  - [x] Add a test in `HouseholdRoutesTest.kt`: create a household, join as user, leave → assert `activeHouseholdId` is null and households list is empty
  - [x] Add a test in `HouseholdComponentTest.kt`: leave the only household → assert `householdId` becomes null and UI shows setup screen
- [x] Handle ownership transfer when owner leaves (AC: 3)
  - [x] In `DELETE /households/{householdId}/memberships/me`, check if the leaving user is the owner
  - [x] If owner, transfer ownership to the next member (oldest membership) before deleting the membership row
  - [x] If no other members exist, allow the leave (household becomes ownerless — can be handled later or the household is effectively abandoned)
  - [x] Add tests for: (a) owner leaves with other members → ownership transfers, (b) owner leaves as last member → household becomes ownerless
  - [x] Show a warning in the leave confirmation dialog when the user is the owner: "As the owner, leaving will transfer ownership to another member."
- [x] Run `./gradlew jvmTest` before marking complete

## Dev Notes

- The `isCurrentUser` bug is in `HouseholdRoutes.kt:303-315` — the `loadOverviewForUser()` function builds `HouseholdMemberResponse` without comparing `userId` to the requesting user. Compare with the logic in `GET /households/overview` (lines 200-211) which has the same bug.
- For AC 2, the leave endpoint (lines 248-275) already handles this: it deletes context and if `stillMember` is null, no new context is inserted. The client already handles null `householdId` → shows `HouseholdSetupScreen`. Just need test coverage.
- For AC 3, ownership transfer is new logic. The `Households` table has `ownerId`. Before deleting the membership, query for another member and update `Households.ownerId`.
- Keep changes minimal: only fix the known gaps, do not refactor the household routes.

### Project Structure Notes

- Touch points:
  - `server/src/main/kotlin/com/opensplit/routes/HouseholdRoutes.kt` — fix `isCurrentUser`, add ownership transfer
  - `server/src/test/kotlin/com/opensplit/features/HouseholdRoutesTest.kt` — add tests for AC 1, 2, 3
  - `app/shared/src/commonTest/kotlin/com/opensplit/HouseholdComponentTest.kt` — add leave-last-household test
  - `app/shared/src/commonMain/kotlin/com/opensplit/features/household/HouseholdUi.kt` — update leave confirm dialog for owner warning

### References

- [Source: `server/src/main/kotlin/com/opensplit/routes/HouseholdRoutes.kt:303-315`] — `loadOverviewForUser()` missing `isCurrentUser`
- [Source: `server/src/main/kotlin/com/opensplit/routes/HouseholdRoutes.kt:200-211`] — Same bug in GET /households/overview
- [Source: `server/src/main/kotlin/com/opensplit/routes/HouseholdRoutes.kt:248-275`] — Leave endpoint (safe landing works, no ownership transfer)
- [Source: `app/shared/src/commonMain/kotlin/com/opensplit/features/household/HouseholdUi.kt:820-857`] — Leave confirmation dialog
- [Source: `app/shared/src/commonMain/kotlin/com/opensplit/dto/household/HouseholdDtos.kt:43-48`] — `HouseholdMemberResponse` with `isCurrentUser` field

## Dev Agent Record

### Agent Model Used

gpt-5.4-mini

### Debug Log References

- Gaps found during code review of Story 1.4: `isCurrentUser` never set by server, no test for leaving last household, no ownership transfer

### Completion Notes List

- Implemented `isCurrentUser` flag in both GET /households/overview and loadOverviewForUser() in HouseholdRoutes.kt
- Added server test: `overviewMarksCurrentUser` verifying isCurrentUser is set
- Added server test: `leavingLastHouseholdReturnsSafeLandingState` for AC 2
- Added component test: leaving the only household → null activeHouseholdId, empty households
- Added ownership transfer logic in DELETE /memberships/me — transfers to another member if owner leaves, allows ownerless if last member
- Added server tests: `ownerLeavesWithOtherMembersTransfersOwnership` and `ownerLeavesAsLastMemberHouseholdBecomesOwnerless`
- Added `isOwner` flag to HouseholdSummaryResponse in both overview endpoints
- Added owner warning text to HouseholdLeaveConfirmDialog
- All tasks complete, all tests pass

### File List

- `server/src/main/kotlin/com/opensplit/routes/HouseholdRoutes.kt` — fixed isCurrentUser, added isOwner to summary, added ownership transfer
- `server/src/test/kotlin/com/opensplit/features/HouseholdRoutesTest.kt` — added 4 new tests
- `app/shared/src/commonTest/kotlin/com/opensplit/HouseholdComponentTest.kt` — added single-household leave test
- `app/shared/src/commonTest/kotlin/com/opensplit/FakeHouseholdGateway.kt` — added withSingleHousehold() helper
- `app/shared/src/commonMain/kotlin/com/opensplit/features/household/HouseholdUi.kt` — added owner warning to leave dialog

### Change Log

- Added isCurrentUser flag to HouseholdMemberResponse in both overview endpoints
- Added isOwner flag to HouseholdSummaryResponse in both overview endpoints
- Added ownership transfer logic in DELETE /memberships/me
- Added owner warning text to leave confirmation dialog
- Added 4 new server-side tests and 1 new component test
