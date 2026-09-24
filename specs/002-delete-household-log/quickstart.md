# Quickstart & Verification Guide: Sync Enum Refactor & Household Log Removal

## Scenario 1: Verify Unit and Route Tests

Run the backend test suite verifying sync routes and repositories:

```bash
./gradlew :server:test --tests "com.opensplit.features.SyncRoutesTest"
```

### Expected Outcome:
- User A creates group and expense -> change is recorded with `SyncEntityType.EXPENSE` and `SyncOperation.INSERT`.
- User B joins group -> membership changes recorded with `SyncEntityType.MEMBERSHIP` and `SyncOperation.INSERT`.
- Sync queries at `/sync?sinceVersion=N` succeed and return correct versions.
- All test assertions pass.

---

## Scenario 2: Verification Gate

Run the repository-wide gate:

```bash
./gradlew jvmTest test ktfmtFormat --offline
```

### Expected Outcome:
- All unit, integration, and UI tests pass.
- Formatting adheres strictly to ktfmt.
- No compilation errors from enum conversions.
