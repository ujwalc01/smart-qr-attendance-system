# QR Smart Attendance System

Local-only MVP POC for QR-based smart attendance with Spring Boot, MySQL, and React.

## Prerequisites

- Java 21
- Maven
- Node.js and npm
- MySQL running locally

## MySQL Setup

```sql
CREATE DATABASE qr_attendance_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

The MVP uses JPA/Hibernate schema generation with `spring.jpa.hibernate.ddl-auto=update`. Flyway is intentionally not used.

## Backend Setup

Set a strong JWT secret before running:

```bash
export JWT_SECRET='replace-with-at-least-32-random-characters'
export DB_USERNAME='root'
export DB_PASSWORD='your-mysql-password'
```

Run:

```bash
cd backend
mvn clean test
mvn spring-boot:run
```

Backend runs at `http://localhost:8080`.

## Frontend Setup

```bash
cd frontend
npm install
npm audit
npm run dev
```

Frontend runs at `http://localhost:5173`.

## Demo Credentials

- Admin: `admin@example.com` / `Admin@12345`
- Faculty: `faculty@example.com` / `Faculty@12345`
- Student: `student@example.com` / `Student@12345`

Demo data is created by a startup seed runner using BCrypt hashing. Plain text passwords are not stored.

Seeded demo data:

- Admin: Priya Nair
- Faculty: Dr. Kavita Menon, Dr. Meera Iyer, Prof. Arjun Rao, Dr. Nisha Kapoor, Prof. Sandeep Kulkarni
- All faculty demo passwords: `Faculty@12345`
- Students: Aarav Sharma, Aditi Rao, Akshay Verma, Ananya Gupta, Arjun Nair, Diya Patel, Ishaan Mehta, Kavya Reddy, Kiran Bhat, Lakshmi Menon, Manav Joshi, Meera Kulkarni, Neha Singh, Nikhil Iyer, Pooja Shah, Pranav Krishnan, Riya Das, Rohan Kapoor, Saanvi Jain, Sahil Khan, Shreya Nair, Tanvi Agarwal, Varun Rao, Vikram Sinha, Zoya Fernandes
- Student emails: `student@example.com`, then `student002@example.com` through `student025@example.com`
- All student demo passwords: `Student@12345`
- Courses: `CS201` Data Structures and Algorithms, `CS202` Database Management Systems, `CS203` Operating Systems, `CS204` Computer Networks, `CS205` Software Engineering, `CS206` Web Application Security, `CS207` Artificial Intelligence Fundamentals, `CS208` Cloud Computing
- The seed runner assigns faculty to courses and enrolls each student in three courses.

For a complete admin, faculty, and student walkthrough, see [docs/E2E_FLOW.md](docs/E2E_FLOW.md).

## Security Notes

- Passwords are stored as BCrypt hashes.
- JWTs are short-lived. The frontend stores the current JWT in `localStorage` so login survives refresh; this is convenient for the MVP but means XSS could expose the token.
- CORS is restricted to `http://localhost:5173` and `http://127.0.0.1:5173`.
- Controllers use DTOs and do not expose JPA entities.
- QR tokens are random, short-lived, stored only as SHA-256 hashes, and returned raw only once on generation.
- Faculty QR generation displays both the raw token code and a QR image generated in the browser.
- Attendance validation is server-side and checks token validity, session time, active enrollment, duplicate attendance, and authenticated role.
- Faculty attendance register shows enrolled students, session/day columns, present/absent status, and monthly percentage per student.

## Known MVP Limitations

QR-only attendance cannot fully prevent proxy attendance. This MVP reduces abuse using authentication, enrollment validation, token expiry, duplicate prevention, IP logging, device hash, and audit logs. Future versions can add dynamic QR refresh, geolocation, face/selfie verification, and anomaly detection.
