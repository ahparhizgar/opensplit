# UI - JetBrains Compose
use Surface(color, contentColor), or Scaffold instead of background modifier where possible.
Use TextButton instead of `clickable` modifier on a Text most of the time.
Use IconButton instead of `clickable` modifier on an Icon most of the time.
Always write previews for screens or public composable components.
Write multiple previews for different important states of the component/screen.
Wrap the preview in OpenSplitTheme.

## Large screens
Support large screens - read `adaptive` skill if needed.
use of Decompose panels and `CContext.windowSizeHolder`
Use `AdaptiveTopAppbar` if you want to use `LargeTopAppBar`.
Use centered cards content on large screens.

## Keyboard accessibility
Be keyboard-accessible. every functionality should be accessible via keyboard.
Always set keyboardOptions and keyboardActions for text fields.

# Using Decompose
Don't make functions of Decompose components `suspend`. instead launch in them and return the `Job`.
It's a good practice to keep screen functions small by extracting private sections.
Pass the whole decompose component to extracted private composables. don't split states and callbacks in them.
But for public components which there is no specific component, pass states and callbacks instead of decompose component.
Always use Value class for states.

# Using kotlin
Always use Clock.System.now() for time, don't use System.currentTimeMillis() or Instant.now().
Don't Use Instant.now(), it's deprecated.
If you need millis use Clock.System.now().toEpochMilliseconds()
Don't use fully qualified name. Write simple name, and let the compiler give error to you.
Prefer enums over strings. Use enums in DTOs and DAOs and domain layer where possible.
when using delay use the overload delay(10.milliseconds) not the one with Long parameter (deprecated).

# Using KOIN
Define dependencies like this: `factoryOf(::DefaultRootComponentFactory).bind<RootComponentFactory>()` So when the signature changes, no need to change the binding.


# Testing

Write UI tests using keyboard actions so keyboard accessibility is verified.
Use default value of fake factories if it doesn't matter.
Testing UI
Testing integration of Decompose components
Unit testing

Testing on backend
Testing multi-user on backend

# How to structure classes into files
Put fake factories after the real classes in the same file.
Don't create file such as Dtos.kt or Models.kt. Instead, create a file for each group of related DTOs or model classes. For example, create GroupDto.kt for GroupDto class and Group.kt for Group model class. Group.kt may contain GroupParticipants class etc. And must contain their fake factories.
In a single file, order like this: 1. the main class/interface, 2. extension functions (if any) 3. related classes, 4. fake factories (in the same order as classes).
In repository file, you can place enums that are used by the repository functions.

# Fake factories
Create meaningful factory functions
Almost always should be a general create()
Write createList() in fake factories if appropriate
Always use fake factories in tests.
Fake factories should use each other. so FakeGroupFactory should use FakeParticipantFactory to create participants. Don't create participants in FakeGroupFactory.

# DTOs
Always use enums in DTOs where possible. Don't use strings for enums in DTOs. Use enums instead.
Fully model data structure using sealed interfaces. we use kotlin serialization and shared module for DTOs. So it creates a live documentation.

GEMINI: use `run_shell_command` tool ./gradlew instead of `gradle_build` tool call. this prevents blocking by an already running Gradle task in the IDE. So always use run_shell_command tool.

# Using Room
always write migrations for Room when schema changes.

# Using Exposed
Use the DSL not the DAO.
Always use `timestamp("name")` instead of `long("name")` for timestamp columns.

---

Project "big picture" (what talks to what)
- Modules:
  - `server` — Ktor backend (main class `com.opensplit.ApplicationKt`) configured in `server/build.gradle.kts`. Uses Exposed + Hikari + PostgreSQL (or H2 for tests).
  - `core` — shared core libraries used across targets (`core/src/commonMain`).
  - `app/shared` — Kotlin Multiplatform UI and client code used by `androidApp`, `desktopApp` and `webApp`. Targets: JVM, Android, iOS simulator, wasmJs. Common code under `app/shared/src/commonMain`.
  - `app/androidApp`, `app/desktopApp`, `app/webApp` — platform entry points.


Project important niche points
  - `youOwe` and `youAreOwed` colors are in the `MaterialTheme.colorSchemeExtended` object defined in `Extended.kt`

Run verification gate after finishing a task:
```bash
./gradlew jvmTest test ktfmtFormat --offline
```
