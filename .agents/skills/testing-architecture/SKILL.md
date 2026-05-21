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
- independent from Android framework
- independent from network/database
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

---

## How to choose the right test type

Use the following decision guide.

| Situation | Preferred test type | Why |
|---|---|---|
| Pure logic with no framework dependency | Unit | Fast and focused |
| Validation, mapping, calculation, transformation | Unit | Best tested in isolation |
| ViewModel behavior across multiple collaborators | Integration | More realistic than mocking everything |
| Repository behavior with local/remote interaction | Integration | Verifies actual collaboration |
| Feature loading/success/error state flow | Integration | High confidence at feature level |
| Full critical user journey across UI/screens | E2E | Validates real user path |
| Trivial use case that only calls repository | Usually no test | Low value, implementation-coupled |
| Simple delegation between layers | Usually no test | Better covered indirectly elsewhere |

---

## Architecture rules by layer

## Domain layer

### Unit test when
Write unit tests for:

- business rules
- validation
- transformation
- conditional logic
- aggregation logic
- fallback rules
- retry decisions
- sorting/filtering

### Usually do not test when
Usually skip tests when a use case:

- only calls one repository method
- returns unchanged result
- adds no business logic

If needed, test the behavior at the integration level instead.

---

## Data layer

### Unit test when
Use unit tests for isolated components such as:

- mappers
- serializers/deserializers
- request/response conversion
- query-building logic
- parsing logic

### Integration test when
Use integration tests for:

- repository behavior
- local + remote coordination
- cache strategy
- DB/API interaction through fakes or in-memory implementations
- data consistency across components

---

## Presentation layer

### Unit test when
Use unit tests for isolated presentation logic such as:

- state reducers
- formatting for display
- UI-specific mapping
- simple decision logic

### Integration test when
Use integration tests for:

- ViewModel behavior
- state emission over time
- screen-level logic with real use cases and fake repositories
- action/result/state flow
- error/loading/retry behavior

### E2E test when
Use E2E tests for:

- complete screen-to-screen journeys
- critical user actions
- important navigation flows
- smoke coverage for app wiring

---

## What we avoid

We avoid tests that:

- verify that one method only calls another method
- assert exact interaction counts without business value
- mock every dependency in the object graph
- fail because of harmless refactoring
- duplicate behavior already covered at a better level
- test internals instead of outcomes
- give little confidence for real user scenarios

---

## Preferred testing style

### Prefer
- behavior-focused assertions
- readable test names
- Arrange / Act / Assert structure
- fake-based setups
- feature-oriented integration tests
- stable fixtures and builders
- deterministic test data

### Avoid
- excessive mocking
- setup-heavy tests
- asserting private/internal details
- over-specifying interactions
- giant tests covering too many concerns

---

## Simple heuristics

Before writing a test, ask:

### 1. If this breaks in production, would this test fail for the right reason?
If not, the test may be low value.

### 2. Am I testing real behavior or just method calls?
Prefer behavior.

### 3. Is this logic meaningful on its own?
If yes, unit test may fit.

### 4. Does confidence depend on collaboration between components?
If yes, integration test is likely better.

### 5. Is this a critical user journey?
If yes, consider E2E.

### 6. Is this just trivial delegation?
If yes, usually do not test it directly.

---

## Recommended default strategy for our team

As a practical default:

- write **unit tests** for pure and logic-heavy code
- write **integration tests** for feature behavior and component collaboration
- write **E2E tests** only for a small number of critical user journeys
- avoid direct tests for trivial delegation
- prefer **fakes over mocks**
- prefer **behavior assertions over interaction assertions**

---

## Examples of correct test selection

### Example 1: Email validator
- test type: **Unit**
- reason: pure logic, isolated, deterministic

### Example 2: Mapper from API model to domain model
- test type: **Unit**
- reason: transformation logic in isolation

### Example 3: `GetProfileUseCase` only calls `profileRepository.getProfile()`
- test type: **Usually no direct test**
- reason: trivial delegation, low value

### Example 4: ViewModel loads profile and exposes loading/success/error state
- test type: **Integration**
- reason: multiple components collaborating, behavior matters more than interactions

### Example 5: Repository reads cache, then fetches remote, then updates local store
- test type: **Integration**
- reason: coordination across components should be tested together

### Example 6: User logs in and reaches home screen
- test type: **E2E**
- reason: critical user journey

---

## Final rule

Choose the test type that gives the **highest confidence with the lowest unnecessary coupling**.

In general:

- **Unit** for isolated logic
- **Integration** for real feature behavior
- **E2E** for critical user journeys
- **No direct test** for trivial delegation

The purpose of testing is not to maximize test count.
The purpose is to maximize confidence and maintainability.
