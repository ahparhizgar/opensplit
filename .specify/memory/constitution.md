<!--
Sync Impact Report:
- Version change: [CONSTITUTION_VERSION] -> 1.0.0
- List of modified principles:
  - PRINCIPLE_1_NAME -> UI - JetBrains Compose and Keyboard Rule
  - PRINCIPLE_2_NAME -> Decompose Component Rules
  - PRINCIPLE_3_NAME -> Kotlin Time and Import Discipline
  - PRINCIPLE_4_NAME -> Dependency Injection and DI Patterns (Koin)
  - PRINCIPLE_5_NAME -> Testing and Fake Factory Discipline
- Added sections:
  - Data Modeling and DTOs (DTO & Serialization Laws)
  - Project Architecture and Verification Gate
- Removed sections:
  - None (replaces template placeholders)
- Follow-up TODOs:
  - None. All AGENTS.md rules translated to caveman dialect.
-->

# OpenSplit Constitution

## Core Principles

### UI - JetBrains Compose and Keyboard Rule
* Surface good. Scaffold good. Background modifier bad, use only when must.
* Text click? No raw `clickable` on Text. Use TextButton. Icon click? Use IconButton.
* Screen or public composable MUST have preview. Not one preview. Multiple previews for many states!
* Wrap all preview in OpenSplitTheme. No naked preview.
* Big screen matter! Support large screen with Decompose panels and `CContext.windowSizeHolder`. Use `AdaptiveTopAppbar` when want `LargeTopAppBar`. Put card content center on big screen.
* Keyboard king! Every knob, button, door must open with keyboard. Text field MUST have `keyboardOptions` and `keyboardActions`.

### Decompose Component Rules
* No `suspend` function on Decompose component! Function launch job inside, return `Job` to caller.
* Screen getting fat? Cut small private slice!
* Private slice take whole Decompose component. No chop states and callbacks into many pieces.
* Public generic slice with no component take raw states and callbacks.

### Kotlin Time and Import Discipline
* Time come only from `Clock.System.now()`.
* Old `System.currentTimeMillis()` forbidden. `Instant.now()` dead, forbidden.
* Need millis? Call `Clock.System.now().toEpochMilliseconds()`.
* No long full package name in code (no FQCN). Write short name, let compiler complain if wrong.

### Dependency Injection and DI Patterns (Koin)
* Bind dependency with `factoryOf(::DefaultRootComponentFactory).bind<RootComponentFactory>()`.
* Signature change in constructor? DI binding stay strong, no manual fix.

### Testing and Fake Factory Discipline
* UI test MUST press keyboard keys to prove keyboard works.
* Fake factories live in same file right after real class. Never make trash files like `Models.kt` or `Dtos.kt`.
* File order law: 1. Main class, 2. Extension functions, 3. Related class, 4. Fake factory.
* Fake factory MUST have `create()` with smart defaults. Add `createList()` when list handy.
* Test MUST use fake factory. Fake factory MUST call neighbor fake factory (FakeGroupFactory call FakeParticipantFactory, not make participant itself).

## Data Modeling and DTOs

* String for enum in DTO? Forbidden! Enum MUST be true enum class.
* Model all domain data with sealed interface. Live documentation with Kotlin Serialization.
* Split file per concept (e.g., `GroupDto.kt` for DTO, `Group.kt` for domain models and fake factory).

## Project Architecture and Verification Gate

* Big picture map:
  - `server`: Ktor, Exposed, Hikari, PostgreSQL (or H2 in test).
  - `core`: Shared core library for everyone.
  - `app/shared`: KMP Compose UI for Android, Desktop, Web.
  - `app/androidApp`, `app/desktopApp`, `app/webApp`: Platform launcher heads.
* Special colors `youOwe` and `youAreOwed` live inside `MaterialTheme.colorSchemeExtended` in `Extended.kt`.
* Task finish? Agent MUST run verification club:
  `./gradlew jvmTest test ktfmtFormat --offline`

## Governance

* Caveman constitution rule whole tribe! Outrank all random ideas.
* Any change to rule need patch note, version bump, consensus.
* Pull request MUST check rules. Fail verification gate = no merge rock into cave.

**Version**: 1.0.0 | **Ratified**: 2026-09-24 | **Last Amended**: 2026-09-24
