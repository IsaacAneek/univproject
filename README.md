# University Management System (Spring Boot + PostgreSQL + Docker)

A University Management System built with **Java Spring Boot**, **Spring Security (RBAC + JWT)**, **Spring Data JPA**, **PostgreSQL**, and **Docker Compose**, featuring a **Monkeytype** dark aesthetic.

---

## 1. Authentication & JWT / JWS

The application supports **Stateless JWT (JSON Web Token) / JWS** authentication for REST API clients alongside standard session/cookie authentication for web browser views.

### JWT Flow:
1. Client logs in with credentials:
   ```bash
   POST /api/auth/login
   Content-Type: application/json

   {
     "username": "admin",
     "password": "admin123"
   }
   ```
2. Server validates credentials against PostgreSQL (using BCrypt) and responds with a signed JWT:
   ```json
   {
     "token": "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsInJvbGUiOiJST0xFX0FETUlOIiwiZXhwIjoxNzYyMDAwMDAwfQ...",
     "type": "Bearer",
     "id": 1,
     "username": "admin",
     "role": "ROLE_ADMIN"
   }
   ```
3. Client includes the JWT on all subsequent API requests in the `Authorization` header:
   ```bash
   GET /api/teachers
   Authorization: Bearer <TOKEN>
   ```
4. `JwtAuthenticationFilter` intercepts the request, validates the cryptographic signature, extracts the user and roles, and loads them into Spring Security's `SecurityContext`.

---

## 2. Role-Based Access Control (RBAC) & Permissions

- **Administrator (ROLE_ADMIN)**:
  - Can add, edit, view, and remove **Admins** (`/api/admins/**`).
  - Can add, edit, view, and remove **Teachers** (`/api/teachers/**`).
  - Can add, edit, view, and remove **Students** (`/api/students/**`).
- **Teacher (ROLE_TEACHER)**:
  - Can view and manage **Teachers** (`/api/teachers/**`).
  - Can add, edit, view, and remove **Students** (`/api/students/**`).
  - Forbidden from accessing administrator endpoints (`/api/admins/**`).
- **Student (ROLE_STUDENT)**:
  - Can view and manage **Students** (`/api/students/**`).
  - Forbidden from accessing teacher and administrator endpoints.

---

## 3. Seed Accounts

The system automatically initializes default accounts:
- **Admin**: `admin` / `admin123` (Full Name: System Administrator, Designation: Chief Systems Administrator, ID: ADM-001)
- **Teacher**: `teacher` / `teacher123` (Full Name: Dr. Sarah Connor, Designation: Associate Professor, ID: TCH-001)
- **Student**: `student` / `student123` (Full Name: John Doe, Designation: Undergraduate Student, ID: STU-001)

---

## 4. UI Theme

- Built using the **Monkeytype** color palette:
  - Primary Background: `#323437`
  - Cards & Modals: `#2c2e31`
  - Accent / Primary Buttons: `#e2b714`
  - Main Text: `#d1d0c5`
  - Sub / Label Text: `#646669`
  - Danger / Delete: `#ca4754`
- Completely free of emojis across all pages (`/login`, `/dashboard`, `/admin/panel`).

---

## 5. Dockerization & Deployment

### Build jar:
```powershell
.\mvnw.cmd package -DskipTests
```

### Start services with Docker Compose:
```powershell
docker compose up --build
```

### Run Automated Tests:
```powershell
.\mvnw.cmd test
```
