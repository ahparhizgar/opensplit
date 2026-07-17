---
name: kotest
description: How to write tests using Kotest in this project
---

# Kotest Testing Standards

This guide defines the standards for writing tests in OpenSplit to ensure they are behavior-focused, stable, and aligned with the project's architecture.

## 1. Core Philosophy
*   **Test Behavior, Not Implementation:** Focus on observable outcomes (state, values, effects) rather than internal call chains. Tests must survive refactoring as long as the contract remains unchanged.
*   **Prefer Realism:** Use real objects or fakes instead of heavy mocking to gain higher confidence in component collaboration.
*   **Avoid Trivial Tests:** Do not test simple delegation logic that adds no business value.

## 2. Framework & Style
*   **Engine:** Use **Kotest** as the primary testing framework.
*   **Spec Style:** Default to **`BehaviorSpec`** for scenario-based tests.
*   **BDD Blocks:** Always use uppercase **`Given`**, **`When`**, and **`Then`**.
*   **Custom DSL:** Utilize the project's custom **`When`** variant that supports an action block.
*   **Coroutines:** Apply `extensions(MainDispatcherExtension())` inside spec (outside Given).

## 3. Behavioral Structure & Naming
A test should read like documentation of a feature's behavior:
*   **`Given`**: Initial context or setup (e.g., `Given("a user with existing tasks")`).
*   **`When`**: The trigger or event. Use the action block or `beforeEach` for mutations.
*   **`And`**: Optional addition to When or Given.
*   **`Then`**: The expected outcome or observable result.

```kotlin
class SampleTest :
    BehaviorSpec({
      extensions(MainDispatcherExtension())
      Given("an initial state") {
        var count by testValue { 1 }

        When("incrementing") {
          beforeEach {
            count++
          }
          And("incrementing again") {
            beforeEach {
              count++
            }
            Then("the value should be updated") {
              count shouldBe 3
            }
          }
        }
      }
    })

```

## 4. Dependencies & Collaborators
*   **Fakes over Mocks:** Use project-provided fakes (e.g., `FakeTaskRepository`) for deterministic and readable data modeling.
*   **Real Wiring:** For integration tests, wire the real object graph for the feature slice being tested instead of mocking every collaborator.
*   **Assertions:** Focus on resulting state or emitted values. Avoid "verify" style assertions on internal method calls unless they are part of a strict external contract.

## 5. Test Categories
*   **Unit:** Isolated logic (validators, mappers, reducers). Fast, deterministic, and dependency-free.
*   **Integration:** Component + Repository + Fake outside interactions. **This is the recommended default level.**
*   **E2E:** Broad wiring and critical user journeys. Use sparingly.
