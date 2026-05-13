---
stepsCompleted:
  - step-01-validate-prerequisites
  - step-03-create-stories
inputDocuments:
  - _bmad-output/planning-artifacts/prd.md
  - _bmad-output/planning-artifacts/architecture.md
  - _bmad-output/planning-artifacts/ux-design-specification.md
---

# bmad-test - Epic Breakdown

## Overview

This document provides the complete epic and story breakdown for bmad-test, decomposing the requirements from the PRD, UX Design if it exists, and Architecture requirements into implementable stories.

## Requirements Inventory

### Functional Requirements

FR1: Users can create an account to access OpenSplit.
FR2: Users can create a household/group for shared expenses.
FR3: Users can join a household/group.
FR4: Users can view the household they belong to.
FR5: Users can switch between households they have access to.
FR6: Users can leave a household they belong to.
FR7: Users can view household members.
FR8: Users can create a shared expense.
FR9: Users can assign themselves or another user as the payer.
FR10: Users can edit an existing expense.
FR11: Users can delete an expense.
FR12: Users can add an expense with equal split.
FR13: Users can add an expense with unequal split.
FR14: Users can assign specific participants to an expense split.
FR15: Users can record an expense in its original currency.
FR16: Users can view expense history.
FR17: Users can view current balances within a household.
FR18: Users can view who owes whom in a household.
FR19: Users can record a full settlement.
FR20: Users can record a partial settlement.
FR21: Users can view settlement history.
FR22: Users can see settlement status for each household member.
FR23: Users can settle an outstanding balance between members.
FR24: Users can create expenses while offline.
FR25: Users can edit expenses while offline.
FR26: Users can record settlements while offline.
FR27: The system can sync offline changes after reconnecting.
FR28: The system can preserve user-entered expense data across reconnects.
FR29: The system can resolve sync conflicts in a way users can understand.
FR30: Users can access OpenSplit on mobile.
FR31: Users can access OpenSplit on web.
FR32: Users can sign in securely.
FR33: Users can keep their household and expense data private by default.
FR34: Users can choose only the information needed for expense tracking.
FR35: The system can prefill common expense entry defaults.
FR36: The system can support a minimal, streamlined expense-entry flow.
FR37: The system can display the most recent balances quickly.

### NonFunctional Requirements

NFR1: Android app startup time on a Galaxy A50 should stay under 0.5 seconds.
NFR2: Core expense entry, balance review, and settlement flows should feel immediate.
NFR3: The app should stay responsive during offline use and sync recovery.
NFR4: User and household data should be privacy-first by default.
NFR5: Only the minimum data needed for expense tracking should be collected.
NFR6: Account access should be securely handled.
NFR7: The app should maintain at least a 98% crash-free session rate.
NFR8: Offline-created, edited, and settled data should survive reconnect and sync correctly.
NFR9: Sync conflicts should be handled predictably so users trust the result.
NFR10: The app should be usable by a broad public audience.
NFR11: The UI should remain clear, minimal, and easy to navigate on mobile and web.
NFR12: Core actions should be reachable without unnecessary complexity.
NFR13: The system should support early growth to at least 1,000 users without degrading the core experience.
NFR14: The first release should prioritize stability and speed over large-scale expansion.

### Additional Requirements

- Kotlin Multiplatform is the shared code strategy across client targets.
- Offline-first behavior is a core v1 requirement, not an enhancement.
- The selected starter template is the Kotlin Multiplatform wizard with Share UI enabled.
- The project should be initialized with `client`, `server`, and `shared` modules.
- REST is the API pattern for client-server communication.
- Email/password authentication and secure session handling are the initial auth approach.
- Authorization is household-scoped and must be enforced consistently.
- Shared DTOs should reduce drift between client and server models.
- The backend is VPS-hosted with Docker Compose.
- Monitoring, logs, and backups require explicit implementation.
- Build and deployment should be reproducible and scripted.

### UX Design Requirements

UX-DR1: Use a themeable Material 3 based design system with a lightweight custom visual layer.
UX-DR2: Define reusable design tokens for color, typography, spacing, and elevation.
UX-DR3: Implement a Household Balance Card component for at-a-glance household balance status.
UX-DR4: Implement an Expense Entry Sheet component with strong defaults, inline validation, and offline states.
UX-DR5: Implement a Settlement Summary component that clearly shows who owes whom and the next action.
UX-DR6: Implement a Sync Status Banner component for synced, pending, reconnecting, and conflict states.
UX-DR7: Maintain strong contrast, large touch targets, and avoid color-only meaning.
UX-DR8: Support mobile-first layouts that scale to tablet and desktop without changing the core flow.
UX-DR9: Support keyboard navigation, screen readers, and WCAG 2.1 AA accessibility.
UX-DR10: Keep forms short, prefilling household, payer, and equal split where possible.
UX-DR11: Make balance feedback immediate after save and keep offline actions recoverable.
UX-DR12: Keep navigation shallow and household-centered across mobile and web.

### FR Coverage Map

FR1: Epic 1 - Secure account creation
FR2: Epic 1 - Household setup
FR3: Epic 1 - Join a household
FR4: Epic 1 - View current household
FR5: Epic 1 - Switch between households
FR6: Epic 1 - Leave a household
FR7: Epic 1 - View household members
FR8: Epic 2 - Create a shared expense
FR9: Epic 2 - Choose the payer
FR10: Epic 2 - Edit an expense
FR11: Epic 2 - Delete an expense
FR12: Epic 2 - Equal split support
FR13: Epic 2 - Unequal split support
FR14: Epic 2 - Participant-specific splits
FR15: Epic 2 - Original currency capture
FR16: Epic 2 - Expense history
FR17: Epic 3 - View current balances
FR18: Epic 3 - See who owes whom
FR19: Epic 3 - Record full settlement
FR20: Epic 3 - Record partial settlement
FR21: Epic 3 - View settlement history
FR22: Epic 3 - See member settlement status
FR23: Epic 3 - Settle outstanding balances
FR24: Epic 4 - Create expenses offline
FR25: Epic 4 - Edit expenses offline
FR26: Epic 4 - Record settlements offline
FR27: Epic 4 - Sync offline changes after reconnect
FR28: Epic 4 - Preserve entered data across reconnects
FR29: Epic 4 - Explain sync conflicts clearly
FR30: Epic 1 - Access on mobile
FR31: Epic 1 - Access on web
FR32: Epic 1 - Secure sign-in
FR33: Epic 1 - Keep data private by default
FR34: Epic 1 - Collect only needed tracking data
FR35: Epic 2 - Prefill common entry defaults
FR36: Epic 2 - Streamlined expense-entry flow
FR37: Epic 3 - Display recent balances quickly

## Epic List

### Epic 1: Join Your Household Securely

Users can create an account, sign in securely, access OpenSplit on mobile and web, create or join a household, and see household membership in a privacy-first way.

**FRs covered:** FR1, FR2, FR3, FR4, FR5, FR6, FR7, FR30, FR31, FR32, FR33, FR34

**UX-DRs addressed:** UX-DR8, UX-DR9, UX-DR12

### Epic 2: Log Shared Expenses Fast

Users can add, edit, delete, and review shared expenses with smart defaults, payer selection, equal and unequal splits, participant-specific splits, and original-currency tracking.

**FRs covered:** FR8, FR9, FR10, FR11, FR12, FR13, FR14, FR15, FR16, FR35, FR36

**UX-DRs addressed:** UX-DR1, UX-DR2, UX-DR4, UX-DR10, UX-DR11, UX-DR12

### Epic 3: Understand Balances and Settle Up

Users can quickly see balances, understand who owes whom, record full or partial settlements, review settlement history, and confirm each member's settlement status.

**FRs covered:** FR17, FR18, FR19, FR20, FR21, FR22, FR23, FR37

**UX-DRs addressed:** UX-DR3, UX-DR5, UX-DR11, UX-DR12

### Epic 4: Trust Offline Changes and Sync Recovery

Users can create expenses and settlements while offline, preserve their work locally, sync after reconnecting, and understand any conflict resolution without losing trust.

**FRs covered:** FR24, FR25, FR26, FR27, FR28, FR29

**UX-DRs addressed:** UX-DR6, UX-DR11

**Dependencies:** Epic 1 establishes access and household context; Epic 2 and Epic 3 can function with online storage, while Epic 4 hardens the offline-first promise across both.

## Epic 1: Join Your Household Securely

Users can create an account, sign in securely, access OpenSplit on mobile and web, create or join a household, and see household membership in a privacy-first way.

**FRs covered:** FR1, FR2, FR3, FR4, FR5, FR6, FR7, FR30, FR31, FR32, FR33, FR34

**Relevant NFRs:** NFR4, NFR5, NFR6, NFR10, NFR11, NFR12

**UX-DRs addressed:** UX-DR8, UX-DR9, UX-DR12

### Story 1.1: Initialize the Kotlin Multiplatform starter project

As a developer,
I want the project initialized from the selected Kotlin Multiplatform wizard starter,
So that the app has the agreed shared client/server structure before feature work begins.

**Acceptance Criteria:**

**Given** the repository is newly initialized
**When** the starter project is created with Share UI enabled
**Then** the `client`, `server`, and `shared` modules exist

**Given** the starter project is in place
**When** the initial app structure is reviewed
**Then** the shared Kotlin foundation is available for subsequent stories

**Given** the selected architecture requires shared UI and shared logic
**When** the project skeleton is created
**Then** the structure supports those constraints without requiring later rework

### Story 1.2: Create and sign in securely

As a new or returning user,
I want to create an account and sign in securely,
So that I can access my households and expenses on mobile or web.

**Acceptance Criteria:**

**Given** a new user is on the sign-up screen
**When** they submit a valid email and password
**Then** the account is created and the user is signed in
**And** only the minimum access information is required

**Given** a returning user is on the sign-in screen
**When** they submit valid credentials
**Then** the user is authenticated and routed to their household context

**Given** the user enters invalid credentials or incomplete data
**When** they submit the form
**Then** inline validation or an error message is shown
**And** no session is created

### Story 1.3: Create or join a household

As a signed-in user,
I want to create a household or join an existing one,
So that I can start sharing expenses with the right group.

**Acceptance Criteria:**

**Given** a signed-in user starts household setup
**When** they enter a valid household name and confirm creation
**Then** a household is created and set as the active context

**Given** a user has a valid join code or invite token
**When** they submit it
**Then** they are added to the household
**And** the household becomes available in their household list

**Given** the setup form is incomplete or invalid
**When** the user tries to continue
**Then** the app prevents submission and shows a clear validation message

### Story 1.4: View and manage household membership

As a household member,
I want to see members, switch households, and leave a household,
So that I can manage my shared-expense context without confusion.

**Acceptance Criteria:**

**Given** the user is viewing a household
**When** the member list loads
**Then** the current household members are visible

**Given** the user has access to multiple households
**When** they choose another household
**Then** the active context switches to that household

**Given** the user chooses to leave a household
**When** they confirm the action
**Then** they are removed from that household and returned to a valid household or safe landing state

### Story 1.5: Use OpenSplit on mobile and web with clear household context

As a signed-in user,
I want OpenSplit to work well on mobile and web,
So that I can access the same household context quickly on either surface.

**Acceptance Criteria:**

**Given** the app is opened on a mobile device or in a web browser
**When** the main screen loads
**Then** the layout adapts to the available screen size

**Given** the user is signed in
**When** the app renders navigation
**Then** the current household context is visible and the core actions are easy to reach

**Given** a user is not signed in
**When** they open the app
**Then** protected household data is not shown

### Story 1.6: Establish local development and CI baseline

As a developer,
I want a reproducible local development and CI baseline,
So that the greenfield project can be built, tested, and run consistently from the start.

**Acceptance Criteria:**

**Given** a fresh checkout of the repository
**When** I follow the documented setup steps
**Then** I can run the client and server locally without manual guesswork

**Given** the baseline CI workflow is configured
**When** the pipeline runs
**Then** shared, client, and server checks execute in a reproducible way

**Given** the project has not yet been modified by feature work
**When** the setup validation runs
**Then** the starter project builds successfully and the baseline test suite passes

## Epic 2: Log Shared Expenses Fast

Users can add, edit, delete, and review shared expenses with smart defaults, payer selection, equal and unequal splits, participant-specific splits, and original-currency tracking.

**FRs covered:** FR8, FR9, FR10, FR11, FR12, FR13, FR14, FR15, FR16, FR35, FR36

**Relevant NFRs:** NFR2, NFR3, NFR10, NFR11, NFR12

**UX-DRs addressed:** UX-DR1, UX-DR2, UX-DR4, UX-DR10, UX-DR11, UX-DR12

### Story 2.1: Create an expense with smart defaults

As a household member,
I want to add an expense with the right defaults already filled in,
So that I can log shared costs in a few taps.

**Acceptance Criteria:**

**Given** the user opens the expense entry flow
**When** the form loads
**Then** the current household, current user as payer, and equal split are prefilled when appropriate

**Given** the user enters required expense details
**When** they save the expense
**Then** the expense is created and appears in the household history

**Given** required data is missing or invalid
**When** the user attempts to save
**Then** inline validation explains what must be fixed

### Story 2.2: Split an expense equally or unequally

As a household member,
I want to split an expense equally or with custom shares,
So that the debt matches the real arrangement.

**Acceptance Criteria:**

**Given** the user selects equal split
**When** the expense is saved
**Then** the amount is divided evenly among the selected participants

**Given** the user selects an unequal split
**When** they enter custom participant shares
**Then** the app validates that the shares total the full expense amount

**Given** the selected participants and split values do not reconcile
**When** the user tries to save
**Then** the save is blocked with a clear error message

### Story 2.3: Edit or delete an expense and keep its original currency

As a household member,
I want to edit or delete an existing expense without losing its original currency,
So that the record stays accurate and trustworthy.

**Acceptance Criteria:**

**Given** an expense exists
**When** the user edits its details and saves
**Then** the changes are persisted and balances are recalculated

**Given** an expense exists
**When** the user deletes it and confirms
**Then** the expense is removed from the household history and balances update

**Given** an expense is stored in a currency
**When** the user views or edits it later
**Then** the original currency remains visible and unchanged

### Story 2.4: Review expense history

As a household member,
I want to review expense history,
So that I can verify what was logged and how the household total evolved.

**Acceptance Criteria:**

**Given** the household has expense records
**When** the history view loads
**Then** expenses are shown in a clear, chronological list

**Given** the user selects an expense from history
**When** the detail view opens
**Then** the expense shows its amount, payer, split, and participants

**Given** the household has no expenses
**When** the history view opens
**Then** an empty state explains the next action

## Epic 3: Understand Balances and Settle Up

Users can quickly see balances, understand who owes whom, record full or partial settlements, review settlement history, and confirm each member's settlement status.

**FRs covered:** FR17, FR18, FR19, FR20, FR21, FR22, FR23, FR37

**Relevant NFRs:** NFR2, NFR7, NFR10, NFR11, NFR12

**UX-DRs addressed:** UX-DR3, UX-DR5, UX-DR11, UX-DR12

### Story 3.1: View current balances and owed relationships

As a household member,
I want to see current balances and who owes whom,
So that I can understand the household state at a glance.

**Acceptance Criteria:**

**Given** the user opens the balance view
**When** data loads
**Then** the current household balances are visible quickly

**Given** the household has outstanding debts
**When** the balance view renders
**Then** it shows who owes whom in clear language

**Given** the household has no outstanding balance
**When** the balance view loads
**Then** the screen clearly indicates that the household is settled

### Story 3.2: Record a full or partial settlement

As a household member,
I want to record a full or partial settlement,
So that the household debt stays accurate after payment.

**Acceptance Criteria:**

**Given** the user is viewing an outstanding balance
**When** they choose a full settlement and confirm
**Then** the debt is cleared and balances update

**Given** the user is viewing an outstanding balance
**When** they enter a valid partial settlement amount and confirm
**Then** the remaining balance is reduced correctly

**Given** the settlement amount is invalid
**When** the user tries to save
**Then** the app blocks the action and explains the issue

### Story 3.3: Review settlement history and member status

As a household member,
I want to see settlement history and each member's status,
So that I can trust the repayment record.

**Acceptance Criteria:**

**Given** the household has settlement activity
**When** the settlement history view loads
**Then** the user can see prior settlements in reverse chronological order

**Given** the user opens a household member record
**When** the status view renders
**Then** the current settlement status for that member is visible

**Given** no settlements exist yet
**When** the history view opens
**Then** a clear empty state is shown

## Epic 4: Trust Offline Changes and Sync Recovery

Users can create expenses and settlements while offline, preserve their work locally, sync after reconnecting, and understand any conflict resolution without losing trust.

**FRs covered:** FR24, FR25, FR26, FR27, FR28, FR29

**Relevant NFRs:** NFR3, NFR7, NFR8, NFR9

**UX-DRs addressed:** UX-DR6, UX-DR11

### Story 4.1: Save expenses and settlements while offline

As a household member,
I want to create and edit expenses and record settlements while offline,
So that I can capture shared costs immediately without waiting for a connection.

**Acceptance Criteria:**

**Given** the device is offline
**When** the user creates or edits an expense
**Then** the data is stored locally

**Given** the device is offline
**When** the user records a settlement
**Then** the settlement is stored locally

**Given** an offline action is saved
**When** the user returns to the app
**Then** the pending state remains visible until sync completes

### Story 4.2: Sync offline changes after reconnecting

As a household member,
I want offline changes to sync automatically after reconnecting,
So that my data stays current without re-entering it.

**Acceptance Criteria:**

**Given** there are pending offline changes
**When** connectivity returns
**Then** the app starts syncing those changes automatically

**Given** a sync completes successfully
**When** the app refreshes its local view
**Then** the final server state is reflected and pending indicators clear

**Given** sync is in progress
**When** the user continues using the app
**Then** the app remains responsive and does not block unrelated actions

### Story 4.3: Resolve sync conflicts clearly

As a household member,
I want sync conflicts explained in plain language,
So that I can trust the final result and understand what changed.

**Acceptance Criteria:**

**Given** a sync conflict occurs
**When** the app detects it
**Then** the user sees an understandable conflict message or resolution view

**Given** a conflict is resolved
**When** the final state is saved
**Then** user-entered data is preserved where possible and the final outcome is visible

**Given** the app has pending, reconnecting, or conflict sync states
**When** the sync status is shown
**Then** the user sees a clear non-blocking status indicator
