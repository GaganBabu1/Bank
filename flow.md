# Banking System Execution Flow

## Purpose

This document describes where execution starts, the order in which the current system runs, and the main function-to-function call paths. It must be updated whenever a feature changes an entry point, execution order, service call, API contract, security boundary, or AI/ML behavior.

The current document describes the existing system. Planned AI/ML features are not included in the execution paths until they are implemented.

## Application Entry Points

There are two independently started applications.

### Backend entry point

```text
banking/src/main/java/com/example/banking/BankingApplication.java
  BankingApplication.main(String[] args)
    -> SpringApplication.run(BankingApplication.class, args)
    -> Spring Boot starts the application context
    -> Security, controllers, services, repositories, and configuration are registered
    -> Embedded web server listens on port 8080
```

When the optional `mock-data` profile is active, startup also runs:

```text
BankingApplication.main()
  -> Spring Boot loads MockDataSeeder
  -> MockDataInitializer.run()
  -> check mock.user@banking.local
  -> if absent, create user, account, and four transactions
  -> if present, do nothing
```

### Frontend entry point

```text
banking-frontend/banking-frontend/src/main.jsx
  document.getElementById('root')
    -> createRoot(...).render(...)
    -> StrictMode
    -> App()
    -> BrowserRouter
    -> AuthProvider
    -> NotificationProvider
    -> Layout
    -> ToastContainer
    -> Routes
```

`App.jsx` selects the page component based on the browser path. `ProtectedRoute` checks authentication and, for admin paths, the required role before rendering the page.

## Request Execution Order

For an authenticated frontend request, the normal order is:

```text
User interaction
  -> React page/component event handler
  -> API method in services/api.js
  -> Axios request interceptor
  -> Authorization: Bearer <access token>
  -> HTTP request to Spring Boot
  -> SecurityConfig filter chain
  -> JwtAuthenticationFilter
  -> Controller endpoint
  -> Service method
  -> Repository method
  -> Database
  -> Entity result
  -> DTO or response body
  -> Axios response
  -> React component state update
  -> UI render
```

For a rejected request, execution stops at the relevant validation or security boundary and returns an error response. The frontend then displays the error using the page state or notification/toast flow.

## Authentication Flow

### Application startup session restoration

```text
main.jsx
  -> App()
  -> AuthProvider()
  -> useEffect()
  -> localStorage.getItem('user')
  -> localStorage.getItem('accessToken')
  -> JSON.parse(storedUser)
  -> normalizeUser(userData)
  -> setUser(normalized)
  -> setIsAuthenticated(true)
  -> setLoading(false)
  -> ProtectedRoute renders the requested protected page
```

If stored data is invalid, `AuthProvider` removes the stored user and tokens and leaves the session unauthenticated.

### Login flow

```text
Login component
  -> useAuth().login(email, password)
  -> authAPI.login({ email, password })
  -> api.post('/auth/login', data)
  -> Axios request interceptor
  -> POST /api/auth/login
  -> AuthController login endpoint
  -> AuthenticationManager
  -> UserDetailsService
  -> UserRepository.findByEmail(email)
  -> BCrypt password verification
  -> authentication service/security logic
  -> AuthResponse
  -> AuthContext.persistSession(response.data)
  -> localStorage.setItem(accessToken)
  -> localStorage.setItem(refreshToken)
  -> localStorage.setItem(user)
  -> setUser(normalized)
  -> setIsAuthenticated(true)
  -> Login component navigates to the application
```

### Registration flow

```text
Register component
  -> useAuth().register(data)
  -> authAPI.register(data)
  -> api.post('/auth/register', data)
  -> POST /api/auth/register
  -> AuthController registration endpoint
  -> user service/repository persistence
  -> AuthResponse
  -> AuthContext.persistSession(response.data)
  -> session tokens and normalized user are stored
  -> authenticated application is rendered
```

### Token refresh flow

```text
Any API request
  -> api response interceptor receives HTTP 401
  -> check originalRequest._retry
  -> read refreshToken from localStorage
  -> axios.post('/api/auth/refresh', { refreshToken })
  -> store new accessToken and refreshToken
  -> retry original request with new Bearer token
```

If refresh fails, the interceptor removes the local session and redirects to `/login`.

## User Banking Flows

### Create account

```text
CreateAccount component
  -> accountAPI.createAccount(data)
  -> api.post('/accounts', data)
  -> BankAccountController.createAccount(request, authentication)
  -> read userId from authentication.getDetails()
  -> AccountService.createAccount(request, userId)
  -> UserRepository.findById(userId)
  -> create BankAccount entity
  -> BankAccountRepository.save(account)
  -> AccountService.toResponse(account)
  -> BankAccountResponse
  -> HTTP 201 response
  -> CreateAccount updates UI and notification state
```

### Deposit

```text
Deposit component
  -> accountAPI.deposit(data)
  -> api.post('/accounts/deposit', data)
  -> BankAccountController.deposit(request)
  -> TransactionService.deposit(accountNumber, amount)
  -> validate amount
  -> BankAccountRepository.findById(accountNumber)
  -> validate account status
  -> update balance and updatedAt
  -> BankAccountRepository.save(account)
  -> create successful Transaction entity
  -> TransactionRepository.save(transaction)
  -> TransactionService.toResponse(account)
  -> BankAccountResponse
  -> Deposit updates UI and notification state
```

### Withdraw

```text
Withdraw component
  -> accountAPI.withdraw(data)
  -> api.post('/accounts/withdraw', data)
  -> BankAccountController.withdraw(request)
  -> TransactionService.withdraw(accountNumber, amount)
  -> validate amount
  -> BankAccountRepository.findById(accountNumber)
  -> validate account status and available balance
  -> update balance and updatedAt
  -> BankAccountRepository.save(account)
  -> create successful Transaction entity
  -> TransactionRepository.save(transaction)
  -> TransactionService.toResponse(account)
  -> BankAccountResponse
  -> Withdraw updates UI and notification state
```

### Transfer

```text
Transfer component
  -> accountAPI.transfer(data)
  -> api.post('/accounts/transfer', data)
  -> BankAccountController.transfer(request)
  -> TransactionService.transfer(fromAccount, toAccount, amount)
  -> validate different accounts and positive amount
  -> BankAccountRepository.findById(sender)
  -> BankAccountRepository.findById(receiver)
  -> validate both account statuses
  -> validate sender balance
  -> validate sender daily transfer limit
  -> update sender balance and dailyTransferUsed
  -> update receiver balance
  -> BankAccountRepository.save(sender)
  -> BankAccountRepository.save(receiver)
  -> create outgoing Transaction entity
  -> TransactionRepository.save(outgoingTransaction)
  -> create incoming Transaction entity
  -> TransactionRepository.save(incomingTransaction)
  -> TransactionService.toResponse(sender)
  -> BankAccountResponse
  -> Transfer updates UI and notification state
```

### View transaction history

```text
TransactionHistory component
  -> transactionAPI.getTransactions(accountId, page, size)
  -> api.get('/accounts/{id}/transactions?...')
  -> BankAccountController.getTransactions(id, page, size)
  -> TransactionService.getAccountTransactions(accountNumber, page, size)
  -> TransactionRepository.findTransactionsByAccount(accountNumber, pageable)
  -> map each Transaction through TransactionService.toTransactionDTO()
  -> paginated TransactionDTO response
  -> TransactionHistory renders the result
```

Incoming and outgoing history follow the same path through `getIncomingTransactions()` or `getOutgoingTransactions()` and their corresponding repository query.

### View one transaction

```text
TransactionHistory component
  -> transactionAPI.getTransactionDetails(transactionRef)
  -> api.get('/accounts/transaction/{transactionRef}')
  -> BankAccountController.getTransactionDetails(transactionRef)
  -> TransactionHistoryService.getTransactionByRef(transactionRef)
  -> TransactionRepository.findByTransactionRef(transactionRef)
  -> TransactionHistoryService.toDTO(transaction)
  -> TransactionDTO response
  -> TransactionHistory renders transaction details
```

## Admin Flows

All admin paths follow this first:

```text
Admin component
  -> adminAPI or accountAPI method
  -> Axios JWT interceptor
  -> SecurityConfig requires ROLE_ADMIN for /api/admin/**
  -> JwtAuthenticationFilter validates the token
  -> AdminController method
```

### Admin dashboard statistics

```text
AdminDashboard component
  -> adminAPI.getStatistics()
  -> api.get('/admin/statistics')
  -> AdminController.getStatistics()
  -> AdminService.getSystemStatistics()
  -> repositories calculate system values
  -> Map<String, Object> response
  -> AdminDashboard renders statistics
```

### Admin account state change

```text
AdminDashboard component
  -> accountAPI.freezeAccount(accountNumber, reason)
     or accountAPI.unfreezeAccount(accountNumber)
     or accountAPI.closeAccount(accountNumber)
  -> POST /api/admin/accounts/{accountNumber}/...
  -> AdminController method
  -> AccountService.freezeAccount()
     or AccountService.unfreezeAccount()
     or AccountService.closeAccount()
  -> BankAccountRepository.findById(accountNumber)
  -> validate requested state change
  -> update BankAccount
  -> BankAccountRepository.save(account)
  -> BankAccountResponse
  -> AdminDashboard refreshes account data
```

### Reset daily limits

```text
AdminDashboard component
  -> adminAPI.resetDailyLimits()
  -> api.post('/admin/reset-daily-limits')
  -> AdminController.resetDailyLimits()
  -> AccountService.resetDailyTransferLimit()
  -> BankAccountRepository.findAll()
  -> set dailyTransferUsed to zero for each account
  -> BankAccountRepository.saveAll(accounts)
  -> success response
  -> AdminDashboard shows notification
```

## Money Health Live Flow

The Money Health Score now reads the authenticated user's live accounts and transactions. It remains read-only and does not write any banking data.

```text
Dashboard component
  -> insightAPI.getMyFinancialHealth() when live data is enabled
  -> api.get('/insights/my-financial-health')
  -> Axios JWT request interceptor
  -> SecurityConfig allows only USER or ADMIN
  -> JwtAuthenticationFilter validates the bearer token
  -> InsightController.getMyFinancialHealth()
  -> AiInsightService.getMoneyHealth(userId)
  -> BankAccountRepository.findByUserId(userId)
  -> TransactionRepository.findTransactionsByAccount(accountNumber, Pageable.unpaged()) for each account
  -> deduplicate transactions and calculate balance, income, spending, withdrawals, and transfer-limit factors
  -> MoneyHealthResponse(score, status, reasons)
  -> Dashboard renders the score and reasons
```

The Dashboard now uses the live path above. The mock method remains in `services/api.js` only for repeatable frontend fallback testing and is not used by the production Dashboard.

Current AI/ML status:

- No AI/ML dependency or external model exists in `banking/pom.xml`.
- `AiInsightService` calculates a score from active balances, incoming and outgoing transaction types, withdrawal count, and average daily transfer-limit usage.
- `InsightController` exposes `GET /api/insights/my-financial-health`.
- The endpoint is read-only and queries only accounts returned for the authenticated JWT user ID.
- The frontend calls the endpoint through `insightAPI` and isolates insight failures from account loading.
- Authentication and authorization tests cover `USER`, `ADMIN`, unauthenticated, and disallowed-role requests.
- Service tests cover healthy scoring, at-risk scoring, and the no-history fallback.

When the first AI/ML feature is implemented, record:

```text
Feature:
Entry point:
Call order:
New or changed backend functions:
New or changed frontend functions:
Data used:
Security boundary:
Read-only or mutation behavior:
Fallback when the score/model fails:
Tests added:
```

AI/ML logic must be called from an authenticated and authorized backend path. It must not bypass the existing service layer or directly change balances, accounts, or transaction records.

## Update Rules

Update this file whenever a change affects any of the following:

- Application entry point or startup order
- Route-to-component behavior
- Component-to-API function calls
- Controller-to-service calls
- Service-to-repository calls
- Security filters, roles, or endpoint access
- Database reads, writes, or transaction boundaries
- AI/ML entry points, scoring, predictions, or model failures

For each update, document the old path if it was replaced, the new execution order, and the files/functions affected. Also update [ARCHITECTURE.md](ARCHITECTURE.md), [AI_ML_PLAN.md](AI_ML_PLAN.md), or [decision.d](decision.d) when the change affects their subject matter.

## Flow Change Log

| Date | Change | AI/ML impact |
|---|---|---|
| 2026-09-11 | Documented current backend and frontend entry points and major execution paths | None; no AI/ML code existed at documentation time |
| 2026-09-11 | Added the mock Money Health Score path from Dashboard through InsightController and AiInsightService | Mock-only; live data and model integration remain unimplemented |
| 2026-09-16 | Replaced the mock response with user-scoped live account and transaction scoring | Live repository data; no external model or financial mutation |
