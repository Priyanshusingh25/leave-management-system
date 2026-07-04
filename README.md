# LeaveFlow - Employee Leave Management System

I built this full-stack leave management system with a Spring Boot backend and an Angular frontend. The application is designed to support employee leave requests, manager approvals, and secure role-based access while keeping deployment and schema management clean and traceable.

---

## Features

### Employee Features
- Login and authentication with JWT
- Apply for leave with date range and reason
- View personal leave history
- Edit or cancel pending leave requests
- Employee dashboard with leave statistics
- View profile details

### Manager Features
- View pending leave requests from team members
- Approve or reject leave requests
- View team leave history and statistics
- Manager dashboard with key metrics
- Search and filter leave records

### System Features
- Role-based access control for EMPLOYEE and MANAGER
- JWT-based stateless authentication
- REST API with Swagger/OpenAPI documentation
- PostgreSQL database with Flyway migrations
- Docker Compose orchestration for local deployment
- Angular frontend served by Nginx in production

---

## Project Structure

```
leave-management-system/
├── backend/
│   └── backend/                      # Spring Boot Maven project
│       ├── src/main/java/com/leaveflow/backend/
│       ├── src/main/resources/
│       │   ├── application.yml
│       │   └── db/migration/
│       ├── Dockerfile
│       ├── mvnw
│       ├── mvnw.cmd
│       └── pom.xml
├── frontend/                         # Angular frontend
│   ├── src/app/
│   ├── src/environments/
│   ├── package.json
│   ├── angular.json
│   ├── Dockerfile
│   └── nginx.conf
├── database/                         # Schema / database docs
├── docs/                             # Documentation files
├── postman/                          # Postman collection
├── docker-compose.yml                # Docker Compose config
├── .env.example                      # Local configuration template
└── README.md                         # Project documentation
```

---

## Installation & Setup

### Prerequisites
- Docker & Docker Compose (recommended)
- Node.js 20+ and npm
- Java 17+ and Maven 3.9+ (for local backend run)
- PostgreSQL 18+ (optional, when not using Docker)

### Option 1: Start with Docker Compose

```bash
cd leave-management-system
docker-compose up --build
```

Open after startup:
- Frontend: http://localhost:4200
- Backend API: http://localhost:8080
- Swagger UI: http://localhost:8080/swagger-ui.html

### Option 2: Run Backend Locally

```bash
cd backend/backend
./mvnw clean package
./mvnw spring-boot:run
```

### Option 3: Run Frontend Locally

```bash
cd frontend
npm install
npm start
```

The frontend uses `http://localhost:8080/api` as its backend API base URL.

---

## Environment Variables

The backend reads configuration from environment variables via `application.yml`.
Copy `.env.example` and update values for local development:

```env
SERVER_PORT=8080
DB_HOST=localhost
DB_PORT=5432
DB_NAME=leave_management
DB_USERNAME=postgres
DB_PASSWORD=ROOT
JWT_SECRET=your-super-secret-256-bit-key
JWT_ACCESS_EXPIRATION=3600000
JWT_REFRESH_EXPIRATION=604800000
CORS_ALLOWED_ORIGINS=http://localhost:4200
```

---

## Technology Stack

### Backend
- Spring Boot 3.5.16
- Java 21
- Spring Security + JWT (JJWT)
- PostgreSQL
- Flyway migrations
- SpringDoc OpenAPI / Swagger UI
- Maven

### Frontend
- Angular 19
- Angular Material
- RxJS
- Nginx (production frontend)
- npm

### Docker
- Docker Compose
- postgres:16-alpine
- eclipse-temurin:17-jre-alpine
- nginx:alpine

---

## API Documentation

### Swagger / OpenAPI
- Swagger UI: http://localhost:8080/swagger-ui.html
- OpenAPI JSON: http://localhost:8080/v3/api-docs

### Key Endpoints

#### Authentication
```
POST   /api/auth/login
POST   /api/auth/refresh
POST   /api/auth/logout
```

#### Employee Operations
```
GET    /api/employees/profile
GET    /api/employees/dashboard
POST   /api/employees/leaves/apply
GET    /api/employees/leaves
GET    /api/employees/leaves/{id}
PUT    /api/employees/leaves/{id}
DELETE /api/employees/leaves/{id}
```

#### Manager Operations
```
GET    /api/manager/dashboard
GET    /api/manager/pending-leaves
GET    /api/manager/team-leave-history
PUT    /api/manager/leaves/{id}/approve
PUT    /api/manager/leaves/{id}/reject
```

### Example Request

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"employee@leaveflow.com","password":"Password@123"}'
```

---

## Database Schema

The schema is defined in `backend/backend/src/main/resources/db/migration/V1__initial_schema.sql`.

### Employees Table
```sql
CREATE TABLE employees (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    department VARCHAR(100) NOT NULL,
    role VARCHAR(20) NOT NULL CHECK (role IN ('EMPLOYEE', 'MANAGER')),
    manager_id BIGINT REFERENCES employees(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);
```

### Leaves Table
```sql
CREATE TABLE leaves (
    id BIGSERIAL PRIMARY KEY,
    employee_id BIGINT NOT NULL REFERENCES employees(id) ON DELETE CASCADE,
    leave_type VARCHAR(30) NOT NULL CHECK (leave_type IN ('SICK', 'CASUAL', 'ANNUAL', 'UNPAID', 'MATERNITY', 'PATERNITY')),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    reason VARCHAR(1000) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED', 'CANCELLED')),
    manager_comments VARCHAR(1000),
    reviewed_by BIGINT REFERENCES employees(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT chk_leave_dates CHECK (end_date >= start_date)
);
```

**Indexes**:
- `idx_employees_email`
- `idx_employees_manager_id`
- `idx_leaves_employee_id`
- `idx_leaves_status`
- `idx_leaves_start_date`

---

## Sample Seed Data

Seeded accounts are defined in `backend/backend/src/main/resources/db/migration/V2__seed_data.sql`.

```
Manager:
  Email: manager@leaveflow.com
  Password: Password@123

Employee:
  Email: employee@leaveflow.com
  Password: Password@123

Employee:
  Email: sara@leaveflow.com
  Password: Password@123
```

---

## Notes

- The frontend builds into `dist/leaveflow` and is served by Nginx in production.
- The backend uses environment variables defined in `application.yml`.
- `docker-compose.yml` starts PostgreSQL, the Spring Boot backend, and the Angular frontend.

---

## Known Limitations

- No email notifications for leave status changes
- No leave balance tracking
- Single manager per employee
- No detailed audit trail for changes
- No real-time notifications

---

## Future Enhancements

- Add email notifications for approvals/rejections
- Track leave balances and carryover
- Support multi-approver workflows
- Add audit/history logging
- Improve mobile responsiveness and accessibility
