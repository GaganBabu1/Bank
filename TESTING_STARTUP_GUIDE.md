# Banking Management System - E2E Testing & Startup Guide

## 🚀 Quick Start

### Prerequisites
- **Java 21** installed (`java -version`)
- **Node.js 18+** installed (`node -v`)
- **MySQL 8.0+** running on localhost:3306
- Database `bankdb` created with user `root` (or configured in `application.properties`)

### Step 1: Start MySQL & Create Database

```bash
# If MySQL not running, start it (Windows)
net start MySQL80

# Or on Mac/Linux
brew services start mysql
```

Connect to MySQL and create database:
```sql
mysql -u root -p

-- Inside MySQL:
CREATE DATABASE IF NOT EXISTS bankdb;
USE bankdb;
SHOW TABLES;  -- Should be empty on first run
```

### Step 2: Start Backend (Spring Boot)

```bash
cd c:\Users\gagan\Downloads\banking

# Option A: Using mvnw (Maven wrapper)
mvnw.cmd spring-boot:run

# Option B: Manual build and run
mvnw.cmd clean package
java -jar target/banking-0.0.1-SNAPSHOT.jar
```

**Expected output:**
```
...
2026-06-15 10:30:00 INFO  o.s.b.w.e.t.TomcatWebServer - Tomcat started on port(s): 8080
2026-06-15 10:30:01 INFO  c.e.b.BankingApplication - Started BankingApplication
```

**Verify backend is running:**
- Open http://localhost:8080/swagger-ui.html
- Should see Swagger UI with all API endpoints
- Tables created in MySQL: `user`, `bank_account`, `transaction`

### Step 3: Start Frontend (React + Vite)

```bash
cd c:\Users\gagan\Downloads\banking\banking-frontend\banking-frontend

# Install dependencies (first time only)
npm install

# Start development server
npm run dev
```

**Expected output:**
```
  Local:   http://localhost:5173/
  press h + enter to show help
```

**Verify frontend is running:**
- Open http://localhost:5173
- Should see Banking System login page
- No console errors in DevTools

---

## ✅ E2E Testing Workflow

### Phase 1: Authentication (15 mins)

#### 1.1 Register New User
```
URL: http://localhost:5173/register
Fill form:
  First Name: John
  Last Name: Doe
  Email: john.doe@test.com
  Phone: +1-234-567-8900
  Address: 123 Main St, City
  Password: Test@Pass123
  Confirm: Test@Pass123
Click: Create Account
Verify:
  ✓ Green success toast appears
  ✓ Redirects to dashboard
  ✓ Header shows "Welcome, John"
  ✓ localStorage has accessToken, refreshToken, user
```

#### 1.2 Test Validation
```
Try: Empty email, click outside field
Verify: Red error "Email is required"

Try: Invalid email "notanemail"
Verify: Error "Invalid email format"

Try: Password "short"
Verify: Error "Password must be at least 8 characters"

Try: Different passwords in password fields
Verify: Error "Passwords do not match"
```

#### 1.3 Logout & Login
```
Click: User menu (top right) → Logout
Verify: Redirects to login, localStorage cleared

URL: http://localhost:5173/login
Fill: john.doe@test.com / Test@Pass123
Click: Login
Verify:
  ✓ Success toast
  ✓ Dashboard loads
  ✓ Tokens refreshed in localStorage
```

#### 1.4 Test Protected Routes
```
Logout again
URL: http://localhost:5173/accounts
Verify: Redirects to /login

URL: http://localhost:5173/create-account
Verify: Redirects to /login

Try accessing /admin/dashboard
Verify: Redirects (non-admin role)
```

### Phase 2: Banking Operations (20 mins)

#### 2.1 Create Account
```
URL: http://localhost:5173/create-account
Verify: Account holder name auto-filled "John Doe"

Fill:
  Account Name: Savings Account
  Initial Balance: 50000
Click: Create Account
Verify:
  ✓ Success toast
  ✓ Account number displayed (16 digits)
  ✓ Balance shows ₹50,000
  ✓ Status shows ACTIVE

Click: Go to Dashboard
Verify: Account visible in dashboard with balance
```

#### 2.2 Create Second Account
```
Navigate: /create-account
Fill:
  Account Name: Checking Account
  Initial Balance: 25000
Submit
Verify: Second account created, balance ₹25,000
```

#### 2.3 Deposit
```
URL: http://localhost:5173/deposit
Select: Savings Account dropdown
Fill: Amount 10000
Click: Deposit
Verify:
  ✓ Success toast: "Deposited ₹10,000"
  ✓ Dashboard shows updated balance: ₹60,000
  ✓ Transaction appears in history

Try: Deposit with empty amount
Verify: Error "Amount is required"

Try: Deposit "abc"
Verify: Error "Amount must be a valid number"
```

#### 2.4 Withdraw
```
URL: http://localhost:5173/withdraw
Select: Savings Account
Fill: Amount 20000
Click: Withdraw
Verify:
  ✓ Success toast
  ✓ Balance updated: ₹60,000 - ₹20,000 = ₹40,000
  ✓ Transaction in history shows type: WITHDRAW

Try: Withdraw 50000 (more than balance 40000)
Verify: Error "Insufficient balance"
```

#### 2.5 Transfer
```
URL: http://localhost:5173/transfer
Select: From Account = Savings (₹40,000)
Select: To Account = Checking (₹25,000)
Fill: Amount 10000
Click: Transfer
Verify:
  ✓ Success toast: "Transferred ₹10,000"
  ✓ Savings balance: ₹30,000
  ✓ Checking balance: ₹35,000

Check both accounts' transaction history:
  ✓ Savings shows TRANSFER_OUT
  ✓ Checking shows TRANSFER_IN

Try: Transfer to same account
Verify: Error or validation rejection

Try: Transfer 50000 (exceeds daily limit ₹1,00,000 or balance)
Verify: Appropriate error message
```

#### 2.6 Transaction History
```
URL: http://localhost:5173/history
Select: Savings Account
Verify: Table shows:
  ✓ All transactions (Deposit, Withdraw, Transfer)
  ✓ Columns: Date, Ref, Type, Status, From, To, Amount, Balance After
  ✓ Loader spinner appears while loading
  ✓ Transactions sorted by date (newest first)

Test Filters:
  Scope: "Incoming" → shows only DEPOSIT, TRANSFER_IN
  Scope: "Outgoing" → shows only WITHDRAW, TRANSFER_OUT
  Type: "Deposit" → shows only deposit transactions
  Search: Enter part of transaction ref → filters results

Test Pagination:
  Page Size: Change from 10 to 20
  Verify: More transactions displayed
  Click: Next page button
  Verify: Shows next batch of transactions
  
Try: Empty account
Verify: "No transactions found" message
```

### Phase 3: Profile Management (5 mins)

#### 3.1 View Profile
```
URL: http://localhost:5173/profile
Verify: Hero section shows:
  ✓ User avatar (initials)
  ✓ Name: John Doe
  ✓ Email: john.doe@test.com
  ✓ Member since: 2026

Verify: Details section shows:
  ✓ Phone: +1-234-567-8900
  ✓ Address: 123 Main St, City
  ✓ Account Status: Active
```

#### 3.2 Edit Profile
```
Click: Edit Profile button
Verify: Form loads with current data

Change:
  First Name: Johnny
  Phone: +1-555-666-7777
  
Click: Save
Verify:
  ✓ Success toast: "Profile updated"
  ✓ Header shows "Welcome, Johnny" immediately
  ✓ Form closes, back to view mode

Refresh page
Verify: Changes persisted (still shows Johnny)

Click: Edit again, Click Cancel
Verify: Reverts to view mode without saving changes
```

### Phase 4: Admin Features (10 mins)
*Requires admin role - skip if not available*

#### 4.1 Admin Access
```
Note: Admin features require role: "ADMIN"
Test approach:
  1. Create user with ADMIN role via backend (if available)
  2. Or logout, login as admin user (if pre-created)
  3. Or manually set role in localStorage (testing only)
```

#### 4.2 Admin Dashboard
```
URL: http://localhost:5173/admin/dashboard
Verify: Tabs visible: Dashboard, Users, Accounts

Dashboard Tab:
  ✓ Total Users: X
  ✓ Total Accounts: Y
  ✓ Active Accounts: Z
  ✓ Frozen Accounts: 0
  ✓ Total Active Balance: ₹XXXXX
  ✓ Loader spinner while loading

Users Tab:
  ✓ Table with Name, Email, Phone, Role, Status
  ✓ John Doe visible with role USER
  ✓ Admin users show role ADMIN

Accounts Tab:
  ✓ Table with Account #, Holder, Balance, Status, Created
  ✓ Both created accounts visible
  ✓ Status shows ACTIVE
```

#### 4.3 Non-Admin Blocked
```
Logout
Login as regular user (John)
Try: URL /admin/dashboard directly
Verify: Redirects to login or shows "Access Denied"

Verify: No "Admin" link in sidebar
```

### Phase 5: Error Handling & UX (5 mins)

#### 5.1 Network Errors
```
Open DevTools → Network tab → Throttle to Offline
Try: Create account
Verify: Error toast with network error message

Reconnect (or remove offline)
Try: Operation again
Verify: Works successfully
```

#### 5.2 Validation Feedback
```
Try: Each form without filling required fields
Verify:
  ✓ Red error text appears below field
  ✓ Error color matches design (danger red)
  ✓ Clear, helpful error messages
  ✓ Toast also shows if form submitted

Form Success:
  ✓ Valid submit shows green toast
  ✓ Toast auto-dismisses after 4 seconds
  ✓ Manual close (×) button works
```

#### 5.3 Loading States
```
Fill: Large transaction history page
Verify: Spinner + "Loading transaction history..." appears
Verify: Action buttons disabled during load

Fill: Create account
Verify: Button text changes "Creating Account..."
Verify: Form disabled during submission
```

#### 5.4 Responsive Design
```
Browser: Resize to 375px (mobile)
Verify:
  ✓ Sidebar hidden or collapsed
  ✓ Content full width
  ✓ Buttons/fields touchable
  ✓ Text readable

Resize to 1024px (tablet)
Verify:
  ✓ Grid layouts adapt
  ✓ Table scrolls horizontally if needed
  ✓ Layout readable

Resize to 1920px (desktop)
Verify:
  ✓ Multi-column layouts optimal
  ✓ No excessive white space
```

### Phase 6: DevTools Verification (5 mins)

#### 6.1 Network Requests
```
DevTools → Network tab
Perform: User registration
Verify:
  ✓ POST /api/auth/register (201 Created)
  ✓ Response has accessToken, refreshToken
  
Perform: Login
Verify:
  ✓ POST /api/auth/login (200 OK)
  ✓ Response headers include auth tokens

Perform: Create account
Verify:
  ✓ POST /api/accounts/create (201 Created)
  ✓ Authorization header: "Bearer <accessToken>"
  
Browse pages
Verify:
  ✓ GET /api/accounts/user/{id} (200 OK)
  ✓ GET /api/accounts/{id}/transactions?page=0&size=10 (200 OK)
```

#### 6.2 Console Errors
```
DevTools → Console tab
Perform: All major operations (register, login, create, deposit, withdraw, transfer, history, profile)
Verify:
  ✓ NO React errors logged
  ✓ NO unhandled promise rejections
  ✓ NO 404s for assets
  ✓ Only expected logs (if any)

If errors appear:
  Log the error details
  Check browser compatibility
  Report issue with exact console output
```

#### 6.3 LocalStorage
```
DevTools → Application → LocalStorage → http://localhost:5173
After login, verify:
  ✓ accessToken: JWT format (3 parts with .)
  ✓ refreshToken: JWT format
  ✓ user: JSON with {id, firstName, email, role, ...}

Example user object:
  {
    "id": 1,
    "firstName": "John",
    "lastName": "Doe",
    "email": "john.doe@test.com",
    "role": "USER",
    ...
  }

After logout, verify:
  ✓ All three keys removed
```

---

## 🐛 Issue Logging Template

If you find issues during testing:

```
**Issue:** [Title]
**Component:** [Component name]
**Steps to Reproduce:**
1. [First step]
2. [Second step]
3. ...

**Expected Behavior:** [What should happen]
**Actual Behavior:** [What actually happens]
**Screenshots/Console Error:** [Paste any errors]
**Severity:** [Critical/High/Medium/Low]
```

---

## ✅ Test Summary Checklist

Once complete, mark items:

### Authentication
- [ ] Registration works with validation
- [ ] Login generates tokens
- [ ] Tokens stored in localStorage
- [ ] Token refresh works on 401
- [ ] Protected routes redirect unauthenticated users
- [ ] Logout clears tokens

### Banking Operations
- [ ] Create account with validation
- [ ] Deposit increases balance
- [ ] Withdraw decreases balance & blocks overdraft
- [ ] Transfer moves funds & enforces daily limit
- [ ] Transaction history displays correctly
- [ ] Pagination & filtering work

### User Profile
- [ ] Profile view shows all data
- [ ] Edit profile updates data
- [ ] Changes persist after refresh
- [ ] Header reflects name changes

### Admin (if applicable)
- [ ] Admin users can access dashboard
- [ ] Non-admin blocked from admin routes
- [ ] Admin stats display correctly
- [ ] User/account tables load

### UX/UI
- [ ] Form validation shows clear errors
- [ ] Toast notifications appear/dismiss
- [ ] Loading states visible
- [ ] Responsive at all breakpoints
- [ ] No console errors
- [ ] Network requests correct

### Overall
- [ ] All tests passed
- [ ] System ready for deployment
- [ ] Issues logged (if any)

---

## 📝 Notes

- **JWT Token Expiry Testing:** Access tokens expire in 15 mins. To test refresh:
  - Wait 15 mins, or
  - Manually delete accessToken from localStorage
  - Make a request (interceptor should refresh automatically)

- **Database Reset:** To start fresh:
  ```sql
  DROP DATABASE bankdb;
  CREATE DATABASE bankdb;
  ```
  Then restart backend (Hibernate will recreate tables)

- **Backend Logs:** Check Spring Boot logs for transaction details:
  ```
  DEBUG com.example.banking - [log output]
  ```

- **Performance:** If slow, check:
  - MySQL indexing
  - Network tab for slow requests
  - Browser DevTools Performance tab
