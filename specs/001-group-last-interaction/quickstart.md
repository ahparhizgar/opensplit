# Quickstart Validation Guide: Group Last Interaction At

## Prerequisites
- JDK 21+
- Gradle wrapper executable `./gradlew`

## Validation Scenarios

### 1. Server Integration Tests
Verify that group creation, expense addition, updating, and deletion properly update `lastInteractionAt` and return it in `GroupDto`.

Command:
```bash
./gradlew :server:test --tests "com.opensplit.features.GroupRoutesTest" --tests "com.opensplit.features.ExpenseRoutesTest"
```

Expected outcomes:
- `POST /groups` returns `GroupDto` where `lastInteractionAt` is approximately now (within last few seconds).
- `POST /groups/{groupId}/expenses` updates group's `lastInteractionAt`.
- `GET /groups` returns updated `lastInteractionAt`.

### 2. Client Domain & Screen UI Tests
Verify that `MyGroupsListScreen` correctly partitions active vs settled groups based on `lastInteractionAt >= now - 7.days`.

Command:
```bash
./gradlew :app:shared:jvmTest --tests "com.opensplit.features.group.my.MyGroupsListScreenTest"
```

Expected outcomes:
- Settled group with `lastInteractionAt = now - 2.days` renders in active section.
- Settled group with `lastInteractionAt = now - 10.days` renders in settled section.
- Unsettled group (balance != 0) with `lastInteractionAt = now - 30.days` renders in active section.

### 3. Full Verification Gate
Verify complete build, tests, and formatting compliance across all modules.

Command:
```bash
./gradlew jvmTest test ktfmtFormat --offline
```
