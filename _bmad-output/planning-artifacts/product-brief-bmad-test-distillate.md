---
title: "Product Brief Distillate: OpenSplit"
type: llm-distillate
source: "product-brief-bmad-test.md"
created: "2026-04-29T20:25:34Z"
purpose: "Token-efficient context for downstream PRD creation"
---

# Product Brief Distillate: OpenSplit

## Product Shape

- Primary launch audience is roommates, not general shared-expense groups.
- Product intent is execution quality, not early differentiation.
- v1 should be a fully working app with smooth core flows.

## Core Problem

- Main user pain: roommates struggle to track shared expenses, total them correctly, and calculate who should pay whom and how much.
- Emotional pain is messy settlement and low trust in the final numbers.
- Existing alternatives implicitly include chat, notes, spreadsheets, and mental math; these create confusion and awkwardness.

## MVP Scope Signals

- In scope for v1: household/group setup, expense entry, payer assignment, equal splits, unequal splits, balance calculation, original-currency preservation per expense, settlement tracking, web support, mobile support, offline support.
- Unequal split support is explicitly required.
- Settlement tracking is explicitly required.
- Partial settlement is allowed.
- Live updates are explicitly out of scope for v1.
- Rich multi-currency behavior is out of scope for v1.

## Currency Rules

- Full multi-currency support was initially considered for v1, then removed.
- Each expense should still be saved in an arbitrary/original currency from day one.
- Preserving original currency is intentional groundwork for later multi-currency support.
- PRD should avoid assuming FX conversion, exchange-rate sourcing, or cross-currency settlement logic in v1 unless reintroduced deliberately.

## Offline Expectations

- Offline support is important, not optional nice-to-have.
- Offline support should include creating expenses before reconnecting.
- Offline support should include editing expenses before reconnecting.
- Offline support should include recording settlements before reconnecting.
- PRD should define reconnect/sync behavior and conflict handling because brief-level guidance does not specify it.

## Platform Context

- Target platforms are web + mobile.
- Product should feel cross-platform, but the brief does not lock implementation approach.
- Earlier brainstorming artifact mentioned Kotlin-heavy implementation preferences: Spring Boot with Kotlin backend and Compose Multiplatform clients.
- Those technical directions are context, not yet hard product requirements.

## Positioning Signals

- User does not want strong differentiation in v1.
- Recommended positioning is roommate-first clarity, trust, and fewer awkward money conversations.
- Product should be framed as a dependable utility rather than a novel financial product.

## Success Signals

- Success for v1 means a fully working app.
- Smooth core flows matter more than advanced features.
- A roommate group should be able to complete the full loop from expense entry to settlement without external tools.

## Rejected Or Deferred Ideas

- Live updates: explicitly out of scope for v1.
- Advanced differentiation: deferred.
- Rich multi-currency capability: deferred, while preserving original-currency records now.
- Real-time collaborative behavior should not be assumed in the PRD.

## User Scenario Notes

- Primary recurring scenario is roommates sharing groceries, utilities, subscriptions, and one-off household purchases.
- Important trust moment is end-of-month or ad hoc settlement when users need confidence in the totals.
- Product should reduce both math friction and social friction.

## Open Questions For PRD

- What exact types of unequal splits are required in v1: fixed amounts, percentages, shares, custom participant inclusion, or a subset?
- How should offline sync conflicts be resolved when multiple devices edit related expense data?
- What settlement history and audit trail should users see?
- What balance model should be used when expenses are stored in arbitrary currencies but rich conversion is not part of v1?
- Should mobile and web launch simultaneously, or can one lead and the other follow?
