# SplitEasy

An expense-sharing app (Splitwise-style) with a **debt simplification algorithm** that settles group balances using the minimum number of payments.

**Live demo:** https://spliteasy-pied.vercel.app
**API docs (Swagger):** https://spliteasy-pranav919191.onrender.com/swagger-ui/index.html

> The backend runs on a free hosting tier and sleeps when idle, so the first request can take 30-60 seconds.

## Features

- JWT authentication with BCrypt password hashing
- Create groups and add members by email
- Add expenses with three split types: **Equal**, **Exact** and **Percentage**
- Exact money handling with `BigDecimal` (for example 100 / 3 = 33.34 + 33.33 + 33.33, never loses a paisa)
- Per-member net balances
- **Debt simplification:** a greedy algorithm using two max-heaps, O(n log n), at most n-1 payments
- Authorization: only group members can view or modify a group
- Swagger/OpenAPI documentation with JWT support
- Unit tests with JUnit 5 and Mockito

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 17, Spring Boot, Spring Security, Spring Data JPA |
| Auth | JWT (jjwt), BCrypt |
| Database | MySQL (Aiven), H2 for tests |
| Frontend | React (Vite), React Router, Axios |
| DevOps | Docker, Render, Vercel |
| Testing | JUnit 5, Mockito |

## Architecture

```
React (Vercel)  -->  Spring Boot REST API (Render, Docker)  -->  MySQL (Aiven)
                      Controller -> Service -> Repository
```

## Debt Simplification

1. Compute each member's net balance (amount paid minus share owed).
2. Put creditors and debtors into two max-heaps.
3. Repeatedly match the largest creditor with the largest debtor and settle `min(a, b)`.
4. Push any remainder back into its heap. Each step fully settles at least one person, so there are at most n-1 transfers.

Greedy gives very good results in practice but is not guaranteed to be globally minimal (that problem is NP-hard).

## API Overview

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/auth/register` | Create an account |
| POST | `/api/auth/login` | Log in and receive a JWT |
| GET | `/api/users/me` | Current user profile |
| POST / GET | `/api/groups` | Create / list your groups |
| GET | `/api/groups/{id}` | Group details |
| POST | `/api/groups/{id}/members` | Add a member by email |
| POST / GET | `/api/groups/{id}/expenses` | Add / list expenses |
| GET | `/api/groups/{id}/balances` | Net balance per member |
| GET | `/api/groups/{id}/settlements` | Simplified payments |

## Run Locally

**Prerequisites:** JDK 17, Node.js 18+, MySQL (optional, H2 is the default).

```bash
# Backend (H2, no setup needed)
cd backend
./mvnw spring-boot:run

# Backend with MySQL: create a database named spliteasy, then add
# backend/src/main/resources/application-local.properties containing
# spring.datasource.password=YOUR_PASSWORD
./mvnw spring-boot:run "-Dspring-boot.run.profiles=mysql,local"

# Frontend
cd frontend
npm install
npm run dev
```

Frontend: http://localhost:5173, Backend: http://localhost:8080, Swagger: http://localhost:8080/swagger-ui.html

## Run Tests

```bash
cd backend
./mvnw test
```

## Deployment

- **Backend:** Docker image built from `backend/Dockerfile`, deployed on Render. Configured through environment variables: `SPRING_PROFILES_ACTIVE`, `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`, `JWT_SECRET`, `FRONTEND_URL`.
- **Frontend:** Vercel, with `VITE_API_URL` pointing to the backend.
- **Database:** managed MySQL on Aiven.

## Author

**Pranav**, GitHub: [@PRANAV151515](https://github.com/PRANAV151515)

