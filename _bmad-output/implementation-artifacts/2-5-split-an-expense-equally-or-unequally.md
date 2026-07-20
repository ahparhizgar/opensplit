# Story 2.5: Split an expense equally or unequally

Status: review

## Story

As a household member,
I want to split an expense equally or with custom shares (unequally, percentages, shares, adjustments),
So that the debt matches the real arrangement.

## Acceptance Criteria

1. **Given** the user is adding an expense, **When** they choose a split method, **Then** they can select from: Equally, Unequally (Exact amounts), By Percentages, By Shares, and By Adjustment.
2. **Given** "Equal Split" is selected, **When** the expense is saved, **Then** the amount is divided evenly among all selected participants.
3. **Given** "Unequally" or "By Percentages" is selected, **When** the user enters custom values, **Then** the app validates that the sum matches the total expense amount (100% or total cost) before allowing save.
4. **Given** "By Shares" or "By Adjustment" is selected, **When** the user enters values, **Then** the app calculates the proportional split or adds adjustments to an equal split accordingly.
5. **Given** the user selects "Multiple People" as payers, **When** they enter individual paid amounts, **Then** the app validates the sum matches the total cost.
6. **Given** an expense is saved, **When** viewed in history or details, **Then** the exact split details (who paid what, who owes what) are visible.

## Task / Subtasks

### Backend (Server) Implementation
- [x] **Database Schema Update:**
  - Create `ExpenseParticipants` table: `id`, `expense_id`, `user_id`, `paid_amount`, `owed_amount`.
  - Update `Expenses` table if needed.
- [x] **Repository & Service Updates:**
  - Update `ExpenseRepository` to save and fetch participants.
  - Update `ExpenseService` to handle complex split persistence.
- [x] **API Update:**
  - Update `POST /households/{id}/expenses` to accept participant shares.
  - Ensure `ExpenseDto` returns the list of participants with their shares.

### Core (Shared) Implementation
- [x] **DTO Updates:**
  - Update `CreateExpenseRequest` to include `List<ParticipantShareDto>`.
  - Update `ExpenseDto` to include `List<ParticipantShareDto>`.
- [x] **Domain Logic:**
  - Implement `SplitCalculator` in `core/src/commonMain/kotlin/com/opensplit/domain/expense/`.
  - Supported modes: `EQUALLY`, `EXACT`, `PERCENTAGE`, `SHARES`, `ADJUSTMENT`.
  - Logic must calculate `owed_amount` for each user based on total cost and inputs.

### Client (Shared UI) Implementation
- [x] **AddExpenseComponent (Decompose):**
  - Manage complex split state (split mode, list of participants with their inputs).
  - Implement validation logic (sum of percentages == 100, sum of exact == total).
- [x] **UI Implementation (Screenshots reference):**
  - **Payer Selection:** Implement "Who paid?" screen.
  - **Split Quick Options:** Implement "How was this expense split?" selection.
  - **Adjust Split Tabs:** Implement tabbed interface for split methods.
- [x] **Navigation:** Wire the new selection screens into the `AddExpense` flow.

### Testing Implementation
- [x] **Backend Integration Test:** `ExpenseRoutesTest.kt`
  - [x] Test saving an expense with multiple payers and unequal split.
  - [x] Verify `ExpenseParticipants` are correctly persisted.
- [x] **Client Component Test:** `AddExpenseComponentTest.kt`
  - [x] Verify `SplitCalculator` output for various modes.
  - [x] Verify `AddExpenseComponent` prevents saving when splits don't reconcile.
  - [x] Verify navigation state transitions between split selection screens.

## Developer Context

### Screenshots & Image Descriptions
- **`image_4.png` (Main Form):** The primary expense entry form with description and amount fields. Shows a summary button "Paid by you and split equally" that triggers the split selection flow.
- **`image_6.png` & `image_7.png` (Quick Split):** A selection screen for common patterns like "You paid, split equally" or "Ali paid, split equally". `image_7.png` shows calculated owe amounts (e.g., "Amir2 owes you IRR100.00").
- **`image_9.png` (Payer Selection):** Selection for who paid. Includes "Multiple people" which leads to `image_5.png`.
- **`image_5.png` (Multiple Payers):** Input screen for entering exact amounts paid by each participant.
- **`image.png` (By Adjustment):** Tab in "Adjust split". Users enter adjustments (e.g., +10.00) and the remainder is split equally.
- **`image_1.png` (By Percentages):** Tab in "Adjust split". Users enter percentages totaling 100%.
- **`image_2.png` (By Shares):** Tab in "Adjust split". Users enter shares (e.g., 2 shares for a family of 2).
- **`image_3.png` (Equally):** Tab in "Adjust split". Checkboxes for who is included in the equal split.
- **`image_8.png` (Unequally/Exact):** Tab in "Adjust split". Users enter exact amounts each person owes.

### Technical Guardrails
- **Calculation Source of Truth:** The client calculates all shares (owed, paid, net) before sending to the server. Reference `sp-api/create_expense.jsonc` for the expected request/response structure.
- **Elephant Images:** Do NOT implement the elephant icons shown in the screenshots. Use standard Material icons or simple text labels.
- **Precision:** Ensure currency math is handled carefully to avoid rounding errors (use `Long` for cents or `Double` with controlled rounding as per existing patterns).

## Dev Notes
- **Architecture:** Keep split logic in the `core` module so it can be reused/tested in isolation.
- **UX:** Provide immediate feedback in the split adjustment screens (e.g., "10.00 left to split" or "105% of 100%").

## References
- `sp-api/create_expense.jsonc`
- `_bmad-output/planning-artifacts/epics.md#Story 2.5`
- `_bmad-output/planning-artifacts/architecture.md#Data Architecture`

## Dev Agent Record

### Implementation Plan
- Updated `core` DTOs to include participant details and `SplitType` enum.
- Created `SplitCalculator` in `core` to centralize shared split math.
- Added `ExpenseParticipants` table to `server` database schema.
- Updated `ExpenseRepository` and `ExpenseService` to handle complex split persistence.
- Revamped `AddExpenseComponent` to manage multi-screen split state and real-time recalculations.
- Implemented `AddExpenseScreen` with sub-screens for Payer selection, Quick options, and tabbed split adjustments.
- Added comprehensive tests for both backend (Ktor) and client (Decompose).

### Completion Notes
- All ACs satisfied.
- Client-side math handles Equals, Exact, Percentage, Shares, and Adjustments.
- Multi-payer support implemented in component logic.
- Rounding issues handled via small epsilon (0.01) in validations.
- Tests passing for all scenarios.

### File List
- `core/src/commonMain/kotlin/com/opensplit/dto/expense/ExpenseDtos.kt`
- `core/src/commonMain/kotlin/com/opensplit/domain/expense/SplitCalculator.kt`
- `core/src/commonTest/kotlin/com/opensplit/domain/expense/SplitCalculatorTest.kt`
- `server/src/main/kotlin/com/opensplit/database/Tables.kt`
- `server/src/main/kotlin/com/opensplit/database/DatabaseInitializer.kt`
- `server/src/main/kotlin/com/opensplit/features/expense/ExpenseModels.kt`
- `server/src/main/kotlin/com/opensplit/features/expense/ExpenseRepositoryImpl.kt`
- `server/src/main/kotlin/com/opensplit/features/expense/ExpenseService.kt`
- `server/src/test/kotlin/com/opensplit/features/ExpenseRoutesTest.kt`
- `app/shared/src/commonMain/kotlin/com/opensplit/features/expense/ExpenseApi.kt`
- `app/shared/src/commonMain/kotlin/com/opensplit/features/expense/AddExpenseComponent.kt`
- `app/shared/src/commonMain/kotlin/com/opensplit/features/expense/AddExpenseScreen.kt`
- `app/shared/src/commonTest/kotlin/com/opensplit/fake/FakeExpenseApi.kt`
- `app/shared/src/commonTest/kotlin/com/opensplit/fake/FakeHouseholdApi.kt`
- `app/shared/src/commonTest/kotlin/com/opensplit/AddExpenseComponentTest.kt`

## Change Log
- Initial implementation of Story 2.5 (Date: 2026-06-08)

## Status
Status: review
