# Engineering Decision Log

This file records meaningful decisions made while changing the project. Each entry explains what was decided, why it was chosen, and what tradeoff was accepted.

Use simple language. Add an entry whenever a change affects architecture, security, data, API behavior, user experience, testing, or maintainability. Do not add entries for mechanical edits such as formatting or typo-only fixes.

## Entry Format

```text
## YYYY-MM-DD - Short decision title

Decision:
Reason:
Pattern:
Tradeoff:
Impact:
```

- **Decision:** What was chosen.
- **Reason:** Why it was needed or useful.
- **Pattern:** Why this approach fits the existing project.
- **Tradeoff:** What was accepted in exchange for the benefit.
- **Impact:** Which files, layers, users, or future work are affected.

## 2026-09-11 - Keep architecture documentation separate

Decision:
Create a dedicated `ARCHITECTURE.md` file instead of putting the full architecture description in `README.md`.

Reason:
The README should help people start and understand the project quickly. The architecture needs more detail and will change as features are added.

Pattern:
Keep onboarding documentation and system design documentation separate. Link the architecture document from the README so both remain easy to find.

Tradeoff:
There are two documents to maintain instead of one. This was accepted because each document has a clearer purpose and the architecture can grow without making the README difficult to scan.

Impact:
Future features must update `ARCHITECTURE.md` when they add or change layers, endpoints, data, security, frontend structure, or cross-cutting behavior.

## 2026-09-11 - Keep AI/ML planning separate from architecture

Decision:
Create a dedicated `AI_ML_PLAN.md` file for the brainstormed AI/ML features.

Reason:
The ideas are proposals, not implemented system behavior. Mixing them into the current architecture would make planned work look like existing functionality.

Pattern:
Keep current-state documentation separate from future-state planning. Mark proposals clearly until they are selected and implemented.

Tradeoff:
The plan must be kept in sync with the architecture after implementation. This was accepted because it prevents unimplemented features from being mistaken for supported behavior.

Impact:
AI/ML ideas remain proposed until a feature is explicitly selected. When implementation begins, the architecture and this decision log must be updated with the actual design.

## 2026-09-11 - Prefer a staged AI/ML rollout

Decision:
Recommend starting with the Money Health Score, Predictive Low-Balance Warning, and Explainable Transaction Anomaly Score with an Admin Attention Queue.

Reason:
These features use data the system already stores and provide value to both users and administrators without requiring a complex model at the beginning.

Pattern:
Start with small, explainable, read-only insights before introducing external models or automated actions into a banking workflow.

Tradeoff:
Rule-based or simple scoring may be less powerful than a trained model. This was accepted because the first version is easier to explain, test, secure, and replace later.

Impact:
The plan does not include automatic transaction blocking or account freezing. Any future score must show contributing reasons and must fail safely when there is not enough history.

## 2026-09-11 - Do not implement AI/ML during planning

Decision:
Record the AI/ML concepts as proposed only and do not add backend or frontend implementation yet.

Reason:
The request at this stage was to capture the brainstormed plan, not to start building a feature before its scope and contract are selected.

Pattern:
Separate planning from implementation. Confirm scope, data, API, security, UI, scoring approach, and tests before writing feature code.

Tradeoff:
No immediate working AI/ML feature is available. This was accepted to avoid silently choosing requirements or adding code that may not match the intended plan.

Impact:
The next AI/ML implementation must begin with an explicit selection from `AI_ML_PLAN.md` and must update `ARCHITECTURE.md` and this file as the design becomes real.

## Future Entries

For every meaningful code change, append a dated entry with the decision, simple reasoning, chosen project pattern, accepted tradeoff, and affected areas. Keep entries factual and do not record assumptions as decisions.

## 2026-09-11 - Add repository-wide development instructions

Decision:
Create `instructions.md` as the project-wide reference for metadata, commands, testing, coding standards, allowed actions, and disallowed actions.

Reason:
Future changes need one clear set of rules based on the versions and workflows that are actually present in the repository.

Pattern:
Keep operational guidance in a dedicated root document and link it from the README, while keeping architecture, execution flow, planning, and decision history in their own documents.

Tradeoff:
There is another document to maintain. This was accepted because separating responsibilities makes the rules easier to find and reduces the chance of mixing current architecture with workflow policy.

Impact:
Future work must use the documented Maven, npm, lint, build, and test commands; follow the existing layered architecture; protect secrets and schema boundaries; and update the related project documents when behavior changes.

## 2026-09-11 - Validate Money Health with mock data first

Decision:
Implement the first Money Health Score as a fixed, read-only mock endpoint before connecting live account or transaction data.

Reason:
This tests the API shape, frontend flow, authentication, authorization, and failure handling without risking real financial data or prematurely choosing scoring rules.

Pattern:
Keep the insight in `AiInsightService`, expose it through `InsightController`, protect `/api/insights/**` in `SecurityConfig`, and consume it through the central frontend API client.

Tradeoff:
The score is not personalized yet and provides no real financial analysis. This was accepted because validating the security and execution path is the explicit prerequisite for live-data implementation.

Impact:
The mock endpoint returns score `78`, status `HEALTHY`, and two reasons. It does not use repositories or mutate banking data. The backend suite includes four focused security cases, all passing.

## 2026-09-16 - Repair partial mock seed without duplicating data

Decision:
Allow the mock-data profile to reuse an existing mock user and create the account and transactions when that user has no accounts.

Reason:
An earlier startup can create the user before failing during account creation, leaving a partial fixture that the original idempotence check would never repair.

Pattern:
Look up the known mock email, inspect its accounts, and stop only when an account already exists.

Tradeoff:
The seeder performs one additional account lookup on startup when the profile is enabled. This was accepted to make local fixture setup recoverable and idempotent.

Impact:
The mock fixture can now recover from a user-only partial seed without creating duplicate accounts or transactions. Focused seeder tests pass.

## 2026-09-16 - Wire login authentication to persisted users

Decision:
Add a repository-backed `UserDetailsService` to `SecurityConfig` so Spring Security can load users by email and verify their BCrypt passwords.

Reason:
The mock user existed in the database, but login failed because `AuthenticationManager` had no database user lookup provider.

Pattern:
Reuse `UserRepository`, the existing `PasswordEncoder`, and the existing role, enabled, and locked fields when building Spring Security user details.

Tradeoff:
The security configuration now depends directly on `UserRepository`. This was accepted because authentication is the boundary that owns user lookup, and it fixes login without duplicating user data or changing the user entity.

Impact:
Persisted users can authenticate and receive JWT access and refresh tokens. The direct login integration test passes.

## 2026-09-11 - Keep insight failures non-blocking for the dashboard

Decision:
Allow the dashboard to continue loading accounts and transactions if the mock insight request fails.

Reason:
The insight is supplementary and should not prevent the core banking dashboard from working.

Pattern:
Use a separate guarded insight request in `Dashboard.jsx` and keep the existing account-loading path independent.

Tradeoff:
The user may see the dashboard without the insight when the endpoint is unavailable. This was accepted because account access is more important than an optional insight.

Impact:
The frontend production build passes. The repository's full lint command still reports pre-existing errors in unrelated files and one pre-existing React hook rule violation in `Dashboard.jsx`.

## 2026-09-16 - Connect Money Health to live user data

Decision:
Replace the fixed Money Health response with a read-only score calculated from the authenticated user's accounts and transactions.

Reason:
The mock security and flow tests passed, so the next planned step was to validate the feature against the application's real persisted data path.

Pattern:
Use the JWT user ID in `InsightController`, load accounts through `BankAccountRepository`, load each account's transactions through `TransactionRepository`, and keep scoring inside `AiInsightService`.

Tradeoff:
The first live version uses simple rule-based scoring and may query transactions once per account. This was accepted because the rules are explainable and the existing repository boundaries are reused; query optimization can be considered after real usage data exists.

Impact:
The endpoint is now personalized, remains read-only, and does not call an external AI provider. It returns a safe `NEEDS_ATTENTION` result when the user has no active account or transaction history. Unit and security tests cover the new path.

## 2026-09-16 - Build the first insight UI with explicit mock data

Decision:
Use `insightAPI.getMockMyFinancialHealth()` for the Dashboard while validating the frontend, and keep `getMyFinancialHealth()` available for the live-data switch.

Reason:
The UI can be tested with a stable response without depending on backend availability or changing real financial data.

Pattern:
Keep both mock and live methods in the central API service, and make the Dashboard's current choice explicit at the call site.

Tradeoff:
The Dashboard is not yet displaying the user's real score. This was accepted because the current phase is frontend mock-data validation, and the live switch remains small and visible.

Impact:
The existing Dashboard now displays the mock score and reasons. The production build passes. The changed API file passes focused lint; the Dashboard retains a pre-existing React hook lint error that is outside this mock-data change.

## 2026-09-16 - Switch the Dashboard to live Money Health data

Decision:
Change the Dashboard to call `insightAPI.getMyFinancialHealth()` instead of `getMockMyFinancialHealth()`.

Reason:
The mock UI, live backend scoring, authentication, authorization, and scoring tests passed.

Pattern:
Keep the Dashboard connected through the central API client and reuse the existing authenticated Axios interceptor.

Tradeoff:
The Dashboard now depends on the backend and database being available. This was accepted because the live-data phase is complete and the existing component already handles insight-request failure without blocking account loading.

Impact:
The user Dashboard displays the authenticated user's live Money Health Score. The mock method remains available for isolated frontend testing but is no longer used by the production Dashboard.

## 2026-09-16 - Add an opt-in mock banking user

Decision:
Add a `mock-data` Spring profile that seeds one known user, funded account, and representative transaction history only when explicitly enabled.

Reason:
Feature testing needs predictable financial data, but normal startup must not insert test records into a user's database.

Pattern:
Use a profile-gated `CommandLineRunner` in `MockDataSeeder`, reuse the existing repositories and entities, and make the seed idempotent by checking the mock email first.

Tradeoff:
The mock credentials are intentionally known and unsuitable for production. This was accepted because the profile is opt-in and exists only for local testing.

Impact:
Starting with the `mock-data` profile creates `mock.user@banking.local` / `Mock@12345`, a `47,000` active account, and deposit, withdrawal, incoming-transfer, and outgoing-transfer records. Normal startup is unchanged.
