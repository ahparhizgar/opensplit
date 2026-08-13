![Kotlin](https://img.shields.io/badge/Kotlin-2.4.0-blue.svg?style=flat&logo=kotlin)(https://kotlinlang.org)
![Build](https://github.com/ahparhizgar/opensplit/actions/workflows/build.yml/badge.svg)(https://github.com/ahparhizgar/opensplit/actions/workflows/build.yml)
![Coverage](https://img.shields.io/endpoint?url=https%3A%2F%2Fgist.githubusercontent.com%2Fahparhizgar%2F91190edf571eb1024def80464e70b88a%2Fraw%2Fcoverage.json)


# OpenSplit
**share expenses with your roommate, travel companions, etc...**

## Build and test

To run two independent instances of the jvm app run:
```shell
./gradlew --offline runA
./gradlew --offline runB
```

## Test Coverage

We use **Kover** to measure and report code coverage across the project.

- **Combined Report**: Run `gradle koverHtmlReport` to get an aggregated coverage report for all modules.
- **Module-specific Reports**:
    - **Server**: `gradle :server:koverHtmlReport`
    - **Client**: `gradle :app:shared:koverHtmlReport`

Reports are generated in the `build/reports/kover/html/` directory of the respective project/module.

## Offline-First Architecture

This project demonstrates a robust **Offline-First** synchronization.

- **Local-Source-of-Truth**: The UI observes Room database `Flows` directly. All user actions (creating expenses, joining groups) are written to local storage and a **Sync Outbox** immediately, providing zero-latency feedback.
- **Hybrid Sync Strategy**:
    - **Delta Sync (Expenses)**: Uses a shared global version sequence on the backend. Clients pull only incremental changes since their last `sync_version`, minimizing payload size.
    - **Full-Refresh (Households)**: Semi-static metadata is refetched on app launch and navigation to ensure consistency, while keeping the local cache for offline navigation.
- **Background Orchestration**: A `SyncManager` handles non-blocking background polling, outbox processing, and conflict-free application of server deltas, ensuring the app remains fully functional even with intermittent connectivity.
