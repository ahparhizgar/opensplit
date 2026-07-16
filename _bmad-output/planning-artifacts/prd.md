---
stepsCompleted:
  - step-01-init
  - step-02-discovery
  - step-02c-executive-summary
  - step-03-success
  - step-04-journeys
  - step-05-domain
  - step-06-innovation
  - step-07-project-type
  - step-08-scoping
  - step-09-functional
  - step-10-nonfunctional
  - step-11-polish
inputDocuments:
  - _bmad-output/planning-artifacts/product-brief-bmad-test.md
  - _bmad-output/planning-artifacts/product-brief-bmad-test-distillate.md
documentCounts:
  briefCount: 2
  researchCount: 0
  brainstormingCount: 0
  projectDocsCount: 0
classification:
  projectType: mobile_app
  domain: general
  complexity: medium
  projectContext: greenfield
workflowType: prd
---

# Product Requirements Document - bmad-test

**Author:** Amir
**Date:** 2026-04-30

## Executive Summary

OpenSplit is a greenfield mobile-first shared-expense product for roommates who need a fast, simple, trustworthy way to log shared costs and understand debts without friction. The product solves the recurring pain of tracking groceries, utilities, subscriptions, and one-off household purchases across multiple people, where the real problem is not just bookkeeping but confidence: users need to know the numbers are right and settlement is clear.

The core experience is optimized for speed and simplicity. Users should be able to create an account quickly, add an expense in seconds, assign payers and splits, and immediately see how balances change. Mobile is the primary surface; web support is also important for fast access to balances and expense entry. The product is intentionally minimal in UI and workflow so it stays faster than chat threads, spreadsheets, or memory-based tracking.

Offline support is part of the v1 product promise, especially on mobile, so users can create, edit, and settle expenses even before reconnecting. The product preserves the original currency of each expense from day one as groundwork for later expansion, but rich multi-currency behavior, live updates, guest/no-account web access, AI-assisted entry, and widget-based capture are future possibilities rather than v1 commitments.

### What Makes This Special

OpenSplit wins by removing friction at the two moments that matter most: adding an expense and understanding who owes what. Its differentiator is not novel finance logic; it is execution speed, clarity, and trust. The product is valuable because it is faster than the workarounds roommates already use, and because it makes settlement feel obvious instead of awkward.

## Project Classification

- **Project Type:** `mobile_app`
- **Domain:** `general`
- **Complexity:** `medium`
- **Project Context:** `greenfield`

## Success Criteria

### User Success

OpenSplit succeeds when users can create an account, add a shared expense, and understand who owes what with the fewest possible clicks and the least possible time. The core experience should feel fast enough to use in the moment, without forcing users to switch to chat, notes, or spreadsheets.

### Business Success

OpenSplit is successful if it reaches 1,000 users. Revenue is not a primary success measure at this stage; early adoption and product usefulness matter more than monetization.

### Technical Success

The app should deliver a 98% crash-free session rate. Android startup time on a Galaxy A50 should stay under 0.5 seconds. Core flows must remain reliable on mobile and web, with offline support working for expense creation, editing, and settlement.

### Measurable Outcomes

- Users can complete expense entry with minimal taps/clicks
- Users can finish the core expense-to-settlement loop without external tools
- The app feels instant enough to stay usable in real roommate situations
- Launch success is defined by 1,000 users, not revenue
- Android startup time stays under 0.5 seconds on Galaxy A50
- Crash-free sessions stay at or above 98%

## Product Scope

### Core Release

- Quick household creation
- Fast expense entry
- Equal and unequal splits
- Balance calculation
- Settlement tracking
- Mobile-first experience
- Web access
- Offline support
- Minimal UI

### Later Opportunities

- AI-assisted expense entry
- Widget-based quick capture
- Guest or no-account web access
- Broader speed optimizations and shortcuts

### Vision (Future)

OpenSplit becomes the fastest way for roommates and adjacent shared-living groups to record expenses, understand debts, and settle up with almost no friction.

## User Journeys

### Primary User - Success Path

Maya is a roommate who just paid for groceries after work. She opens OpenSplit on her phone while standing in the kitchen and wants the app to get out of the way. The expense form comes prefilled with the current household, the current user as payer, and an equal split by default, so she only changes what is necessary and saves in a few taps. The app updates balances immediately so she can see who owes what without mental math or old chat threads.

The critical moment is the save. If it feels instant and obvious, Maya trusts the app and keeps using it. If it feels slow or cluttered, she falls back to notes or chat. The journey succeeds when expense entry is faster than her current workaround and the balance result is clear enough to settle later without confusion.

### Primary User - Edge Case

Maya is trying to add a shared utility bill, but her phone is offline. She still needs to record the expense now so she does not forget it later. OpenSplit lets her create or edit the expense offline, then syncs it when connectivity returns. If there is a conflict, the app should make the outcome understandable and preserve trust in the recorded data.

The critical moment is recovery after reconnect. If the app handles sync cleanly, Maya continues using it without thinking about the technical layer. If data appears missing or altered, trust breaks immediately. This journey requires offline creation, edit, settlement recording, sync, and conflict handling.

### Household Admin / Setup User

Maya creates a new roommate household when the group first starts using OpenSplit. She needs a fast setup path with minimal friction so the group can begin logging expenses right away. She names the household, invites roommates, and confirms the group is ready. The setup flow should feel lightweight, not like onboarding for a complex finance tool.

The critical moment is first successful household creation. If that is quick, the rest of the product becomes usable immediately. If setup feels heavy, the group never gets to the core value. This journey reveals requirements for account creation, household setup, and fast invitation or joining flow.

### Web Access / Quick Check Journey

Later, Maya is on her laptop and wants to check balances quickly without hunting through messages. She opens the web app and expects immediate access to current balances and recent expenses. The experience should be minimal and fast, so the web surface feels like a direct utility rather than a separate product.

The critical moment is fast access to the current state. If she can see balances right away, web becomes a useful companion surface. If login or navigation gets in the way, she will not return to it often. This journey reveals requirements for lightweight web access, balance visibility, and fast navigation.

### Journey Requirements Summary

- Default-first expense entry
- Fast account and household creation
- Minimal-tap expense entry
- Equal and unequal split support
- Immediate balance calculation
- Clear settlement status
- Offline create/edit/settlement support
- Sync and conflict handling after reconnect
- Fast web access to balances and expense history
- Minimal UI optimized for speed

## Domain-Specific Requirements

### Technical Constraints
- Privacy-first data handling
- Secure authentication and session management
- Only collect what is needed for core expense tracking
- Reliable offline storage and sync
- Fast performance on mobile, especially lower-end Android devices

### Risk Mitigations
- Avoid unnecessary personal data collection
- Keep expense and household data protected by default
- Make sync behavior predictable so users trust their data
- Prevent slow or heavy UI flows from hurting the core experience

## Mobile App Specific Requirements

### Project-Type Overview

OpenSplit is a mobile-first app with shared code across platforms using Kotlin Multiplatform. The mobile experience is the primary product surface, and it must feel fast, minimal, and optimized for low-friction expense entry and balance checking.

### Technical Architecture Considerations

- Native apps with shared business logic via Kotlin Multiplatform
- Offline support is highly important and part of the core v1 promise
- Mobile startup and interaction speed are priority requirements
- UI should stay minimal and optimized for quick entry flows
- Cross-platform consistency should come from shared logic, not a heavy UI abstraction

### Implementation Considerations

- No push notifications in v1
- No special device features required for v1
- Widget support is not part of v1
- App Store / Play Store compliance should follow standard requirements
- Offline-first behavior should be treated as a core product requirement, not an enhancement

## Project Scoping

### Strategy & Philosophy

**Approach:** single release
**Resource Requirements:** lean cross-functional team with Kotlin Multiplatform, mobile, and backend skills

### Complete Feature Set

**Core User Journeys Supported:**
- Fast expense entry with smart defaults
- Offline expense creation, editing, and settlement
- Household setup and quick access
- Balance review on mobile and web
- Settlement tracking with clear debt visibility

**Must-Have Capabilities:**
- Native mobile apps with shared Kotlin code
- Fast account and household creation
- Default-first expense entry
- Equal and unequal splits
- Immediate balance calculation
- Settlement tracking
- Offline-first behavior
- Web access
- Privacy-first data handling
- Minimal UI
- 98% crash-free sessions
- Sub-0.5s Android startup on Galaxy A50

**Nice-to-Have Capabilities:**
- AI-assisted expense entry
- Widget-based quick capture
- Guest/no-account web access
- Push notifications
- Broader speed shortcuts and optimizations

### Risk Mitigation Strategy

**Technical Risks:**
Keep the first release focused on speed, offline reliability, and trustable sync. Avoid adding features that slow the core flow.

**Market Risks:**
Validate that users actually adopt the product by focusing on the fastest possible expense-entry loop and clear balances.

**Resource Risks:**
If resources tighten, preserve the core loop: create household, add expense, calculate debt, settle. Everything else is secondary.

## Functional Requirements

### Account and Household Management

- FR1: Users can create an account to access OpenSplit.
- FR2: Users can create a household/group for shared expenses.
- FR3: Users can join a household/group.
- FR4: Users can view the household they belong to.
- FR5: Users can switch between households they have access to.
- FR6: Users can leave a household they belong to.
- FR7: Users can view household members.

### Expense Management

- FR8: Users can create a shared expense.
- FR9: Users can assign themselves or another user as the payer.
- FR10: Users can edit an existing expense.
- FR11: Users can delete an expense.
- FR12: Users can add an expense with equal split.
- FR13: Users can add an expense with unequal split.
- FR14: Users can assign specific participants to an expense split.
- FR15: Users can record an expense in its original currency.
- FR16: Users can view expense history.

### Balances and Settlement

- FR17: Users can view current balances within a household.
- FR18: Users can view who owes whom in a household.
- FR19: Users can record a full settlement.
- FR20: Users can record a partial settlement.
- FR21: Users can view settlement history.
- FR22: Users can see settlement status for each household member.
- FR23: Users can settle an outstanding balance between members.

### Offline and Sync

- FR24: Users can create expenses while offline.
- FR25: Users can edit expenses while offline.
- FR26: Users can record settlements while offline.
- FR27: The system can sync offline changes after reconnecting.
- FR28: The system can preserve user-entered expense data across reconnects.
- FR29: The system can resolve sync conflicts in a way users can understand.

### Access and Privacy

- FR30: Users can access OpenSplit on mobile.
- FR31: Users can access OpenSplit on web.
- FR32: Users can sign in securely.
- FR33: Users can keep their household and expense data private by default.
- FR34: Users can choose only the information needed for expense tracking.

### Product Experience

- FR35: The system can prefill common expense entry defaults.
- FR36: The system can support a minimal, streamlined expense-entry flow.
- FR37: The system can display the most recent balances quickly.

## Non-Functional Requirements

### Performance

- Android app startup time on a Galaxy A50 should stay under 0.5 seconds.
- Core expense entry, balance review, and settlement flows should feel immediate.
- The app should stay responsive during offline use and sync recovery.

### Security

- User and household data should be privacy-first by default.
- Only the minimum data needed for expense tracking should be collected.
- Account access should be securely handled.

### Reliability

- The app should maintain at least a 98% crash-free session rate.
- Offline-created, edited, and settled data should survive reconnect and sync correctly.
- Sync conflicts should be handled predictably so users trust the result.

### Accessibility

- The app should be usable by a broad public audience.
- The UI should remain clear, minimal, and easy to navigate on mobile and web.
- Core actions should be reachable without unnecessary complexity.

### Scalability

- The system should support early growth to at least 1,000 users without degrading the core experience.
- The first release should prioritize stability and speed over large-scale expansion.
