# 2-3 Implement inline validations for required fields

Status: done

Summary:
Add inline validation to the Add Expense form so required fields (amount, participants) show immediate feedback and prevent invalid saves.

Acceptance Criteria:
- Amount field shows error for non-numeric or empty input.
- Participant selection shows error if none selected when required.
- Validation messages are accessible and localized.
- Inline validation prevents saving until corrected.

Implementation notes:
- Implemented outside BMAD; please add commit reference if desired.
- Manual tests performed; consider adding UI tests.
