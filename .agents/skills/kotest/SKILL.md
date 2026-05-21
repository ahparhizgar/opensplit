---
name: kotest
description: How to write test using kotest
---

# Kotest Guide

This guide explains how tests should be written in this project.

It is intended for agents and contributors who add or update tests in our Android codebase.

Our project uses:

- **Kotest**
- primarily **BehaviorSpec**
- uppercase **Given / When / Then**
- a **custom `When` variant**
- a custom **`testValue`** utility for mutable test state
- test helpers such as **`createComponentContext`**
- component-aware test scopes such as **`testComponent`**

The goal is to write tests that are:

- behavior-focused
- readable
- stable during refactoring
- aligned with our real architecture
- low in mocking
- high in confidence

---

## Core testing philosophy

### Test behavior, not implementation
Prefer assertions about:

- returned values
- visible state
- emitted state
- UI-relevant behavior
- persisted data
- lifecycle-driven effects

Avoid tests that mainly verify:

- one method called another method
- exact interaction count without business value
- internal call chains
- implementation details that can change safely during refactoring

### Prefer realistic tests
When possible, use:

- real objects
- fake repositories
- real state transitions
- real component setup
- project test utilities

Do not overuse mocks when a fake or real collaboration gives stronger confidence.

### Do not test trivial delegation
If a class only forwards a call and adds no meaningful logic, avoid writing a direct test for it.

---

## Test framework conventions

## Required style

### Use Kotest
Tests in this project should use **Kotest**.

### Prefer `BehaviorSpec`
Default test style is **`BehaviorSpec`** unless there is a strong reason to use another style.

### Use uppercase BDD blocks
Use:

- `Given`
- `When`
- `Then`

Do not replace them with lowercase alternatives when writing tests for this project.

### Use the project’s custom `When`
This project includes a custom version of `When`.
Use the project’s existing `When` style and conventions instead of inventing a different pattern.

### Use `testValue` for mutable scenario state
If a test needs mutable value state across nested `Given / When / Then` scopes, use the project’s `testValue` helper.

This is preferred over ad hoc mutable state patterns.

---

## Preferred structure

A typical test should read like behavior documentation.

Example style:

```kotlin name=sample-behavior-spec.kt
class SampleTest : BehaviorSpec({
    Given("some initial state") {
        var count by testValue { 1 }

        When("incrementing count") {
            beforeEach {
                count += 1
            }

            Then("should update the value") {
                count shouldBe 2
            }
        }
    }
})
```

### Structure rules
- `Given` describes the initial context
- `When` describes the action or event
- `Then` describes the expected observable result
- keep descriptions behavior-focused and human-readable
- prefer one clear behavior per branch
- nested `When` blocks are acceptable when they represent meaningful scenario progression

---

## Mutable test state rules

### Use `testValue`
When state must be shared safely across nested test scopes, use:

```kotlin name=test-value-example.kt
var myVar by testValue { 1 }
```

Use this instead of:

- plain mutable vars with fragile lifecycle assumptions
- scattered setup across nested scopes
- manual reset logic when `testValue` is the intended project pattern

### Mutate state in setup blocks
If a scenario requires changing state before assertions, do it in setup blocks such as `beforeEach` or the custom `When` action block.

Example:

```kotlin name=test-value-mutation.kt
When("set it to 2", { myVar = 2 }) {
    Then("should be 2") {
        myVar shouldBe 2
    }
}
```

or:

```kotlin name=before-each-mutation.kt
When("adding one") {
    beforeEach {
        myVar += 1
    }

    Then("should be 2") {
        myVar shouldBe 2
    }
}
```

### Mutations must reflect real behavior
Only use mutable scenario state when it makes the scenario easier to understand.
Do not create unnecessary mutable state if a simpler arrangement is possible.

---

## Component testing rules

This project contains component-oriented utilities.
When testing components, prefer project helpers over custom setup.

## Use `createComponentContext`
If a test needs a `ComponentContext`, use the project utility:

```kotlin name=create-component-context-example.kt
val lifecycleRegistry = LifecycleRegistry()
val context = createComponentContext(lifecycle = lifecycleRegistry)
```

Do not duplicate `DefaultComponentContext` setup manually unless there is a specific reason.

## Prefer `testComponent` when appropriate
If the test belongs naturally in the component test scope pattern, use the provided `testComponent` helper.

This helper already integrates:

- `LifecycleRegistry`
- `ComponentContext`
- `TestScope`
- `runTest`

This is the preferred approach when writing tests for components that benefit from a dedicated component-aware coroutine scope.

Example shape:

```kotlin name=test-component-example.kt
testComponent("does something") {
    // use lifecycleRegistry
    // use context
    // run assertions in the provided test scope
}
```

## Reuse lifecycle-aware setup
For component tests, lifecycle should be explicit when relevant.

Typical setup:

```kotlin name=component-setup-example.kt
val lifecycleRegistry = LifecycleRegistry()
val context = createComponentContext(lifecycle = lifecycleRegistry)
```

Use this especially when behavior depends on component lifecycle.

---

## Repository and dependency setup

### Prefer fake implementations
When testing feature behavior, prefer:

- `FakeTaskRepository`
- other fake repositories in the project
- in-memory or deterministic collaborators

Avoid mock-heavy setup when a fake can model the behavior more clearly.

Example:

```kotlin name=fake-repository-example.kt
val repo = FakeTaskRepository()
```

### Use real collaborating objects when practical
For integration-style tests, prefer wiring the real object graph for the feature slice being tested.

Example style:

```kotlin name=real-component-setup.kt
val lifecycleRegistry = LifecycleRegistry()
val repo = FakeTaskRepository()
val context = createComponentContext(lifecycle = lifecycleRegistry)

val component = DefaultTaskListComponent(
    componentContext = context,
    repo = repo,
    taskItemFactory = DefaultTaskItemComponent.Factory(taskRepository = repo),
    onEditRequested = { },
)
```

This is preferred over mocking every dependency in the chain.

---

## Test type selection

## Unit tests
Use unit tests for isolated logic such as:

- validators
- mappers
- formatting/parsing
- reducers
- calculations
- small business rules
- transformation logic

Characteristics:

- no Android/framework dependency unless truly necessary
- minimal setup
- no unnecessary mocks
- fast and deterministic

## Integration tests
Use integration tests for collaboration between real project components, especially:

- component + repository
- ViewModel/component + use cases + fake repository
- feature state transitions
- lifecycle-aware behavior
- repository + fake/in-memory dependencies

This is often the preferred level in this codebase.

## E2E tests
Use E2E tests only for critical user journeys and broad application wiring confidence.

Do not move every scenario to E2E.

---

## Writing BehaviorSpec tests in this project

## Naming conventions

### `Given`
Describe the initial context or object under test.

Good examples:
- `Given("a TaskListComponent with real operations")`
- `Given("an empty repository")`
- `Given("a user with existing tasks")`

Avoid vague descriptions like:
- `Given("test")`
- `Given("setup")`

### `When`
Describe the action, trigger, or event.

Good examples:
- `When("adding a task")`
- `When("loading tasks")`
- `When("the lifecycle is resumed")`

Avoid implementation-centric wording like:
- `When("calling load()")`

Prefer behavior wording unless the API call itself is the behavior being documented.

### `Then`
Describe the expected observable outcome.

Good examples:
- `Then("should expose the new task in state")`
- `Then("should show loading then content")`
- `Then("should keep previously cached items")`

Avoid:
- `Then("repository should be called once")`
  unless that interaction is itself the actual contract being tested.

---

## Assertions

### Assert outcomes
Prefer assertions on:

- state
- output
- emitted items
- resulting collection contents
- side effects visible to consumers

Examples:
- task list contains the created task
- loading state is emitted before success
- selected item is updated
- callback result changes as expected

### Avoid over-asserting internals
Do not assert:

- every intermediate call
- exact private implementation behavior
- wiring details already implied by outcome

---

## Coroutine and dispatcher usage

If the test requires main dispatcher replacement, use the project’s existing extension setup.

Example style already used in project:

```kotlin name=dispatcher-extension-example.kt
extensions(MainDispatcherExtension())
```

Do not invent a new dispatcher pattern if a project standard already exists.

If a helper such as `testComponent` already wraps `runTest`, do not wrap it again unnecessarily.

---

## Preferred setup patterns

## Pattern 1: Simple BehaviorSpec with `testValue`

```kotlin name=simple-test-value-pattern.kt
class CounterTest : BehaviorSpec({
    Given("an initial counter") {
        var counter by testValue { 0 }

        When("incrementing it") {
            beforeEach {
                counter += 1
            }

            Then("should increase the counter") {
                counter shouldBe 1
            }
        }
    }
})
```

## Pattern 2: Component setup with real collaborators

```kotlin name=component-real-collaborators-pattern.kt
class TaskListComponentTest : BehaviorSpec({
    extensions(MainDispatcherExtension())

    Given("A TaskListComponent with real operations") {
        val lifecycleRegistry = LifecycleRegistry()
        val repo = FakeTaskRepository()
        val context = createComponentContext(lifecycle = lifecycleRegistry)

        val component = DefaultTaskListComponent(
            componentContext = context,
            repo = repo,
            taskItemFactory = DefaultTaskItemComponent.Factory(taskRepository = repo),
            onEditRequested = { },
        )

        When("loading tasks") {
            // trigger behavior

            Then("should expose tasks from repository") {
                // assert observable state
            }
        }
    }
})
```

## Pattern 3: Using `testComponent`

```kotlin name=test-component-pattern.kt
class MyComponentTest : FunSpec({
    testComponent("should behave correctly") {
        val repo = FakeTaskRepository()

        val component = DefaultTaskListComponent(
            componentContext = context,
            repo = repo,
            taskItemFactory = DefaultTaskItemComponent.Factory(taskRepository = repo),
            onEditRequested = { },
        )

        // act
        // assert
    }
})
```

Use whichever pattern best matches the existing surrounding test style.

---

## What agents should prefer

When writing tests in this repository, prefer:

1. **BehaviorSpec**
2. **Given / When / Then**
3. **custom `When`**
4. **`testValue` for nested mutable state**
5. **`createComponentContext` for component setup**
6. **`testComponent` for component-aware scoped tests**
7. **fake repositories over mocks**
8. **real collaborators over over-mocking**
9. **observable outcomes over interaction verification**
10. **clear scenario names over technical names**

---

## What agents should avoid

Do not:

- introduce a different test framework style without reason
- replace project BDD style with arbitrary alternatives
- use lowercase `given/when/then` if project standard is uppercase
- create manual component context wiring when `createComponentContext` exists
- overuse mocks for repository or component collaboration
- test trivial delegation
- write assertions focused only on method calls
- add brittle tests tightly coupled to implementation details

---

## Heuristics before writing a test

Before adding a test, ask:

### 1. Is this testing real behavior?
If not, rethink the test.

### 2. Is this better as unit, integration, or E2E?
Choose the lowest level that still gives meaningful confidence.

### 3. Can this use a fake instead of a mock?
Prefer yes.

### 4. Does this project already provide a utility for this setup?
If yes, use it:
- `testValue`
- `createComponentContext`
- `testComponent`

### 5. Will this test survive harmless refactoring?
If not, it is likely too implementation-coupled.

---

## Final rule

Write tests in the style of this codebase, not in a generic textbook style.

In this project, that means:

- use **Kotest**
- prefer **BehaviorSpec**
- write **Given / When / Then**
- use the project’s **custom `When`**
- use **`testValue`** for mutable nested scenario state
- use **`createComponentContext`** for component tests
- use **`testComponent`** when appropriate
- prefer **fakes and real behavior**
- verify **outcomes, state, and feature behavior**

The best test is the one that gives strong confidence, reads clearly, and stays stable when implementation details change.