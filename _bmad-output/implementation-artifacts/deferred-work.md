## Deferred from: code review of 2-1-add-a-simple-expense-with-minimal-ui.md (2026-07-18)

- Financial Precision (ID 1): The code uses `Double` for expense amounts. Needs migration to `BigDecimal` or `Long` (cents) later.
