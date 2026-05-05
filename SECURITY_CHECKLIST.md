# Security Checklist

## Dependencies Used

Backend:

- Spring Web: REST APIs.
- Spring Security: authentication and authorization.
- Spring Data JPA: parameterized repository access and MySQL persistence.
- MySQL Driver: local MySQL connectivity.
- Bean Validation: request validation.
- Lombok: reduces boilerplate in entities/services.
- JJWT: JWT creation and validation.
- Spring Boot Test and Spring Security Test: backend verification.

Frontend:

- React: UI.
- Vite: local development/build tooling.
- Axios: API client.
- qrcode: browser-side QR image generation from the short-lived raw token.

No QR scanner package is used in this MVP.

## Known Risks

- JWT is stored in `localStorage` so login survives browser refresh.
- `localStorage` improves MVP usability but XSS could expose JWTs. Keep dependencies minimal, avoid unsafe HTML rendering, and use short token expiry.
- `spring.jpa.hibernate.ddl-auto=update` is acceptable for this local MVP only, not production.
- QR-only attendance cannot fully prevent proxy attendance.
- IP address and device hash are weak signals and should be treated as audit hints, not proof of presence.

## Audit Commands

```bash
cd frontend
npm audit
```

```bash
cd backend
mvn test
```

## Manual Security Checklist

- Confirm `JWT_SECRET` is set and at least 32 random characters.
- Confirm CORS allows only `http://localhost:5173` and `http://127.0.0.1:5173`.
- Confirm backend rejects unauthenticated access to admin, faculty, and student endpoints.
- Confirm admin cannot view password hashes through API responses.
- Confirm faculty cannot create sessions for unassigned courses.
- Confirm raw QR token is returned only immediately after generation.
- Confirm expired QR token scans are rejected.
- Confirm duplicate attendance scans are rejected.
- Confirm unenrolled students cannot mark attendance.
- Confirm attendance records include IP address and optional device hash.
- Confirm audit logs are created for login success, login failure, QR generation, attendance marked, and attendance rejected.
- Confirm `npm audit` reports 0 vulnerabilities after installing frontend dependencies.
