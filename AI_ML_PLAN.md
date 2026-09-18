# AI/ML Feature Plan

## Status

This plan began as a proposal based only on the ideas discussed so far. The first feature was validated with mock data and now has a live-data implementation. External model integration is not implemented.

## Goals

- Add basic AI/ML-inspired functionality for both users and administrators.
- Keep the features distinctive compared with typical banking projects.
- Prefer explainable results over opaque predictions.
- Build incrementally on the existing users, accounts, balances, limits, and transactions.

## Recommended First Release

The first release should focus on these three features:

1. Personal Money Health Score
2. Predictive Low-Balance Warning
3. Explainable Transaction Anomaly Score with an Admin Attention Queue

These features provide functionality for both sides of the application without starting with a generic chatbot or a complex external model.

### Current First-Feature Status

The Personal Money Health Score has passed mock-data validation and now reads live user-scoped data:

- Endpoint: `GET /api/insights/my-financial-health`
- Access: authenticated `USER` or `ADMIN`
- Behavior: read-only calculated response
- Data source: authenticated user's accounts and transactions
- Live account and transaction data: connected through existing repositories
- External AI/ML model: not connected
- Security tests: passed for valid JWT, invalid JWT, allowed roles, unauthenticated, and disallowed roles
- Scoring tests: passed for healthy, at-risk, and no-history profiles
- Frontend UI: implemented on the existing user Dashboard
- Frontend live API wiring: active

The score is intentionally rule-based and explainable. It does not block transactions, freeze accounts, or modify financial records.

## User-Side Plans

### 1. Personal Money Health Score

Calculate a score from the user's available financial activity, such as:

- Income compared with spending
- Balance stability
- Transfer-limit usage
- Number of withdrawals
- Recent unusual activity
- Emergency balance coverage

Example result:

> Money Health: 78/100
> "Your spending is stable, but withdrawals increased 24% this month."

The score should include the factors that contributed to it.

### 2. Predictive Low-Balance Warning

Estimate whether the user's balance may become low soon, based on:

- Recent withdrawal average
- Recent transfer average
- Current balance
- Recurring transaction patterns
- Transfer-limit usage

Example result:

> "Based on your recent withdrawals, your balance may fall below 2,000 within 6 days."

The feature should handle insufficient transaction history safely.

### 3. Smart Transaction Explanation

Explain a transaction in simple language using its amount, direction, balance impact, and the user's recent activity.

Example result:

> "This transfer reduced your balance by 5,000. It represents 32% of your current balance and is higher than your recent average transfer."

### 4. User Spending Personality

Classify transaction behavior into understandable categories, such as:

- Saver
- Frequent spender
- Transfer-heavy
- Cash-dependent
- Balanced
- Irregular activity

Example result:

> "Your current pattern is: Balanced Planner."

The classification should be explainable and should not be presented as a financial diagnosis.

### 5. Smart Transaction Search

Allow a user to describe transaction filters in natural language, for example:

> "Show my large outgoing transfers this month."

The request could map to existing filters such as:

- Direction
- Amount
- Date range
- Transaction type
- Status

## Admin-Side Plans

### 6. Explainable Transaction Anomaly Score

Score a transaction using factors such as:

- Amount compared with the historical average
- New or unusual transaction pattern
- Multiple transfers in a short period
- Amount as a percentage of the current balance
- Sudden change in transaction direction
- Near-limit transfers

Example user-facing result:

> "This transfer looks unusual because it is 4.2 times higher than your normal transfer amount."

Example admin-facing result:

> "High-risk transaction: 87/100. Reasons: unusually large amount, rapid repeated transfers."

The initial version should flag transactions for review and should not automatically block transactions.

### 7. Admin Attention Queue

Rank accounts or transactions that may require administrator review, including:

- Suspicious transactions
- Repeated failed operations
- Rapid balance changes
- Frequent account freezing
- High transfer-limit usage
- Dormant accounts that suddenly become active
- Unusually large balance changes

Example table:

| Priority | Account | Reason | Score |
|---|---|---|---|
| High | Masked account ending 4821 | Unusual transfer pattern | 91 |
| Medium | Masked account ending 1190 | Multiple limit violations | 72 |

### 8. Admin Customer Segmentation

Group users into useful operational segments, such as:

- Mostly inactive accounts
- High-value accounts
- Growing balances
- High-risk accounts
- Users approaching transfer limits
- Users with declining activity

This should complement the existing administrative statistics rather than replace them.

## Proposed Implementation Shape

The previously discussed backend concepts were:

```text
services/
  AiInsightService.java
  RiskScoringService.java
  CashFlowPredictionService.java

controller/
  InsightController.java
```

The previously discussed endpoint concepts were:

```text
GET /api/insights/my-financial-health
GET /api/insights/low-balance-prediction
GET /api/insights/transaction/{id}/explanation
GET /api/admin/insights/attention-queue
GET /api/admin/insights/risk-summary
```

These names are proposals only. They must be confirmed before implementation.

## Proposed Build Order

1. Personal Money Health Score (live read-only implementation complete)
2. Predictive Low-Balance Warning
3. Explainable Transaction Anomaly Score
4. Admin Attention Queue
5. Smart Transaction Explanation
6. User Spending Personality
7. Admin Customer Segmentation
8. Smart Transaction Search

The order can change only after reviewing the selected feature's data requirements, API contract, security boundary, and tests.

## Guardrails

- Do not start with a generic banking chatbot.
- Do not automatically block transactions based on an initial score.
- Do not automatically freeze accounts based on an initial score.
- Do not expose one user's private financial information to another user.
- Do not treat a score as a final financial decision.
- Show reasons behind scores and classifications.
- Handle insufficient history explicitly.
- Keep financial mutations inside the existing backend service and transaction boundaries.

## Decision Required Before Implementation

For each feature, confirm:

- Feature name and final scope
- User side, admin side, or both
- Required data
- Read-only or mutation behavior
- API contract
- Security role
- UI location
- Scoring or prediction approach
- Test scenarios
- Whether an external model or provider is actually necessary

## Planning Rule

This file must be updated when a plan is selected, reordered, narrowed, expanded, implemented, deferred, or rejected. The architecture rules for implementation remain in [ARCHITECTURE.md](ARCHITECTURE.md).

## Plan Change Log

| Date | Change | Status |
|---|---|---|
| 2026-09-11 | Recorded the brainstormed user-side and admin-side AI/ML ideas and recommended first release | Proposed |
| 2026-09-11 | Implemented the first feature as a mock-only, authenticated Money Health Score slice | Mock implementation complete; live data pending |
| 2026-09-16 | Connected the Money Health Score to authenticated user accounts and transactions | Live read-only implementation complete |
| 2026-09-16 | Added the Money Health card using explicit frontend mock data | Mock UI complete; live UI switch pending |
| 2026-09-16 | Switched the Dashboard from the mock method to the live insight API | Live frontend integration complete |
