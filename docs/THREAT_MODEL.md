# Threat Model

## Assets

- User credentials and password hashes.
- JWTs.
- Attendance records.
- QR token hashes.
- Audit logs.

## Main Threats

- Credential theft.
- JWT theft through XSS.
- Proxy attendance by sharing QR tokens.
- Duplicate scans.
- Scans by unenrolled students.
- Faculty creating sessions for courses they do not teach.
- Accidental exposure of password hashes or JPA internals.

## MVP Mitigations

- BCrypt password hashing.
- Short-lived JWTs.
- In-memory frontend JWT storage.
- Role-based authorization.
- DTO-only controller responses.
- Bean Validation for request bodies.
- Restricted CORS.
- Random short-lived QR tokens.
- SHA-256 storage of QR token values.
- Enrollment validation.
- Duplicate attendance unique constraint.
- IP address and optional device hash logging.
- Audit logs for important auth and attendance events.

## Residual Risk

QR-only attendance cannot fully prevent proxy attendance because a student can share a valid token with another authenticated enrolled student. Future versions can add dynamic QR refresh, geolocation, face/selfie verification, device attestation, and anomaly detection.
