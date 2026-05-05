# API Reference

Base URL: `http://localhost:8080/api`

Use `Authorization: Bearer <jwt>` for all endpoints except login.

## Auth

`POST /auth/login`

```json
{ "email": "admin@example.com", "password": "Admin@12345" }
```

Returns a JWT and current user DTO.

`GET /auth/me`

Returns the authenticated user DTO.

## Admin

`POST /admin/students`

```json
{ "name": "Student One", "email": "s1@example.com", "password": "Student@12345", "rollNumber": "S001" }
```

`GET /admin/students`

`POST /admin/faculty`

```json
{ "name": "Faculty One", "email": "f1@example.com", "password": "Faculty@12345", "employeeCode": "F001" }
```

`GET /admin/faculty`

`POST /admin/courses`

```json
{ "name": "Computer Networks", "code": "CN101" }
```

`GET /admin/courses`

`POST /admin/courses/{courseId}/faculty/{facultyId}`

`POST /admin/enrollments`

```json
{ "studentId": 3, "courseId": 1 }
```

## Faculty

`GET /faculty/courses`

Returns courses assigned to the authenticated faculty user.

`POST /faculty/attendance-sessions`

```json
{
  "courseId": 1,
  "title": "Lecture 1",
  "startsAt": "2026-05-05T04:00:00Z",
  "endsAt": "2026-05-05T05:00:00Z"
}
```

`GET /faculty/attendance-sessions`

`POST /faculty/attendance-sessions/{sessionId}/qr`

Returns the raw QR token once plus expiry time.

`GET /faculty/attendance-sessions/{sessionId}/records`

`GET /faculty/attendance-sessions/courses/{courseId}/summary?month=2026-05`

Returns enrolled students for the course, sessions created by the faculty in the selected month, per-session present/absent cells, present count, total sessions, and monthly attendance percentage.

## Student

`POST /student/attendance/scan`

```json
{ "token": "raw-token-from-faculty", "deviceHash": "optional-device-fingerprint" }
```

`GET /student/attendance/my`
