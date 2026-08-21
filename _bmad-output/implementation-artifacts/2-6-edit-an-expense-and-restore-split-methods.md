# Story 2.6: Edit an expense and restore split methods

Status: ready-for-dev

## Story
As a household member,
I want to edit an existing expense and have its split method restored,
so that I can easily adjust details while maintaining the original intent.

## Acceptance Criteria
1. **Given** an expense exists, **When** the user edits its title, amount, payer, or split details and saves, **Then** the changes are persisted and balances are recalculated.
2. **Given** an expense was created with a specific split method (e.g. Percentage, Shares, Adjustment), **When** the user opens the edit screen, **Then** the original split method and its inputs (e.g. the actual percentages or shares entered) are restored and editable.
3. **Given** a user is editing an expense, **When** they change the total amount, **Then** the split calculations (if based on percentages, shares, or equal split) update automatically, while fixed amounts (Unequally) remain unchanged but may trigger validation errors if they no longer sum to the total.
4. **Given** a user is in the edit screen, **When** they switch from one split method to another (e.g., from Equal to Percentage), **Then** the new method is initialized with reasonable defaults based on the current participants.

## TDD / Tests (Given-When-Then)

### Backend Tests (`ExpenseRoutesTest.kt`)
- **Scenario: Successfully update expense title and amount**
  - **Given** an existing expense with ID `exp-1`, amount `100.0`, and Equal split
  - **When** a `PUT /households/{hId}/expenses/exp-1` request is made with title "New Title" and amount `150.0`
  - **Then** the server returns 200 OK
  - **And** the expense title is `New Title` and amount is `150.0` in the database
  - **And** `ExpenseParticipants` owed amounts are updated to `75.0` each (for 2 members)
- **Scenario: Change payer and verify balance recalculation**
  - **Given** an expense where User A paid `100.0` and split equally with User B
  - **When** the expense is updated to set User B as the payer
  - **Then** the `ExpenseParticipants` table reflects that User B paid `100.0`
  - **And** the household balances are adjusted accordingly
- **Scenario: Update split method from Equal to Unequal**
  - **Given** an expense with Equal split
  - **When** it is updated to Unequal split with specific `owedAmount` values
  - **Then** the database correctly stores the new `splitMethod` DTO
  - **And** the `ExpenseParticipants` table matches the exact values provided
- **Scenario: Fail to update non-existent expense**
  - **When** a `PUT` request is made for a non-existent expense ID
  - **Then** the server returns 404 Not Found
- **Scenario: Fail to update expense in a household the user doesn't belong to**
  - **Given** an expense in Household X
  - **When** a user who is NOT a member of Household X tries to `PUT` the expense
  - **Then** the server returns 403 Forbidden

### Client Tests (`AddExpenseComponentTest.kt`)
- **Scenario: Restore "Percentage" split method**
  - **Given** an expense saved with `Percentage(percentages = {U1: 70, U2: 30})`
  - **When** the edit screen is opened and "Adjust Split" is clicked
  - **Then** the "Percentage" tab is selected
  - **And** the text fields for U1 and U2 show `70` and `30`
- **Scenario: Restore "Shares" split method**
  - **Given** an expense saved with `Shares(shares = {U1: 2, U2: 1})`
  - **When** the edit screen is opened
  - **Then** the "Shares" tab is active and shows `2` and `1` in the inputs
- **Scenario: Restore "Adjustment" split method**
  - **Given** an expense with `Adjustment(adjustments = {U1: 10.0})`
  - **When** editing, the "Adjustment" tab shows U1 has a `+10.0` adjustment
- **Scenario: Auto-recalculate percentages when total amount changes**
  - **Given** an expense of `100.0` with 50/50 percentage split
  - **When** the user changes the total amount to `200.0`
  - **Then** the UI shows each person now owes `100.0` (based on 50%)
- **Scenario: Preserve fixed "Unequally" amounts when total amount changes**
  - **Given** an expense of `100.0` split unequally as `U1: 40.0, U2: 60.0`
  - **When** the user changes the total amount to `120.0`
  - **Then** the UI still shows `40.0` and `60.0` as inputs
  - **And** a validation error appears stating `20.0` is left to split
- **Scenario: Switch split method during edit**
  - **Given** a user is editing an "Equal" split expense
  - **When** they tap the "Percentage" tab
  - **Then** the percentage inputs are prefilled with equal values (e.g., 50/50)

## Tasks / Subtasks

### 1. Backend: Repository & API
- [ ] **Repository Update:** Implement `updateExpense` in `ExpenseRepository`.
  - Handle updating the `Expenses` table.
  - Surgical update or delete/re-insert for `ExpenseParticipants`.
- [ ] **API Update:** Implement `PUT /households/{hId}/expenses/{expenseId}` in `ExpenseRoutes.kt`.
- [ ] **Validation:** Ensure the user belongs to the household before allowing the update.

### 2. Client: Logic & Navigation
- [ ] **Component State:** Update `AddExpenseComponent` to accept an `expenseId` for Edit mode.
- [ ] **Data Fetching:** Fetch existing expense details in `Edit` mode.
- [ ] **Split Restoration:** Implement logic to map the `SplitMethod` DTO back to the internal `AddExpenseComponent` UI state (AC: 2).
- [ ] **Validation Logic:** Update validation to handle the "Edit" context (e.g. immediate validation after change).

### 3. Client: UI
- [ ] **Header Update:** Change screen title to "Edit Expense" when an `expenseId` is present.
- [ ] **Top Bar:** Ensure the save button is correctly wired to the `PUT` API.
- [ ] **Form Population:** Pre-fill all fields (Description, Amount, Payer) from the fetched expense.

## Dev Notes
- **Split Restoration:** This is CRITICAL. The `SplitMethod` sealed class was designed to hold the "intent". Do not just use the calculated `shares` list for editing if a specific method was used.
- **Sync:** Remember that edits must be tracked for offline sync. Ensure `ChangeLog` is updated with `operation = "UPDATE"`.
- **Delete:** Already implemented in previous stories. This story focuses solely on the Edit flow and restoration of intent.

## References
- `core/src/commonMain/kotlin/com/opensplit/dto/expense/SplitMethod.kt`
- `server/src/main/kotlin/com/opensplit/database/Tables.kt`
- `_bmad-output/implementation-artifacts/2-5-split-an-expense-equally-or-unequally.md`
