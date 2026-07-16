---
name: testing-architecture
description: in which layer write which test
---

# Test Architecture Guidelines

This document explains how we choose between **unit tests**, **integration tests**, and **end-to-end (E2E) tests** in our Android codebase.

Our goal is to write tests that give **real confidence**, stay **readable**, and remain **stable during refactoring**.

---

## Why this guideline exists

We are moving away from a test strategy that creates high maintenance cost and low confidence.

Problems we want to avoid:

- tightly coupled tests
- heavy use of mocks
- tests that verify implementation details instead of behavior
- tests for trivial delegation
- brittle tests that fail during harmless refactors

Our test strategy should help us:

- catch real regressions
- support safe refactoring
- test user-relevant behavior
- keep tests understandable
- reduce unnecessary mocking

---

## Core principles

### 1. Test behavior, not implementation
Tests should validate observable outcomes:

- returned values
- emitted state
- persisted data
- rendered UI
- visible side effects

Avoid testing internal call chains unless they are the actual behavior being validated.

### 2. Prefer higher-value tests
When multiple test levels are possible, prefer the one that gives the best confidence with reasonable speed and maintenance cost.

In many feature scenarios, **integration tests provide better value than mock-heavy unit tests**.

### 3. Prefer fakes over mocks
Use:

- fake repositories
- fake data sources
- in-memory database
- fake clock
- deterministic fixtures

Avoid excessive interaction-based verification such as:

- verifying a method was called once
- verifying call order across layers
- mocking every dependency in the chain

### 4. Do not test trivial delegation
If a class only forwards a call without adding logic, the test usually has low value.

Example of low-value logic:

- use case calls repository
- returns result unchanged
- no mapping, validation, filtering, retry, fallback, or error translation

In such cases, prefer testing the more meaningful layer instead.

### 5. Keep the pyramid practical
Our goal is not maximum unit-test count.
Our goal is confidence at the right level.

A healthy default mindset for our app:

- unit tests for pure logic
- integration tests for feature behavior
- E2E tests for critical user journeys

---

## Test levels

## Unit tests

### Purpose
Unit tests verify **small, isolated logic** with minimal dependencies.

### Best suited for
Use unit tests for code that contains real logic and can be tested in isolation, such as:

- pure business rules
- validators
- mappers
- formatters/parsers
- reducers/state transformers
- calculations
- filtering/sorting logic
- error mapping
- nontrivial decision-making

### Characteristics
Good unit tests are:

- fast
- deterministic
- focused
- independent of Android framework
- independent of network/database
- low in setup complexity

### Avoid unit tests for
Do not write unit tests for:

- trivial delegation
- simple pass-through use cases
- code with no real branching or transformation
- behavior that only becomes meaningful when multiple components work together

### Good examples
Write a unit test when the code:

- validates user input
- maps API model to domain model
- calculates pricing or limits
- transforms state based on events
- decides which error message to show based on conditions

---

## Integration tests

### Purpose
Integration tests verify that **multiple real components work together correctly**.

This should be our main confidence layer for feature behavior.

### Best suited for
Use integration tests for scenarios like:

- ViewModel + use case + repository
- use case + repository + fake data source
- repository + fake API + in-memory database
- feature-level state transitions
- loading/success/error flows
- caching behavior
- retry behavior
- combining local and remote data
- persistence and retrieval behavior
- coroutine/Flow interactions across components

### Characteristics
Good integration tests:

- test realistic collaboration
- validate outcomes instead of internal interactions
- use fakes instead of mocks where possible
- focus on feature behavior
- remain stable across refactors if behavior stays correct

### Recommended setup
Prefer using:

- fake repositories
- fake API clients
- in-memory database
- test dispatchers
- fake clock
- fixture builders

### Avoid integration tests for
Avoid writing integration tests that:

- duplicate full E2E coverage without extra value
- depend on unstable external services
- are overly broad and difficult to debug
- assert implementation details across layers

### Good examples
Write an integration test when you want to verify:

- ViewModel emits loading then success after fetching data
- retry after failure updates state correctly
- repository returns cached data before remote refresh
- submitting a form updates stored data and exposed state
- a feature flow works correctly across domain and data layers

---

## End-to-End (E2E) tests

### Purpose
E2E tests verify **critical user journeys** from the user perspective.

They confirm that the app is wired correctly across screens, UI, navigation, and major integrations.

### Best suited for
Use E2E tests only for important, business-critical paths such as:

- login
- onboarding
- checkout or purchase
- account creation
- submitting an important form
- startup happy path
- key offline/online recovery flow
- critical navigation path

### Characteristics
Good E2E tests are:

- few in number
- stable
- business-critical
- focused on user-visible behavior
- maintained carefully

### Avoid E2E tests for
Do not use E2E tests for:

- every edge case
- low-level business rules
- every UI variation
- behavior already sufficiently covered in unit or integration tests

### Good examples
Write an E2E test when you need confidence that:

- a user can log in successfully
- a user can complete onboarding
- a user can place an order
- a user can submit a key workflow from start to finish
