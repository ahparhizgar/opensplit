# Sprint Change Proposal — 2026-08-10

## Issue Summary
Trigger: Multiple stories were implemented outside BMAD because the backlog ordering felt imperfect and BMAD felt overwhelming.
Affected stories: 2-2, 2-3, 2-5, 4-1, 4-2
Evidence: git status, sprint-status.yaml, and missing story files under _bmad-output/implementation-artifacts.

## Impact Analysis
- Epics: Epic 2 and Epic 4 have several stories now marked 'done' in sprint-status.yaml.
- Artifacts: Missing implementation artifact files for 2-2, 2-3, 4-1, 4-2 were created to restore traceability.
- Technical: Code changes already in repo; no rollback recommended.

## Recommended Approach
Direct Adjustment (modify BMAD artifacts to reflect implemented work). Rationale: work is already implemented and passing manual verification; syncing artifacts preserves team traceability with minimal risk.

Effort estimate: Low (create artifacts + small review)
Risk: Low

## Detailed Change Proposals
1) Create implementation artifact files for each implemented story and mark status 'done' (created and committed).
   - Files created:
     - _bmad-output/implementation-artifacts/2-2-ui-of-add-expense-should-have-defaults.md
     - _bmad-output/implementation-artifacts/2-3-implement-inline-validations-for-required-fields.md
     - _bmad-output/implementation-artifacts/4-1-save-expenses-and-settlements-while-offline.md
     - _bmad-output/implementation-artifacts/4-2-sync-offline-changes-after-reconnecting.md
   - Commit: 0d6c9c2
2) Keep sprint-status.yaml as-is (it already marks these stories as done).
3) Recommend adding commit references into the created story files for future traceability.

## Implementation Handoff
Scope: Minor — Developer agent / Engineers to finalize by adding commit refs and tests.
Handoff to: Developer(s) for small follow-ups (add unit/UI tests, record commit refs).
Success criteria: Each created story file contains a link to the implementing commit; CI passes and manual smoke tests succeed.

---

Please review and approve to finalize this proposal.
