# Testing

## Backend

```bash
cd backend
mvn test
```

Covered scenarios:

- Login success.
- Invalid login.
- Expired QR rejection.
- Duplicate attendance rejection.
- Unenrolled student rejection.
- Successful attendance mark.

## Frontend

```bash
cd frontend
npm install
npm audit
npm run build
```

Manual checks:

- Login as admin, faculty, and student.
- Admin creates a course, assigns faculty, and enrolls a student.
- Faculty creates a session and generates a token.
- Student submits the token.
- Faculty views attendance records.
- Student views own attendance.

See `docs/E2E_FLOW.md` for a step-by-step app walkthrough from each role's point of view.
