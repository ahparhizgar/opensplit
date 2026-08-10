# 2-2 UI of add expense should have defaults

Status: done

Summary:
Ensure the Add Expense UI uses sensible defaults so users can add an expense with minimal taps: default household, current user as payer, and equal split among participants.

Acceptance Criteria:
- The expense form preselects the current household.
- The current user is preselected as payer.
- The split defaults to equal among household members.
- Quick-save flow completes in 2 taps for typical use.

Implementation notes:
- Implemented outside BMAD; please add commit reference if desired.
- Manual verification performed on Android; unit tests recommended.
