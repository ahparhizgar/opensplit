---
title: "Product Brief: OpenSplit"
status: "complete"
created: "2026-04-29T20:04:20Z"
updated: "2026-04-29T20:23:03Z"
inputs:
  - "_bmad-output/brainstorming/brainstorming-session-2026-04-29-225239.md"
---

# Product Brief: OpenSplit

## Executive Summary

OpenSplit is a shared expense tracking app for roommates who need a simple, trustworthy way to record household spending, split costs fairly, and know exactly who owes whom. Today, many roommate groups handle this with chat messages, notes apps, spreadsheets, or mental math. The result is confusion, forgotten expenses, awkward conversations, and messy settlements at the end of the month.

OpenSplit delivers the core Splitwise-style experience as a focused, roommate-first product for web and mobile. The first release centers on smooth core flows: create a household, add expenses, split them equally or unequally, preserve the original currency of each expense, and track settlement clearly. The product goal is not to reinvent the category in v1, but to execute the core experience well enough that roommates can depend on it daily and settle confidently without friction.

## The Problem

Roommates regularly share groceries, utilities, cleaning supplies, internet bills, household subscriptions, and one-off purchases. Tracking these expenses across multiple people is harder than it should be. The friction does not come only from entering expenses; it comes from knowing whether the totals are right, whether the split is fair, and how to settle up without confusion.

Current solutions often break down in predictable ways. People forget to log expenses, lose track of who paid last time, or struggle to handle uneven contributions. By the time a group tries to settle, nobody fully trusts the numbers. That creates social friction on top of financial friction. For roommates, the real pain is not just bookkeeping - it is the awkwardness of resolving money issues when the math feels uncertain.

## The Solution

OpenSplit gives roommates a dedicated place to manage shared expenses from first payment to final settlement. Users can create a shared household, record expenses quickly, assign who paid, choose equal or unequal splits, and see balances update into a clear view of who owes whom. Mobile supports quick capture in the moment, while web provides a larger surface for reviewing balances and settling up.

The v1 experience focuses on reliability and clarity over feature breadth. Each expense is stored in the currency it was created in so the product preserves original financial records from the start, even before richer multi-currency capabilities are introduced later. Settlement tracking lets users record both full and partial settlements and preserve a clear record of what happened. Offline support allows users to enter expenses, edit them, review balances, and record settlements before reconnecting, especially on mobile.

## What Makes This Different

OpenSplit does not aim to differentiate through a novel business model or feature wedge in the first release. Its value comes from delivering a dependable, clean implementation of the expense-sharing workflow that roommates already understand and want.

What matters in v1 is execution: a straightforward experience, cross-platform access on web and mobile, offline-friendly usage, and accurate balance tracking for common roommate scenarios. The strongest initial positioning is not novelty, but trust and clarity for shared living: fewer awkward money conversations, less manual calculation, and more confidence that the numbers are right. The product can explore broader differentiation later, but the first objective is to become a fully working app that handles the core Splitwise use case without friction.

## Who This Serves

OpenSplit primarily serves roommates who live together and routinely share household costs. These users need a low-friction way to track recurring and ad hoc expenses, keep a transparent record of payments, and avoid disputes during settlement.

The ideal early user is part of a small household that shares bills and purchases every week. Success for this user means they no longer need to reconstruct expenses from memory or chat history, and they can settle confidently because the math is visible and trusted. OpenSplit should be especially useful during the recurring monthly cycle of shared bills and end-of-month reconciliation.

## Success Criteria

- Users can create a household and add shared expenses without confusion
- Equal and unequal split flows feel smooth and reliable on both web and mobile
- Users can clearly understand balances and settlement status at any time
- A roommate group can complete the full loop from expense entry to settlement without relying on external tools
- Offline usage supports creating, editing, and settling expenses before reconnecting

## Scope

The first version includes household/group setup, expense entry, payer assignment, equal and unequal splits, balance calculation, original-currency preservation per expense, and settlement tracking across web and mobile surfaces.

The first version explicitly does not include live updates or richer multi-currency features beyond preserving the currency attached to each expense. Advanced differentiation, payment integrations, and other expansion features are also deferred until the core experience is stable and trustworthy.

## Vision

If OpenSplit succeeds, it becomes a dependable shared-finance utility people can use across everyday living situations, starting with roommates and expanding later to adjacent use cases such as trips, couples, and shared households of different kinds. The long-term opportunity is to become the default lightweight coordination layer for shared expenses: clear, trusted, and available wherever people manage money together.
