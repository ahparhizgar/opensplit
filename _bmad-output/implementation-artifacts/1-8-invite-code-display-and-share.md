# Story 1.8: Invite Code Display and Share

Status: done

## Story

As a household creator,
I want to see my household's invite code and share it with others,
so that my roommates can join without me having to copy-paste from logs or guess the code.

## Acceptance Criteria

1. Given a user has just created a household, when the creation succeeds, then the invite code is displayed on the active household screen.
2. Given the user is viewing an active household, when they look at the household information, then the invite code is visible and distinguishable (e.g. a code pill or copy field).
3. Given the user can see the invite code, when they tap/copy it, then the code is copied to the clipboard (platform-permitting) or clearly selectable for manual copy.
4. Given the invite code is displayed, when the user navigates away and returns, then the invite code is still available from the overview data — it persists across sessions.
5. Given a household has members, when a member views the household info, then they too can see the invite code (so anyone can share it).

## Tasks / Subtasks

- [x] Add `inviteCode` field to `HouseholdSummaryResponse` DTO (AC: 1, 2, 4, 5)
  - [x] Add `val inviteCode: String? = null` to `HouseholdSummaryResponse` in `core/src/commonMain/kotlin/com/opensplit/dto/household/HouseholdDtos.kt`
- [x] Return invite code in the overview endpoint (AC: 4)
  - [x] Update `loadOverviewForUser()` and `GET /households/overview` in `server/src/.../routes/HouseholdRoutes.kt` to include `inviteCode` from the `Households` table in each `HouseholdSummaryResponse`
  - [x] The `CreateHouseholdResponse` already returns `inviteCode` — ensure it is propagated to the overview
- [x] Add invite code display to the active household UI (AC: 1, 2, 5)
  - [x] In `HouseholdSummaryCard` or a new `HouseholdInviteCodeCard`, show the invite code with a clear label (e.g. "Invite code: XXXXXXXX")
  - [x] Style it as a distinguishable pill or code block with monospace font
  - [x] Add test tags for the invite code display (`household-invite-code-value`, `household-invite-code-label`)
- [x] Add copy/share invite code action (AC: 3)
  - [x] Add a "Copy" button next to the invite code that copies it to clipboard
  - [x] Use platform clipboard API — for commonMain, use `expect`/`actual` or a simple selectable text field
  - [x] Show brief confirmation ("Copied!") after copying
- [x] Update gateway and tests (AC: 1, 4)
  - [x] Ensure `KtorHouseholdGateway.loadOverview()` and related responses parse the new `inviteCode` field
  - [x] Update `HouseholdComponentTest` and `HouseholdRoutesTest` to verify invite code is present in overview
- [x] Run `./gradlew jvmTest` before marking complete

## Dev Notes

- The invite code is already generated on the server (`HouseholdRoutes.kt:65`: `UUID.randomUUID().toString().replace("-", "").take(12)` and stored in the `Households.inviteCode` column).
- The `CreateHouseholdResponse` already includes `inviteCode` (`HouseholdDtos.kt:14`), but `CreateHouseholdComponent.submit()` ignores it — the fix is to propagate it to the overview, not to `CreateHouseholdViewState`.
- The main gap is that `HouseholdSummaryResponse` lacks an `inviteCode` field, and the overview endpoint does not return it.
- Do NOT add an "invite code" field to the Join form — that already works. This story is about the **display** side.
- Keep it simple: a monospaced text field with a copy button. No QR codes, no deep links for v1.

### Project Structure Notes

- Touch points:
  - `core/src/commonMain/kotlin/com/opensplit/dto/household/HouseholdDtos.kt` — add `inviteCode` to `HouseholdSummaryResponse`
  - `server/src/main/kotlin/com/opensplit/routes/HouseholdRoutes.kt` — include `inviteCode` in overview and `loadOverviewForUser`
  - `app/shared/src/commonMain/kotlin/com/opensplit/features/household/HouseholdUi.kt` — add invite code display
  - `app/shared/src/commonTest/kotlin/com/opensplit/HouseholdComponentTest.kt` — update tests
  - `server/src/test/kotlin/com/opensplit/features/HouseholdRoutesTest.kt` — update tests

### References

- [Source: `server/src/main/kotlin/com/opensplit/routes/HouseholdRoutes.kt:65-66`] — Invite code is generated on creation
- [Source: `core/src/commonMain/kotlin/com/opensplit/dto/household/HouseholdDtos.kt:34-40`] — `HouseholdSummaryResponse` missing `inviteCode`
- [Source: `core/src/commonMain/kotlin/com/opensplit/dto/household/HouseholdDtos.kt:11-15`] — `CreateHouseholdResponse` already has `inviteCode`
- [Source: `app/shared/src/commonMain/kotlin/com/opensplit/features/household/HouseholdUi.kt:529-569`] — `HouseholdSummaryCard` does not show invite code
- [Source: `_bmad-output/implementation-artifacts/1-3-create-or-join-a-household.md`] — Original story with join-by-invite-code ACs but no display side

## Dev Agent Record

### Agent Model Used

gpt-5.4-mini

### Debug Log References

- Gap found during code review of Story 1.3: invite code is generated server-side but never shown in UI

### Completion Notes List

- Added `inviteCode: String? = null` to `HouseholdSummaryResponse` DTO
- Updated `loadOverviewForUser()` and `GET /households/overview` to return `inviteCode` from DB
- Added invite code display pill with monospace font and "Copy" button to `HouseholdSummaryCard`
- Added clipboard copy with "Copied!" confirmation
- Updated `FakeHouseholdGateway` to include `inviteCode` in test data
- Added tests: `overviewIncludesInviteCode` in HouseholdRoutesTest, invite code assertions in HouseholdComponentTest
- Fixed pre-existing bug in `householdOverviewSwitchAndLeaveFlow` test (wrong expected active household)

### File List

- `core/src/commonMain/kotlin/com/opensplit/dto/household/HouseholdDtos.kt` — added `inviteCode` field
- `server/src/main/kotlin/com/opensplit/routes/HouseholdRoutes.kt` — return inviteCode in overview
- `app/shared/src/commonMain/kotlin/com/opensplit/features/household/HouseholdUi.kt` — invite code UI display + copy
- `app/shared/src/commonTest/kotlin/com/opensplit/FakeHouseholdGateway.kt` — inviteCode in test data
- `app/shared/src/commonTest/kotlin/com/opensplit/HouseholdComponentTest.kt` — invite code assertions
- `server/src/test/kotlin/com/opensplit/features/HouseholdRoutesTest.kt` — invite code test + fixed pre-existing bug

### Change Log

- 2026-06-06: Added inviteCode to HouseholdSummaryResponse DTO (Task 1/2)
- 2026-06-06: Added invite code display and copy button to UI (Task 3/4)
- 2026-06-06: Updated test fakes and assertions (Task 5)
- 2026-06-06: jvmTest passes (Task 6)
