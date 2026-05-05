# End-to-End App Flow

Use this flow after starting both apps:

```bash
cd backend
export JWT_SECRET='replace-with-at-least-32-random-characters'
mvn spring-boot:run
```

```bash
cd frontend
npm run dev
```

Open `http://localhost:5173/` or `http://127.0.0.1:5173/`.

## Full Successful Example

This path uses new data created from the admin dashboard, then completes attendance as faculty and student.

### 1. Admin Point of View

Login:

- Email: `admin@example.com`
- Password: `Admin@12345`

Create records:

- Create student:
  - Name: `Rahul Malhotra`
  - Email: `rahul.malhotra@example.com`
  - Password: `Student@12345`
  - Roll number: `STU901`
- Create faculty:
  - Name: `Dr. Asha Raman`
  - Email: `asha.raman@example.com`
  - Password: `Faculty@12345`
  - Employee code: `FAC901`
- Create course:
  - Name: `Mobile Application Development`
  - Code: `CS901`
- Assign `Dr. Asha Raman` to `Mobile Application Development`.
- Enroll `Rahul Malhotra` in `Mobile Application Development`.

Logout when done.

### 2. Faculty Point of View

Login:

- Email: `asha.raman@example.com`
- Password: `Faculty@12345`

Create attendance session:

- Course: `CS901 - Mobile Application Development`
- Title: `Mobile Apps Week 1`
- Starts at: current time or a few minutes earlier
- Ends at: at least 30 minutes in the future

Generate QR token:

- Click `Generate QR Token` on the new session.
- The UI shows both a QR image and the raw token code.
- Copy the displayed raw token code for manual student submission.
- The raw token is shown only at generation time.

Logout when done.

### 3. Student Point of View

Login:

- Email: `rahul.malhotra@example.com`
- Password: `Student@12345`

Mark attendance:

- Paste the raw QR token into `Submit QR Token`.
- Optionally enter `device-rahul-laptop` as device hash.
- Click `Mark Attendance`.

Expected result:

- Success message: `Attendance marked`.
- The attendance appears in `My Attendance`.

### 4. Faculty Verification

Logout, then login again:

- Email: `asha.raman@example.com`
- Password: `Faculty@12345`

Verify:

- Open records for `Mobile Apps Week 1`.
- Confirm `Rahul Malhotra` appears with scanned time, IP address, and device hash.
- Open the `Course Attendance Register`.
- Select `CS901` and the current month.
- Confirm Rahul appears in the course roster, the session day column shows `P`, and the monthly percentage reflects the marked session.

## Seeded Fast Demo

You can also skip creating records and use seeded data:

- Faculty: `faculty@example.com` / Dr. Kavita Menon
- Student: `student@example.com` / Aarav Sharma
- Course: `CS201` Data Structures and Algorithms
- This faculty and student are already connected through seeded assignments/enrollments.

### 1. Faculty

Login:

- Email: `faculty@example.com`
- Password: `Faculty@12345`

Create attendance session:

- Course: `CS201 - Data Structures and Algorithms`
- Title: `Week 1 Attendance`
- Starts at: current time or a few minutes earlier
- Ends at: at least 30 minutes in the future

Generate QR token:

- Click `Generate QR Token` on the new session.
- Copy the displayed raw token.
- The raw token is shown only at generation time.

Copy the token.

### 2. Student

Logout, then login:

- Email: `student@example.com`
- Password: `Student@12345`

Mark attendance:

- Paste the raw QR token into `Submit QR Token`.
- Optionally enter a device hash such as `device-aarav-laptop`.
- Click `Mark Attendance`.

Expected result:

- Success message: `Attendance marked`.
- The attendance appears in `My Attendance`.

### 3. Faculty Verification

Logout, then login again as faculty:

- Email: `faculty@example.com`
- Password: `Faculty@12345`

Verify records:

- Open the session records.
- Confirm Aarav Sharma appears with scanned time, IP address, and optional device hash.

## Negative Checks

- Submit the same QR token twice as the same student: the second request should be rejected as duplicate attendance.
- Login as a student not enrolled in that course and submit the token: the request should be rejected.
- Wait until the QR token expires and submit it: the request should be rejected.
