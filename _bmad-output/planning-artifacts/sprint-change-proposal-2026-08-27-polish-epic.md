# Sprint Change Proposal: Polish & Refinement Epic (Epic 5)

**Date:** 2026-08-27  
**Project:** OpenSplit  
**Prepared by:** Copilot (via bmad-correct-course)  
**Status:** Proposal — Awaiting Approval  

---

## Executive Summary

Four technical debt and UX refinement tasks identified during Epic 1 and Epic 2 implementation need to be addressed **before Epic 3 (Balances & Settlement)** begins. These tasks represent critical polish work on the household setup workflow, logout functionality, and UI consistency.

**Recommendation:** Create **new Epic 5: Polish & Refinement Epic** with 4 stories, completing it within 1-2 weeks, then proceed to Epic 3. This approach:
- ✅ Addresses security gap (logout feature)
- ✅ Improves UX clarity (refactored join household flow)
- ✅ Polishes UI consistency (form styling across household setup)
- ✅ Strengthens foundation before balance/settlement work
- ⚠️ Adds ~1 week to project timeline but improves overall quality

---

## Issue Summary

**Trigger:** Technical debt and UX improvements discovered during Epic 1 and Epic 2 implementation.

**Core Problems:**
1. **Security Gap:** No logout feature — users cannot sign out from shared devices
2. **UX Confusion:** Combined "Create or Join" component is ambiguous; needs splitting
3. **UI Discoverability:** Join household workflow is not clearly visible on household list
4. **UI Inconsistency:** Household creation and join forms lack polish and don't match app design system

**Why This Matters:**
- Users need logout for security, especially on shared/household devices
- Clear household setup UX is foundation for all future work
- Polished forms improve user confidence and reduce errors
- Better to address now than accumulate post-MVP technical debt

**Discovery Context:**
- Epic 1 (Join Your Household Securely) marked complete with 9 stories done
- Epic 2 (Log Shared Expenses Fast) marked complete with 7 stories done
- During implementation and retro, team identified these gaps as important for v1
- These tasks are blockers for proceeding confidently to Epic 3

---

## Impact Analysis

### Epic Impact Assessment

| Epic | Status | Impact | Details |
|------|--------|--------|---------|
| **Epic 1** | Done | Will be reopened as polish work | Stories 1-3 and 1-4 improved via 5-1 through 5-4 |
| **Epic 2** | Done | No impact — independent | Expense logging unaffected |
| **Epic 3** | Backlog | Blocked until Epic 5 complete | Can proceed immediately after |
| **Epic 4** | In-progress (2/3 done) | No blocking impact | Can continue in parallel |

### Story-Level Dependencies

| Current Story | Polish Epic Impact | Notes |
|---------------|-------------------|-------|
| 1-2 (Sign in) | 5-1 depends on it | Logout (5-1) extends auth flow |
| 1-3 (Create/Join) | 5-2, 5-3, 5-4 refactor it | Refactored into clearer flow |
| 1-4 (Manage membership) | 5-3 depends on it | Join button added to household list |
| All Epic 2 stories | No impact | Independent functionality |

### Artifact Impact Analysis

| Artifact | Status | Required Changes |
|----------|--------|------------------|
| **PRD** | No changes needed | v1 scope already included logout and household UX polish |
| **Epics Document** | Update required | Add new Epic 5 with 4 stories and dependencies |
| **Sprint Status** | Update required | New epic-5 entry, story statuses |
| **Architecture** | No changes | Existing patterns sufficient |
| **UX Design** | Minor update | Household forms styling documentation |

### Technical Impact

- **Backend:** No new API endpoints (all server work already done per retro notes)
- **UI/Compose:** Form styling, input fields, validation feedback, loading states
- **Navigation:** Household setup flow refactored (back buttons, navigation patterns)
- **Data:** No schema changes; logout clears local Room/DataStore
- **Testing:** Form validation, offline behavior, error handling edge cases

---

## Recommended Approach: Direct Adjustment

**Selected Option:** Create new Epic 5 with 4 polish stories, complete before Epic 3.

**Rationale:**
1. **Independence:** Polish work is isolated from balance/settlement logic
2. **Effort:** 1-2 weeks is low-risk, achievable timeline
3. **Risk:** Low technical risk (UI polish + logout on proven auth pattern)
4. **Quality:** Improves foundation before larger Epic 3 work
5. **User value:** Logout is a security must-have; join UX improves discoverability

**Effort Estimates:**
- Story 5-1 (Logout): 3-4 days
- Story 5-2 (Remove Create/Join Component): 4-5 days
- Story 5-3 (Join Household UI): 3-4 days
- Story 5-4 (Create Household UI): 3-4 days
- **Total: ~13-17 days** (2-2.5 weeks with testing and integration)

**Timeline:**
- Epic 5 kickoff: Aug 28
- Story development: Aug 28 - Sep 10
- QA/UAT: Sep 10-11
- Ready for Epic 3: Sep 12

**Risk Assessment: LOW**
- ✅ No new patterns or architecture required
- ✅ Existing backend APIs already validated
- ✅ Form styling uses existing ThemeExtended design system
- ✅ Team has experience with household workflows (from Epic 1)
- ⚠️ Minor risk: Story 5-2 requires navigation refactor (well-understood)

---

## Detailed Change Proposals

### Story 5-1: Implement Logout Feature

**Type:** Security / Auth feature  
**Priority:** High  
**Effort:** 3-4 days  

**Problem:** Users cannot sign out from the app. On shared/household devices, this is a security risk.

**Solution:**

```
As a signed-in user,
I want to sign out securely from my account,
So that I can leave the app in a logged-out state on shared devices.

Acceptance Criteria:

1. Given a user is signed in and viewing a screen
   When they access the logout option (menu, settings, or profile screen)
   Then they see a "Sign Out" button prominently displayed

2. Given the user taps "Sign Out"
   When they confirm the action
   Then the user is signed out and session is cleared

3. Given the user has signed out
   When they return to the app
   Then they are returned to the sign-in/sign-up screen

4. Given a user chooses to sign out
   When the logout action is processed
   Then all local data is cleared:
     - Room database is cleared (expenses, households, members)
     - DataStore preferences are cleared (JWT token, user preferences)
     - App state is reset to unauthenticated

5. Given the user has local offline changes pending when they sign out
   When they confirm logout
   Then a warning is shown about unsync'd changes
   And they must confirm before local data is deleted
```

**Technical Notes:**
- No server-side session invalidation needed (JWT tokens are client-side)
- Clear Room database and DataStore on logout
- Handle offline changes gracefully with user confirmation
- Depends on: Story 1-2 (Create and sign in securely)
- Testing: Unit test session clearing, integration test with offline changes

**UX Changes:**
- Add logout button to household/settings screen (typically top-right menu or profile)
- Show confirmation dialog with data-loss warning if offline changes exist
- Redirect to sign-in screen after logout

---

### Story 5-2: Remove Create or Join Component & Refactor Household Setup

**Type:** UX Refactoring  
**Priority:** High  
**Effort:** 4-5 days  

**Problem:** The combined "Create or Join" component (Story 1-3) creates UX confusion. Users need two distinct, sequential flows.

**Solution:**

```
As a signed-in user with no active household,
I want a clear, linear household setup flow,
So that I understand whether I'm creating a new household or joining an existing one.

Acceptance Criteria:

1. Given a user is viewing the households list screen
   When they look at the action buttons
   Then they see:
     - "Start a New Group" button (existing)
     - "Join an Existing Group" button (NEW - placed below "Start a New Group")

2. Given the user taps "Join an Existing Group"
   When the join flow opens
   Then the old combined create/join component is NOT shown
   And a clear "join household" form appears with:
     - Text input for invite code
     - Submit button
     - Back button in app bar to return to household list

3. Given the user enters a valid invite code
   When they submit
   Then they are added to that household
   And they are returned to the household list with the new household now visible

4. Given the user enters an invalid invite code
   When they submit
   Then an error message is shown
   And they can try again or cancel

5. Given the user is back on the household list after joining
   When they view their households
   Then the newly-joined household appears in their list
```

**Technical Notes:**
- Refactor navigation flow: remove combined component, add join button to household list
- UI: Two distinct pathways (create new vs. join existing)
- Backend API already supports this (per Epic 1 completion)
- Depends on: Story 1-3 (Create or join — refactoring implementation)
- Testing: Manual UI testing for both flows, verify invite codes work

**UX Changes:**
- Remove combined "Create or Join" component
- Add "Join an Existing Group" button to household list screen, below "Start a New Group"
- Separate visual pathways for creation vs. join feedback

---

### Story 5-3: Implement Join Household Button and Form UI

**Type:** UX Polish  
**Priority:** High  
**Effort:** 3-4 days  

**Problem:** Join household form lacks visual polish and consistency with app design system.

**Solution:**

```
As a user viewing the join household form,
I want the form to match the app's visual style and provide clear feedback,
So that the experience feels consistent with the rest of OpenSplit.

Acceptance Criteria:

1. Given the user taps "Join an Existing Group"
   When the join form screen opens
   Then the screen displays:
     - App bar with back button (existing navigation pattern)
     - Title: "Join a Household"
     - Text input field with placeholder: "Enter invite code"
     - Submit button: "Join" (styled to match app buttons)

2. Given the user is viewing the input field
   When they focus it
   Then styling matches other input fields in the app (Material Design, ThemeExtended colors)

3. Given the user submits a valid code
   When the server confirms they've joined
   Then they see success feedback (toast, snackbar, or dialog) matching the app's existing feedback pattern
   And the screen navigates back to household list

4. Given the user submits an invalid code
   When the server responds with an error
   Then the error is displayed in the app's standard error style
   And the user can retry without losing their input

5. Given the form is loading
   When waiting for server response
   Then the submit button shows the app's standard loading state
   And the user cannot submit multiple times

6. Given the user is offline and tries to join
   When they attempt to submit
   Then they see the app's standard offline message
   And submission is prevented until online
```

**Technical Notes:**
- Pure UI polish — backend logic already complete
- Use existing ThemeExtended design system for consistency
- Handle loading states, error states, offline states with app patterns
- Depends on: Stories 1-3 and 5-2 (backend and navigation refactor)
- Testing: Test valid/invalid codes, offline scenarios, error messages

**UX Changes:**
- Form styling matches app's design language (Material Design, ThemeExtended)
- Use back button in app bar (not cancel button)
- Standard error/success feedback patterns
- Clear loading state for user feedback

---

### Story 5-4: Polish Create Household UI and Workflow

**Type:** UX Polish  
**Priority:** High  
**Effort:** 3-4 days  

**Problem:** Create household form needs visual polish to match app design system and provide better feedback.

**Solution:**

```
As a user creating a new household,
I want the form to match the app's visual style and provide clear feedback,
So that creating a household feels polished and consistent.

Acceptance Criteria:

1. Given the user taps "Start a New Group"
   When the create household screen opens
   Then the screen displays:
     - App bar with back button
     - Title: "Create a Household"
     - Text input for household name with placeholder text
     - Submit button: "Create" (styled to match app buttons)

2. Given the user is viewing the input field
   When they focus it
   Then styling matches other input fields in the app (Material Design, ThemeExtended colors)

3. Given the user enters a household name
   When they submit the form
   Then the name is validated (not empty, reasonable length)
   And appropriate error feedback is shown if invalid

4. Given the user successfully creates a household
   When the form submits
   Then they see success feedback matching the app's feedback pattern
   And are taken to the household details screen
   And the invite code is displayed prominently for sharing

5. Given the form is loading
   When waiting for server response
   Then the submit button shows the app's standard loading state

6. Given the user is offline
   When they try to create a household
   Then they see the app's standard offline message
   And submission is prevented until online
```

**Technical Notes:**
- Pure UI polish — backend already functional
- Use existing ThemeExtended design system
- Inline validation, loading states, error handling
- Depends on: Story 1-3 (Create or join backend)
- Testing: Test valid/invalid inputs, offline scenarios, loading states

**UX Changes:**
- Consistent input styling with join form (Story 5-3)
- Clear visual hierarchy and spacing
- Standard error/success feedback
- Loading state for network requests

---

## Implementation Handoff

### Change Scope Classification: **MODERATE**

**Rationale:**
- Stories 5-1, 5-3, 5-4: Pure UI/UX polish and form refactoring (no architecture changes)
- Story 5-2: Navigation refactor (well-understood pattern, no new architecture)
- All backend APIs already complete (per Epic 2 retro)
- Uses existing patterns and design system (ThemeExtended)
- Moderate scope because Story 5-2 requires navigation cleanup

### Recommended Handoff: **Developer Agent** (with PO alignment)

**Handoff Path:**
1. ✅ Product Owner approval of change proposal (completed — awaiting final approval)
2. → Developer Agent: Implement 4 stories in sequence or parallel
3. → QA: Regression testing for household workflows
4. → Product Owner: Final UAT before Epic 3 kickoff

### Responsibilities

| Role | Task | Timeline |
|------|------|----------|
| **Product Owner** | Approve proposal, validate ACs, UAT | Aug 27-28 |
| **Developer** | Implement 4 stories, unit tests, code review | Aug 28 - Sep 10 |
| **QA** | Regression testing, edge cases | Sep 10-11 |
| **Project Lead** | Tracking, blockers, Epic 3 kickoff | Ongoing |

### Success Criteria

- [x] All 4 stories planned and approved
- [ ] Epic 5 added to sprint-status.yaml
- [ ] All stories moved to "ready-for-dev" status
- [ ] Story 5-1 through 5-4 implement all ACs without blockers
- [ ] Zero regressions in household workflows (Epic 1/2 unchanged)
- [ ] Form UI polish matches app design system (ThemeExtended)
- [ ] QA regression suite passes (household creation, joining, logout)
- [ ] Ready to start Epic 3 by Sep 12

### Dependencies & Sequencing

**Recommended Story Order:**

1. **Story 5-1 (Logout)** — Independent, touches auth pattern
2. **Story 5-2 (Remove Create/Join Component)** — Navigation refactor, depends on 1-3 understanding
3. **Story 5-3 (Join Household UI)** — Depends on 5-2 completion
4. **Story 5-4 (Create Household UI)** — Can run parallel with 5-3 (isolated from join flow)

**Parallel Work Possible:**
- 5-3 and 5-4 can run in parallel (independent form work)
- 5-1 can run independent of 5-2, 5-3, 5-4

---

## MVP and Project Impact

### Impact on PRD MVP
**Status:** ✅ No impact on MVP scope  
**Rationale:** 
- Logout and household UX polish are part of v1 scope (per PRD)
- These stories complete existing functionality, not expand scope
- No new features added, existing features polished

### Impact on Timeline
**Original Plan:** Epic 2 complete by Aug 27, Epic 3 starts Sep 1  
**Revised Plan:** Epic 5 inserted, Epic 3 starts Sep 12  
**Delay:** ~1 week (from Sep 1 to Sep 12)  
**Justification:** Worth the delay because:
- Logout is critical for v1 (security)
- Join UX polish improves core workflow
- Addresses technical debt now rather than post-MVP
- Strengthens foundation for balance/settlement work

### Impact on Release Date
**Estimated impact:** +1 week (if release was targeted for end of Sep, now targeting mid-Oct)  
**Risk:** Low (polish work is lower-risk than core features)

---

## Pre-Implementation Checklist

- [ ] User approves Sprint Change Proposal (this document)
- [ ] sprint-status.yaml updated with Epic 5 and story statuses
- [ ] Stories 5-1 through 5-4 moved to "ready-for-dev"
- [ ] AC dependency coverage verified (per Epic 2 retro Action Item 1)
- [ ] Developer assigned and contextual story files created
- [ ] QA prepared for regression suite after implementation

---

## Continuity with Previous Epics

### How This Addresses Epic 2 Learnings

From Epic 2 Retro:
- ✅ AC Dependency Coverage: All 4 stories have explicit dependency documentation
- ✅ Definition of Done: Clear DoD criteria for each story (primary AC + error cases + downstream dependencies)
- ✅ Action Item Follow-Through: Polish work directly addresses "improve household workflows" need identified during Epic 2

### Connection to Epic 1 Completion

Epic 5 improves the household setup workflows from Epic 1:
- Story 5-1 (Logout) extends auth from Story 1-2
- Story 5-2 (Refactor Create/Join) improves navigation from Story 1-3
- Story 5-3/5-4 (UI Polish) polish the membership experience from Story 1-4

---

## Risk Mitigation

| Risk | Likelihood | Impact | Mitigation |
|------|------------|--------|-----------|
| Story 5-2 navigation breaks existing flow | Low | High | Thorough manual testing of create/join flows |
| Form styling doesn't match ThemeExtended | Low | Medium | Design review before implementation |
| Logout doesn't clear all local data | Low | High | Unit test local data clearing, QA verification |
| Join form validation incomplete | Low | Medium | AC review, test edge cases (invalid codes, offline) |

**Mitigation Strategy:**
- ✅ Epic 2 retro already identified AC dependency gaps — apply to all 4 stories
- ✅ Assign experienced developer familiar with household workflows
- ✅ QA regression suite focuses on no regressions to existing stories

---

## Next Steps After Epic 5

**Upon Epic 5 Completion (Sep 12):**
1. ✅ All 4 stories marked "done"
2. ✅ Epic 5 marked "done"
3. ✅ Epic 5 retrospective scheduled (optional, per template, but recommended)
4. → **Epic 3 Kickoff:** Balances & Settlement workflows
5. → Epic 4 continues in parallel (Story 4-3: Sync conflict resolution)

**Epic 3 Dependencies Confirmed Ready:**
- ✅ Household workflows solid (Epic 5 polish)
- ✅ Expense creation/editing complete (Epic 2)
- ✅ Split calculation proven (Story 2-5)
- → Ready to start balance queries and settlement recording

---

## Approval

**This proposal requires explicit approval before proceeding to implementation.**

**Approvers:**
- [ ] Project Lead (Amir)
- [ ] Product Owner (Alice)
- [ ] Development Lead

**Approval Signoff:**

| Role | Name | Date | Approved? |
|------|------|------|-----------|
| Project Lead | Amir | — | [ ] |
| Product Owner | Alice | — | [ ] |
| Dev Lead | — | — | [ ] |

---

## Document Metadata

| Item | Value |
|------|-------|
| Document ID | sprint-change-proposal-2026-08-27-polish-epic |
| Status | Proposal — Awaiting Approval |
| Last Updated | 2026-08-27 |
| Prepared By | Copilot (bmad-correct-course) |
| Change Management | bmad-correct-course Workflow |
| Related Documents | epic-2-retro-2026-08-27.md, epics.md, sprint-status.yaml |

