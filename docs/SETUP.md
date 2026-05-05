# Setup

## Database

Create the local MySQL database:

```sql
CREATE DATABASE qr_attendance_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

Schema tables are generated from JPA entities using `spring.jpa.hibernate.ddl-auto=update`.

On startup, the backend seeds demo users, 25 named students, multiple named faculty, eight courses, faculty-course assignments, and student enrollments. The seed is idempotent and updates old generic demo names without creating duplicate rows.

## Environment

Set a strong JWT secret:

```bash
export JWT_SECRET='replace-with-at-least-32-random-characters'
```

Optional MySQL overrides:

```bash
export DB_USERNAME='root'
export DB_PASSWORD='your-mysql-password'
export DB_URL='jdbc:mysql://localhost:3306/qr_attendance_db?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC'
```

## Backend

```bash
cd backend
mvn clean test
mvn spring-boot:run
```

## Frontend

```bash
cd frontend
npm install
npm audit
npm run dev
```
