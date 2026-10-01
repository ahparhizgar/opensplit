[![Kotlin](https://img.shields.io/badge/Kotlin-2.4.0-blue.svg?style=flat&logo=kotlin)](https://kotlinlang.org)
[![Build](https://github.com/ahparhizgar/opensplit/actions/workflows/build.yml/badge.svg)](https://github.com/ahparhizgar/opensplit/actions/workflows/build.yml)
![Coverage](https://img.shields.io/endpoint?url=https%3A%2F%2Fgist.githubusercontent.com%2Fahparhizgar%2F91190edf571eb1024def80464e70b88a%2Fraw%2Fcoverage.json)

![](pictures/banner.jpeg)

# OpenSplit

**Share expenses with your roommate, travel companions, etc...**

# ToC

* [Test Coverage](#test-coverage)
* [Offline-First Architecture](#offline-first-architecture)

## Modules

Core contains shared code used by both server and client.  
Shared code are DTOs and verification logic.

```mermaid
graph BT
%% Core module
    subgraph CoreModule [Core Module]
        core[core]
    end

%% Server module
    subgraph ServerModule [Server Module]
        server[server]
    end

%% Client module and submodules
    subgraph client [Client Module]
        direction BT
        shared[shared]
        androidApp[androidApp]
        DesktopApp[DesktopApp]
        iosApp[iosApp]
    end

%% Dependencies flowing bottom to top
    server --> core
    client --> core
%% Internal client relationships
    androidApp -.-> shared
    DesktopApp -.-> shared
    iosApp -.-> shared
```

## Build and test

To run two independent instances of the jvm app run:

```shell
./gradlew --offline runA
./gradlew --offline runB
```

## Test Coverage

This project uses **Kover** to measure and report code coverage across the project.

- **Combined Report**: Run `gradle koverHtmlReport` to get an aggregated coverage report for all
  modules.
- **Module-specific Reports**:
    - **Server**: `gradle :server:koverHtmlReport`
    - **Client**: `gradle :app:shared:koverHtmlReport`

## Error Handling

This project uses [Katch](https://github.com/ahparhizgar/katch/) a library form the same author;
which wraps http and network errors into a hierarchical exception model.

```text
ApiCallError (base class)
├─ InvalidDataError
├─ NetworkError           // Connection issues, timeouts
└─ HttpError              // HTTP status code errors
    ├─ ServerError        // 5xx errors
    └─ ClientError        // 4xx errors
        ├─ BadRequest     // 400
        ├─ Unauthorized   // 401
        :
        └─ OtherClientError // Other 4xx codes
```

These exceptions are automatically handled using a `CoroutineExceptionHandler` in the client, and a `MessageShower` is used to display a snack-bar message to the user.

```kotlin
interface MessageShower {
  suspend fun showSnackbarForResult(message: SnackbarMessage): SnackbarResult

  suspend fun showSnackbar(message: SnackbarMessage)
}
```

So launching a network request is as simple as:
```kotlin
scope.launch {
  val result = apiCall()
  // Network errors are handled automatically
}
```

## Offline-First Architecture

This project implements a local-first, bidirectional sync approach. The UI binds directly to the local database, while mutations are queued in an outbox and downstream updates are fetched via versioned delta sync.

```mermaid
flowchart LR
    UI[UI / User Actions] -->|Write & Observe| DB[(Local Room DB)]
    DB -->|Enqueue Op| Queue[Sync Outbox Queue]
    Queue -->|Process FIFO| SyncMgr[SyncManager]
    SyncMgr -->|1. Upstream mutations| Server[Ktor Server]
    Server -->|2. Downstream delta sync| SyncMgr
    SyncMgr -->|Reconcile & Mark SYNCED| DB
```
- **Local-Source-of-Truth**: The UI observes Room database `Flows` directly. User actions (
  e.g. creating/editting expenses) are written to local storage and a **Sync Outbox** immediately,
  providing zero-latency feedback.
- **Hybrid Sync Strategy**:
    - **Delta Sync (Expenses)**: Uses a shared global version sequence on the backend. Clients pull
      only incremental changes since their last `sync_version`, minimizing payload size.
    - **Full-Refresh (Groups)**: Semi-static metadata is refetched on app launch and navigation to
      ensure consistency, while keeping the local cache for offline navigation.
- **Background Orchestration**: A `SyncManager` handles non-blocking background polling, outbox
  processing, and application of server deltas, ensuring the app remains 
  functional even with intermittent connectivity.

## 📸 Screenshot Testing

This project uses **Roborazzi** to automatically test the UI.

- **One Annotation Does It All**: Just tag the screens with `@ScreenPreview` to automatically test Mobile Dark, and Desktop Light modes. For smaller parts of the UI, use `@ComponentPreview` to test Light and Dark modes.
- **Automatic Discovery**: There is no need to write manual test files for previews! Roborazzi scans the codebase and tests them automatically. To skip a specific preview, simply add `@ExcludeScreenshotTest`.
