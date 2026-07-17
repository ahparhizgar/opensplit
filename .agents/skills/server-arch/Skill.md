---
name: server-arch
description: architecture of server of this repo
---

## Architecture
- Organize server code by feature under `features/`.
- Each feature contains `Routes`, `Service`, `Repository`, `RepositoryImpl`, `Models`, `KoinModule` and `KtorModule`.
- Write shared DTOs to `:core`
- Shared infrastructure belongs in `plugins/`, `config/`, `database/`, and `common/`.

## Conventions
- Use Koin dependency injection.
- prefer factoryOf, singleOf.
- Each feature must have a `KoinModule.kt` file that defines a `fun {feature}KoinModule() = module { ... }`.
- Do not use default constructor parameters.
- Prefer nested routing:
  ```kotlin
  route("/household") {
      get { }
  }
  ```
## Validation
1. Run:
   ```bash
   ./gradlew :server:test --offline
   ```