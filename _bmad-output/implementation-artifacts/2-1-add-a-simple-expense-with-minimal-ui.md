# Story 2.1: Add a simple expense with minimal UI

Status: ready-for-dev

## Story

As a household member,
I want to add an expense using a minimal form,
So that I can log shared costs without unnecessary complexity.

## Acceptance Criteria

1. **Given** the user is in a household, **When** they navigate to the "Add Expense" screen, **Then** they see a minimal form with fields for description and amount.
2. **Given** the user enters a valid description and amount, **When** they save the expense, **Then** the expense is recorded and appears in the household history.

## Tasks / Subtasks

### Backend (Server) Implementation
- [ ] Create `Expenses` table in `server/src/main/kotlin/com/opensplit/database/Tables.kt` (AC: 2)
  - Fields: `id`, `household_id`, `description`, `amount`, `payer_id`, `created_at`
- [ ] Update `DatabaseInitializer.kt` to include `Expenses` table (AC: 2)
- [ ] Create `ExpenseRecord` and `CreateExpenseRequest` in a new `server/src/main/kotlin/com/opensplit/features/expense/ExpenseModels.kt` (AC: 2)
- [ ] Implement `ExpenseRepository` and `ExpenseRepositoryImpl` in `server/src/main/kotlin/com/opensplit/features/expense/` (AC: 2)
- [ ] Create `ExpenseService` to handle business logic and DTO mapping (AC: 2)
- [ ] Implement `ExpenseRoutes` and Ktor/Koin modules for expenses (AC: 2)
  - Endpoint: `POST /households/{id}/expenses`

### Core (Shared) Implementation
- [ ] Create `ExpenseDto` and `CreateExpenseRequest` in `core/src/commonMain/kotlin/com/opensplit/dto/expense/ExpenseDtos.kt` (AC: 2)
- [ ] Add expense validation logic in `core/src/commonMain/kotlin/com/opensplit/validation/expense/ExpenseValidation.kt` (AC: 2)

### Client (Shared UI) Implementation
- [ ] Update `HouseholdApi` to include `createExpense` method (AC: 2)
- [ ] Create `AddExpenseComponent` and `AddExpenseScreen` in `app/shared/src/commonMain/kotlin/com/opensplit/features/expense/` (AC: 1)
- [ ] Add "Add Expense" navigation to `HouseholdDetailsComponent` (AC: 1)
- [ ] Implement simple form with `description` and `amount` fields (AC: 1)
- [ ] Wire save button to call `createExpense` and navigate back on success (AC: 2)

### Testing Implementation
- [ ] **Backend Integration Test:** `ExpenseRoutesTest.kt`
  - [ ] `POST /households/{id}/expenses` returns `BadRequest` when description is empty
  - [ ] `POST /households/{id}/expenses` returns `Created` when form is valid
- [ ] **Component Integration Test:** `AddExpenseComponentTest.kt`
  - [ ] Navigate from `HouseholdDetailsComponent` to `AddExpenseComponent` on button click
  - [ ] `AddExpenseComponent` shows field errors when submitting empty form
  - [ ] `AddExpenseComponent` navigates back to `HouseholdDetails` on successful save

## Dev Notes

- **Architecture Compliance:**
  - Follow the **REST** pattern: `POST /households/{id}/expenses`.
  - Use **Exposed** for database operations in `server`.
  - Share **DTOs** in `core` module.
  - Use **Decompose** for navigation and component logic.
  - Use **Compose Multiplatform** for UI.
- **Data Handling:** Use `Double` for amount for now as per current `HouseholdMemberDto` pattern, but consider `BigDecimal` or `Long` (cents) if specified in future stories.
- **Validation:** Description must not be empty; amount must be greater than 0.

## Testing Requirements

- **Backend:** Use `testOpenSplit` and `Ktor` client to verify integration between routes, service, and database.
- **Component:** Use `com.arkivanov.decompose` testing utilities to verify navigation and component state changes. Verify `AddExpenseComponent` updates `uiState` with errors on validation failure.

### Project Structure Notes

- New feature directory: `com.opensplit.features.expense` in both `server` and `app/shared`.
- New DTO package: `com.opensplit.dto.expense` in `core`.

### References

- [Source: _bmad-output/planning-artifacts/epics.md#Story 2.1]
- [Source: _bmad-output/planning-artifacts/architecture.md#Data Architecture]
- [Source: _bmad-output/planning-artifacts/ux-design-specification.md#Fast Expense Entry]

## Dev Agent Record

### Agent Model Used

Amelia (Senior Software Engineer)

### File List

- `server/src/main/kotlin/com/opensplit/database/Tables.kt`
- `server/src/main/kotlin/com/opensplit/database/DatabaseInitializer.kt`
- `server/src/main/kotlin/com/opensplit/features/expense/*`
- `core/src/commonMain/kotlin/com/opensplit/dto/expense/ExpenseDtos.kt`
- `core/src/commonMain/kotlin/com/opensplit/validation/expense/ExpenseValidation.kt`
- `app/shared/src/commonMain/kotlin/com/opensplit/features/household/HouseholdApi.kt`
- `app/shared/src/commonMain/kotlin/com/opensplit/features/expense/*`
- `app/shared/src/commonMain/kotlin/com/opensplit/features/household/details/HouseholdDetailsComponent.kt`
- `app/shared/src/commonMain/kotlin/com/opensplit/features/household/details/HouseholdDetailsScreen.kt`
