# 🏦 Banking Management System

A full-stack banking app I built to learn Spring Boot and React. You can create accounts, deposit/withdraw money, and transfer funds between accounts!

## What's Inside 📚

**Core Features:**
- Create bank accounts with initial balance
- Deposit money into accounts
- Withdraw money (with balance check)
- Transfer money between accounts
- View transaction history
- Error handling (insufficient balance, invalid amounts, etc.)

## Tech I Used 🔧

**Backend:**
- Spring Boot 4.0 (Java)
- MySQL Database
- JPA/Hibernate (for database queries)
- Maven (build tool)

**Frontend:**
- React 19 
- Vite (super fast!)
- React Router

## How to Run It 🚀

### Backend Setup

1. Clone the repo:
```bash
git clone https://github.com/GaganBabu1/Bank.git
cd banking
```

2. Create a MySQL database:
```sql
CREATE DATABASE bankdb;
```

3. Update database details in `src/main/resources/application.properties`:
```properties
spring.datasource.url=jdbc:mysql://localhost:3306/bankdb
spring.datasource.username=root
spring.datasource.password=your_password
```

4. Run backend:
```bash
./mvnw spring-boot:run
```
Runs on: http://localhost:8080

### Frontend Setup

1. Go to frontend folder:
```bash
cd ../banking-frontend/banking-frontend
```

2. Install dependencies:
```bash
npm install
```

3. Start dev server:
```bash
npm run dev
```
Runs on: http://localhost:5173

## What I Learned 💡

- How to structure a backend with Controllers → Services → Repositories
- Building REST APIs and connecting frontend to backend
- Database design and relationships
- Exception handling and input validation
- Full-stack development workflow

## API Endpoints 📡

```
POST   /accounts                  → Create new account
GET    /accounts/{id}             → Get account details
POST   /accounts/deposit          → Deposit money
POST   /accounts/withdraw         → Withdraw money
POST   /accounts/transfer         → Transfer between accounts
GET    /accounts/{id}/transactions → See transaction history
```

## Project Structure 📂

```
banking/
├── controller/    → API endpoints
├── services/      → Business logic
├── repository/    → Database queries
├── entity/        → Database models
├── dto/           → Request/Response objects
└── exception/     → Custom error handling

banking-frontend/
├── components/    → React pages
├── App.jsx
└── main.jsx
```

## What's Next? 🎯

Planning to add:
- User login with JWT tokens
- AI to detect suspicious transactions
- Unit tests
- API documentation
- Docker setup for easy deployment

## Author

**Gagan Babu** | Fresher | Learning Full-Stack Development
- GitHub: [@GaganBabu1](https://github.com/GaganBabu1)

---

Feel free to check it out and let me know if you have suggestions! 😊
