# Repository Instructions

These instructions apply to all future changes in this repository. Follow them together with [ARCHITECTURE.md](ARCHITECTURE.md), [flow.md](flow.md), [AI_ML_PLAN.md](AI_ML_PLAN.md), and [decision.md](decision.md).

## Core Metadata and Scope

### Project Overview

This repository contains a full-stack banking management system. Users can register, authenticate, create bank accounts, deposit, withdraw, transfer funds, view transaction history, and manage their profile. Administrators can view system data, manage accounts, freeze or close accounts, and reset daily transfer limits.

The backend is a Spring Boot REST API. The frontend is a React single-page application built with Vite. The system uses JWT authentication, role-based authorization, JPA persistence, MySQL in normal operation, and H2 for tests.

### Tech Stack

Versions are based on the current project files and the frontend lockfile.

Backend:

- Java 21
- Spring Boot 4.0.5
- Maven Wrapper 3.3.4
- Apache Maven 3.9.14 through the wrapper distribution
- Spring Web, Spring Security, Spring Data JPA, and Spring Validation, versions managed by Spring Boot 4.0.5
- JJWT 0.12.3
- MySQL Connector/J, version managed by Spring Boot 4.0.5
- Lombok, version managed by Spring Boot 4.0.5
- Springdoc OpenAPI 2.0.2
- H2 for tests, version managed by Spring Boot 4.0.5

Frontend:

- Node.js 20 in CI
- React 19.2.5
- React DOM 19.2.5
- React Router DOM 7.14.1
- Axios 1.18.0
- Vite 8.0.9
- ESLint 9.39.4
- eslint-plugin-react-hooks 7.1.1
- eslint-plugin-react-refresh 0.5.2
- @eslint/js 9.39.4
- @vitejs/plugin-react 6.0.1

The frontend manifest uses caret ranges, but `package-lock.json` is the source of the currently resolved frontend versions. Do not upgrade dependencies casually.

### File Scope

These instructions apply to:

- Backend Java, resource, test, Maven, and configuration files under `banking/`
- Frontend JavaScript, JSX, CSS, HTML, Vite, ESLint, package, and lock files under `banking-frontend/banking-frontend/`
- Root documentation and workflow files that describe or verify the project

The main backend source scope is:

```text
banking/src/main/java/com/example/banking/
  config/       Spring, security, and OpenAPI configuration
  controller/   REST endpoints
  dto/          API request and response contracts
  entity/       JPA persistence models
  exception/    Domain exceptions and global error handling
  repository/   Spring Data database access
  security/     JWT authentication components
  services/     Business rules and transaction boundaries
```

The main frontend source scope is:

```text
banking-frontend/banking-frontend/src/
  components/   Pages and reusable UI components
  context/      Authentication, notification, and toast state
  services/     Central API client
  utils/        Shared validation helpers
```

Generated output such as `target/`, `dist/`, and dependency directories must not be edited manually or committed.

## Commands and Workflow

Run commands from the directory stated in each section. On Windows, use `mvnw.cmd`.

### Backend Build and Run

From the repository root:

```powershell
.\banking\mvnw.cmd test
.\banking\mvnw.cmd clean package
.\banking\mvnw.cmd spring-boot:run
```

To start locally with the opt-in mock user and funded test account:

```powershell
.\banking\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=mock-data"
```

The `mock-data` profile creates `mock.user@banking.local` with password `Mock@12345`, a `47,000` active account, and representative deposit, withdrawal, incoming-transfer, and outgoing-transfer records. It is for local testing only and is not enabled by normal startup.

To run the packaged backend after a successful build:

```powershell
java -jar .\banking\target\banking-0.0.1-SNAPSHOT.jar
```

The backend normally runs at `http://localhost:8080`. A configured MySQL database is required for normal operation. Use the repository's test configuration for tests; do not put credentials in source files.

### Frontend Build and Run

From `banking-frontend/banking-frontend/`:

```powershell
npm install
npm run dev
npm run build
npm run preview
```

The frontend normally runs at `http://localhost:5173`.

### Test Instructions

Backend tests:

```powershell
cd banking
.\mvnw.cmd -B -DskipTests=false test
```

New backend business logic must include focused unit or integration tests for its success path, validation failures, authorization-sensitive behavior, and important error paths. Financial operations must test balance and transaction-history consistency.

The frontend currently has no test script or frontend test runner configured. Until one is added, new frontend behavior must be verified with `npm run lint`, `npm run build`, and the applicable manual or E2E scenario. Do not claim frontend automated coverage when no frontend test runner has been configured.

For new code, tests are required at the layer where behavior is introduced. Do not reduce existing coverage or remove tests to make a build pass. If a numeric coverage threshold is needed, add and document the coverage tool and threshold as a separate reviewed decision; the current project does not define a numeric threshold.

### Lint and Format

From `banking-frontend/banking-frontend/`:

```powershell
npm run lint
npm run build
```

ESLint configuration is in `eslint.config.js`. There is currently no repository formatter script. Preserve the existing formatting and import style; do not introduce a formatter or reformat unrelated files without a reviewed decision.

Before finishing any change, run:

```powershell
git diff --check
```

The CI baseline is:

- Backend: Java 21, Maven tests
- Frontend: Node 20, `npm install`, and `npm run build`

A change is not complete until the relevant checks pass or the failure is clearly reported.

## Standards and Guardrails

### Coding Style

- Use the existing layered pattern: Controller -> Service -> Repository.
- Keep controllers focused on HTTP input, authorization annotations, service calls, and response mapping.
- Put business rules and financial transaction boundaries in services.
- Keep repository code focused on persistence and queries.
- Use DTOs at API boundaries; do not expose JPA entities directly from controllers.
- Use the existing package names and Java naming conventions: `UpperCamelCase` for classes, `lowerCamelCase` for methods and variables, and descriptive names instead of one-letter variables.
- Keep financial mutations inside appropriate `@Transactional` service methods.
- Validate on the backend even when frontend validation exists.
- Use existing domain exceptions and `GlobalExceptionHandler` for expected failures.
- Add new Axios calls to the appropriate group in `src/services/api.js`; do not duplicate JWT or refresh-token handling in components.
- Use `AuthContext`, `NotificationContext`, and `ToastContext` for their existing responsibilities.
- Keep frontend route protection in `ProtectedRoute`, while treating backend authorization as authoritative.
- Preserve existing public APIs and response shapes unless the feature requires a documented contract change.
- Keep AI/ML logic explainable, independently testable, and outside direct balance/account mutation unless a reviewed decision explicitly changes that boundary.

### Documentation Updates

Every meaningful feature change must update the relevant documentation in the same change:

- `ARCHITECTURE.md`: structure, boundaries, security, data, or cross-cutting changes
- `flow.md`: entry points, execution order, or function call paths
- `AI_ML_PLAN.md`: AI/ML plan status, scope, order, or decisions
- `decision.md`: meaningful decisions, reasoning, patterns, and accepted tradeoffs

Do not mark planned behavior as implemented. Record what is actually present in the code.

### Allowed Actions

Safe autonomous work includes:

- Fixing typos, syntax errors, compiler errors, lint errors, and failing tests caused by the current change
- Adding focused tests for existing behavior or new behavior
- Adding small, scoped features that follow the documented architecture
- Adding or updating DTOs, service methods, controllers, repository queries, API methods, and UI components required by an approved feature
- Improving validation and consistent error handling
- Updating architecture, flow, plan, decision, and testing documentation to reflect actual changes
- Running the documented build, test, lint, and verification commands
- Refactoring a small touched area when behavior and public contracts remain unchanged

### Disallowed Actions

Do not do any of the following without explicit review and an entry in `decision.md`:

- Modify, delete, or invent database migrations or change the database schema
- Change account balances, transaction history, transfer limits, or account status outside the established service and transaction boundaries
- Bypass JWT authentication, role checks, backend authorization, or ownership checks
- Expose passwords, JWT secrets, database passwords, API keys, tokens, or other credentials
- Commit secrets to properties, JavaScript, Markdown, logs, or test fixtures
- Change production configuration to include a real credential
- Add an external AI/ML provider or send financial data outside the application without an explicit privacy, failure, security, and cost review
- Allow AI/ML output to automatically block transfers or freeze accounts without an approved design and tests
- Upgrade major language, framework, runtime, or dependency versions without review
- Replace the Controller -> Service -> Repository architecture with direct controller-to-repository access
- Expose JPA entities directly as API responses
- Add broad unrelated refactors or reformat unrelated files
- Remove tests, weaken validation, or disable lint rules to make verification pass
- Edit generated `target/`, `dist/`, or dependency files manually
- Commit or create a branch unless explicitly requested

## Completion Checklist

Before reporting a change as complete:

1. Confirm the change matches an approved requirement or plan item.
2. Follow the existing architecture and security boundaries.
3. Add focused tests for new backend behavior and the available frontend verification.
4. Run the relevant Maven, npm, lint, build, and `git diff --check` commands.
5. Review the diff for unrelated changes and secrets.
6. Update `ARCHITECTURE.md`, `flow.md`, `AI_ML_PLAN.md`, or `decision.md` when applicable.
7. Report any checks that could not be run and why.
