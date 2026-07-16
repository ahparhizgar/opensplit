---
stepsCompleted:
  - 1
  - 2
  - 3
  - 4
  - 5
  - 6
  - 7
inputDocuments:
  - _bmad-output/planning-artifacts/prd.md
  - _bmad-output/planning-artifacts/ux-design-specification.md
  - _bmad-output/planning-artifacts/product-brief-bmad-test.md
workflowType: 'architecture'
project_name: 'bmad-test'
user_name: 'Amir'
date: '2026-05-13'
lastStep: step-02-context
completed: true
---

# Architecture Decision Document

_This document builds collaboratively through step-by-step discovery. Sections are appended as we work through each architectural decision together._

## Project Context Analysis

### Requirements Overview

**Functional Requirements:**
OpenSplit is a shared-expense product centered on household creation, member management, expense entry, split calculation, balance review, settlement tracking, offline capture, sync recovery, and secure access on mobile and web. Architecturally, this implies a household-scoped domain model, expense and settlement workflows, offline persistence, sync conflict handling, and shared business logic for consistent calculations across platforms.

**Non-Functional Requirements:**
The architecture must prioritize speed, reliability, privacy, and offline resilience. Key constraints include sub-0.5s Android startup on a Galaxy A50, at least 98% crash-free sessions, responsive core flows, secure authentication, minimum data collection, WCAG 2.1 AA accessibility, and predictable sync behavior users can trust.

**Scale & Complexity:**
The project is medium complexity with a focused domain and multiple cross-cutting concerns. It is primarily a mobile-first product with a web companion surface and shared Kotlin Multiplatform business logic.

- Primary domain: mobile_app with web companion access
- Complexity level: medium
- Estimated architectural components: 6

### Technical Constraints & Dependencies

- Kotlin Multiplatform for shared business logic across platforms
- Offline-first behavior is a core v1 requirement, not an enhancement
- Mobile performance is critical, especially on lower-end Android hardware
- Web access must expose the same core household and balance model
- Security and privacy must be handled by default with minimal personal data collection
- Sync must preserve user-entered data and resolve conflicts predictably

### Cross-Cutting Concerns Identified

- Offline persistence and synchronization
- Balance calculation consistency across platforms
- Authentication and household-scoped access control
- Privacy and secure data handling
- Performance and startup responsiveness
- Responsive UX consistency between mobile and web
- Accessibility for a broad public audience

## Starter Template Evaluation

### Primary Technology Domain

Mobile app with shared Kotlin Multiplatform code and a web companion surface.

### Starter Options Considered

- Archived Compose Multiplatform template: not selected because JetBrains marks it archived and points new projects to the Kotlin Multiplatform wizard instead.
- Kotlin Multiplatform wizard with Share UI enabled: selected because it is the current supported path for Compose Multiplatform projects and fits the need for shared UI and shared logic.

### Selected Starter: Kotlin Multiplatform Wizard

**Rationale for Selection:**
This is the current JetBrains-recommended starting point for Compose Multiplatform projects. It avoids building the platform wiring manually, aligns with the project's shared Kotlin logic strategy, and gives us a supported base for Android plus companion surfaces.

**Initialization Command:**

```bash
Use the Kotlin Multiplatform wizard at https://kmp.jetbrains.com/ and enable Share UI.
```

**Architectural Decisions Provided by Starter:**

**Language & Runtime:**
Kotlin Multiplatform with shared Kotlin code across targets.

**Styling Solution:**
Compose Multiplatform / Compose-based UI foundation.

**Build Tooling:**
Gradle-based Kotlin Multiplatform project structure.

**Testing Framework:**
Standard Kotlin/JVM and platform test setup, extendable per target.

**Code Organization:**
Shared logic separated from platform-specific app entry points and UI targets.

**Development Experience:**
Current JetBrains-supported path, with wizard-generated project structure and platform scaffolding.

**Note:** Project initialization using this command should be the first implementation story.

## Core Architectural Decisions

### Decision Priority Analysis

**Critical Decisions (Block Implementation):**
- Data architecture: local-first with sync
- Authentication: email/password now, mixed approach later
- API pattern: REST
- Frontend architecture: shared UI across targets
- Deployment baseline: VPS-hosted backend with Docker Compose

**Important Decisions (Shape Architecture):**
- Offline persistence must be treated as core behavior
- Sync conflicts must be resolved predictably and visibly
- Shared business logic must remain the source of truth for balance calculation
- Household-scoped access control must be enforced consistently
- DTOs can be shared between Kotlin backend and Kotlin client to reduce drift

**Deferred Decisions (Post-MVP):**
- OAuth providers for the mixed auth approach
- Advanced hosting automation beyond scripted Docker Compose deployment
- Alternative API mechanisms beyond REST if future needs require them

### Data Architecture

OpenSplit uses a local-first data model with synchronization to a server-backed source of truth. This supports offline expense creation, editing, and settlement, while preserving fast mobile interactions. Shared business logic should own balance and split calculations so results remain consistent across platforms. Data validation should happen both locally for immediate feedback and server-side for trust and integrity. Conflict resolution must be understandable to users and preserve entered data where possible.

### Authentication & Security

Authentication starts with email/password and secure session handling. A mixed approach can be added later, but the initial system should stay simple and consistent across mobile and web. Authorization is household-scoped, meaning access checks must verify membership before reading or mutating household data. Privacy-first handling and minimal data collection remain default requirements.

### API & Communication Patterns

OpenSplit uses REST for client-server communication. This keeps the API easy to reason about for mobile, web, and sync flows. Error responses should be standardized and sync-friendly so clients can recover predictably. REST also fits the app's need for explicit create/edit/settle operations and straightforward auth/session handling.

### Frontend Architecture

The app uses shared UI across targets on top of Kotlin Multiplatform. Shared UI and shared business logic should minimize drift between platforms and keep the core experience consistent. The component model should stay compact and focused on the expense-entry, balance-review, and settlement flows. Platform-specific shell code should be kept thin.

### Infrastructure & Deployment

OpenSplit will be deployed on a VPS with Docker Compose managing the backend runtime and supporting services. This gives a simple, repeatable server setup with minimal manual work while keeping control over the environment. Deployment should be scripted and reproducible, with environment configuration separated from code. Monitoring, logs, and backups will need explicit implementation because the project is self-hosted.

### Decision Impact Analysis

**Implementation Sequence:**
1. Initialize the Kotlin Multiplatform project
2. Set up shared domain models and validation
3. Build local persistence and sync foundations
4. Implement email/password auth and session handling
5. Define REST endpoints for household, expense, settlement, and sync flows
6. Build shared UI screens and shared interactions
7. Prepare VPS and Docker Compose deployment scripts

**Cross-Component Dependencies:**
- Local-first storage depends on REST sync endpoints and shared data models
- Shared UI depends on shared domain logic for consistent display and behavior
- Auth and household access rules affect every API and data mutation path
- VPS deployment depends on Docker Compose and environment configuration conventions
- Shared Kotlin DTOs depend on a common module boundary that both client and backend can consume

## Implementation Patterns & Consistency Rules

### Pattern Categories Defined

**Critical Conflict Points Identified:**
5 areas where AI agents could make different choices

### Naming Patterns

**Database Naming Conventions:**
Use lowercase camelCase-compatible names in code and API-facing models. Keep entity names singular in domain code and use consistent identifiers like `householdId`, `expenseId`, and `memberId` throughout shared logic.

**API Naming Conventions:**
Use plural REST endpoints such as `/households`, `/expenses`, and `/settlements`. Route params should use a simple `:id` style in documentation and implementation. JSON fields should use camelCase.

**Code Naming Conventions:**
Use PascalCase for types and components, camelCase for functions and variables, and camelCase file names where practical in shared code. Keep names domain-first and avoid abbreviations unless they are already established in the product language.

### Structure Patterns

**Project Organization:**
Organize primarily by feature, with shared core layers underneath for domain, data, and UI primitives. Keep feature code together so each vertical slice is easy to understand, while shared layers hold reusable business rules and platform-neutral models.

**File Structure Patterns:**
Use mixed test placement: co-locate unit tests with the code they verify, and keep integration or workflow-level tests in dedicated test folders. Place shared utilities in shared core modules rather than scattering them across features.

### Format Patterns

**API Response Formats:**
Return direct JSON responses for success cases. Use a minimal, consistent error shape for failures so clients can show inline messages or escalate to global sync handling when needed.

**Data Exchange Formats:**
Use camelCase JSON fields, booleans as true/false, ISO timestamps for dates, and explicit nulls where absence matters. Keep sync payloads stable and predictable.

### Communication Patterns

**Event System Patterns:**
If events are introduced later, use clear domain names tied to household actions, but do not introduce an event system unless a concrete use case requires it.

**State Management Patterns:**
Use unidirectional state flow with immutable state. Prefer a Decompose-based navigation/state structure with MVVM-like presentation logic, not MVI. State changes should be explicit and predictable so shared UI stays consistent.

### Process Patterns

**Error Handling Patterns:**
Handle validation and user-correctable issues inline wherever possible. Use global handling for sync failures, rare edge cases, and unexpected exceptions. Decompose custom context can carry global error state where needed.

**Loading State Patterns:**
Keep loading states local to the affected screen or component unless a global sync or app-level operation is in progress. Loading indicators should be brief, specific, and never block unrelated interactions.

### Enforcement Guidelines

**All AI Agents MUST:**

- Use feature-first organization with shared core layers for reusable logic
- Keep state immutable and flow direction explicit
- Preserve camelCase naming in code and JSON
- Use direct JSON success responses and simple error shapes
- Reuse shared Kotlin DTOs where client and backend models are intentionally identical

**Pattern Enforcement:**

- Verify patterns during code review and story validation
- Record any deviations in the architecture document before implementation proceeds
- Update patterns only by explicit architectural decision, not by local convenience

### Pattern Examples

**Good Examples:**
- `householdId`, `expenseId`, `memberName`
- `/households/123/expenses`
- `ExpenseListScreen`, `settlementStatus`, `SyncState`
- Feature folder plus shared domain layer
- Inline validation error plus global sync banner

**Anti-Patterns:**
- Mixed naming styles in the same module
- Feature logic spread across unrelated folders
- Mutable shared state hidden behind UI components
- Wrapped success envelopes without a clear reason
- Global loading overlays for simple form validation errors

## Project Structure & Boundaries

### Complete Project Directory Structure

```text
opensplit/
├── README.md
├── .gitignore
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradle/
│   └── wrapper/
├── .github/
│   └── workflows/
│       └── ci.yml
├── client/
│   ├── build.gradle.kts
│   ├── src/
│   │   ├── commonMain/
│   │   │   └── kotlin/
│   │   │       ├── app/
│   │   │       ├── features/
│   │   │       │   ├── auth/
│   │   │       │   ├── households/
│   │   │       │   ├── expenses/
│   │   │       │   ├── balances/
│   │   │       │   └── settlements/
│   │   │       ├── core/
│   │   │       │   ├── ui/
│   │   │       │   ├── state/
│   │   │       │   └── error/
│   │   │       └── navigation/
│   │   ├── androidMain/
│   │   ├── iosMain/
│   │   └── webMain/
│   └── src/commonTest/
├── server/
│   ├── build.gradle.kts
│   ├── src/
│   │   ├── main/
│   │   │   ├── kotlin/
│   │   │   │   ├── app/
│   │   │   │   ├── features/
│   │   │   │   │   ├── auth/
│   │   │   │   │   ├── households/
│   │   │   │   │   ├── expenses/
│   │   │   │   │   ├── balances/
│   │   │   │   │   └── settlements/
│   │   │   │   ├── core/
│   │   │   │   │   ├── auth/
│   │   │   │   │   ├── error/
│   │   │   │   │   └── persistence/
│   │   │   │   └── routes/
│   │   │   └── resources/
│   │   └── test/
│   │       └── kotlin/
│   └── docker/
│       ├── Dockerfile
│       └── docker-compose.yml
└── shared/
    ├── build.gradle.kts
    └── src/
        ├── commonMain/
        │   └── kotlin/
        │       ├── dto/
        │       ├── domain/
        │       ├── validation/
        │       └── support/
        └── commonTest/
            └── kotlin/
```

### Architectural Boundaries

**API Boundaries:**
The server owns REST endpoints for auth, households, expenses, balances, settlements, and sync. The client consumes those endpoints and should not duplicate server rules. Shared DTOs define the contract shape for both sides.

**Component Boundaries:**
The client owns UI composition, Decompose navigation, and presentation state. The shared module owns DTOs, domain models, validation, and shared calculation logic. The server owns request handling, authorization checks, persistence, and sync orchestration.

**Service Boundaries:**
Feature code stays within its own directory, but common cross-feature concerns live in shared core layers. Auth, household access, and sync logic must not leak into unrelated features.

**Data Boundaries:**
Shared DTOs define the transport layer, while shared domain models define business meaning. The server persists canonical state; the client keeps local state for offline use and sync recovery.

### Requirements to Structure Mapping

**Feature/Epic Mapping:**
- Account and auth flows → `client/features/auth`, `server/features/auth`, `shared/dto`
- Household management → `client/features/households`, `server/features/households`, `shared/domain`
- Expense management → `client/features/expenses`, `server/features/expenses`, `shared/domain`
- Balances and settlements → `client/features/balances`, `client/features/settlements`, `server/features/balances`, `server/features/settlements`
- Offline sync → `client/core/state`, `client/core/error`, `server/core/persistence`, `server/routes`

**Cross-Cutting Concerns:**
- Shared DTOs and validation → `shared/dto`, `shared/validation`
- State and error handling → `client/core/state`, `client/core/error`
- Persistence and sync → `server/core/persistence`, `server/features/*`
- Shared calculation logic → `shared/domain`

### Integration Points

**Internal Communication:**
Client features communicate through shared state and Decompose navigation. Shared core layers provide DTOs, domain logic, and validation. Server features communicate through REST routes and service layers.

**External Integrations:**
Auth providers, hosting, and any future third-party services are isolated behind server-side adapters so the client stays focused on the product domain.

**Data Flow:**
User actions update local client state first. The client sends REST requests to the server, which validates, authorizes, and persists canonical data. Shared DTOs keep request and response models aligned. Sync reconciles local and canonical state after reconnect.

### File Organization Patterns

**Configuration Files:**
Keep root build, Gradle, and CI configuration at the repository root. Place server deployment files under `server/docker` and shared module configuration under `shared`.

**Source Organization:**
Organize by feature first, with shared core layers underneath. Keep navigation, state, and UI concerns on the client; keep routes, persistence, and auth on the server; keep DTOs and business rules in shared.

**Test Organization:**
Keep unit tests near their modules, with integration tests centralized under each module's test source set. Use shared tests for shared DTO and calculation behavior.

**Asset Organization:**
Keep static assets in the relevant client target folders. Keep server resources limited to runtime config and templates if needed.

### Development Workflow Integration

**Development Server Structure:**
Use separate client and server run targets with the shared module compiled into both. The client can run against a local server or containerized backend during development.

**Build Process Structure:**
The root build coordinates all modules. Shared code must compile before client and server packaging. CI should validate shared, client, and server modules together.

**Deployment Structure:**
The server deploys through Docker Compose on a VPS. The client ships as platform-specific builds with shared Kotlin logic packaged into each target.

## Architecture Validation Results

### Coherence Validation ✅

**Decision Compatibility:**
The Kotlin Multiplatform starter, shared UI, Decompose-based state flow, REST API, local-first sync model, and VPS deployment all fit together without direct conflicts.

**Pattern Consistency:**
Naming, structure, format, state, error handling, and shared DTO patterns all support the same implementation model.

**Structure Alignment:**
The project tree supports client/server/shared separation, shared DTO reuse, feature-first organization, and offline sync boundaries.

### Requirements Coverage Validation ✅

**Functional Requirements Coverage:**
The architecture supports auth, households, expenses, balances, settlements, offline sync, and web/mobile access.

**Non-Functional Requirements Coverage:**
Performance, reliability, privacy, accessibility, and offline resilience are covered at the architectural level.

### Implementation Readiness Validation ✅

**Decision Completeness:**
Critical decisions are documented, including starter, data, auth, API, frontend, deployment, and shared DTOs.

**Structure Completeness:**
The project structure is specific and aligned to the chosen stack.

**Pattern Completeness:**
Naming, structure, data format, state, error handling, and loading patterns are defined.

### Gap Analysis Results

**Important Gaps:**
- Exact database choice and schema migration approach are still open.
- Exact auth session/token implementation is not yet pinned down.
- CI pipeline details and observability specifics remain for implementation.

### Validation Issues Addressed

No blocking issues remained after aligning shared DTOs with the Kotlin client and server and cleaning duplicate structure content.

### Architecture Completeness Checklist

**Requirements Analysis**

- [x] Project context thoroughly analyzed
- [x] Scale and complexity assessed
- [x] Technical constraints identified
- [x] Cross-cutting concerns mapped

**Architectural Decisions**

- [x] Critical decisions documented with versions
- [x] Technology stack fully specified
- [x] Integration patterns defined
- [x] Performance considerations addressed

**Implementation Patterns**

- [x] Naming conventions established
- [x] Structure patterns defined
- [x] Communication patterns specified
- [x] Process patterns documented

**Project Structure**

- [x] Complete directory structure defined
- [x] Component boundaries established
- [x] Integration points mapped
- [x] Requirements to structure mapping complete

### Architecture Readiness Assessment

**Overall Status:** READY FOR IMPLEMENTATION

**Confidence Level:** high

**Key Strengths:**
- Shared Kotlin foundation reduces client/server drift
- Offline-first model fits the product promise
- Clear feature, shared, and server boundaries
- Decompose and immutable state rules are explicit

**Areas for Future Enhancement:**
- Database and migration specifics
- Auth provider expansion for the mixed approach
- CI, monitoring, and backup hardening

### Implementation Handoff

**AI Agent Guidelines:**

- Follow all architectural decisions exactly as documented
- Use implementation patterns consistently across all components
- Respect project structure and boundaries
- Refer to this document for all architectural questions

**First Implementation Priority:**
Initialize the Kotlin Multiplatform project with shared UI enabled and the `client`, `server`, and `shared` module split.
