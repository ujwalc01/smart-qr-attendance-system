package com.example.attendance.attendance;

import com.example.attendance.attendance.dto.AttendanceRecordResponse;
import com.example.attendance.attendance.dto.AttendanceSummaryResponse;
import com.example.attendance.attendance.dto.CreateSessionRequest;
import com.example.attendance.attendance.dto.QrTokenResponse;
import com.example.attendance.attendance.dto.ScanAttendanceRequest;
import com.example.attendance.attendance.dto.SessionResponse;
import com.example.attendance.audit.AuditService;
import com.example.attendance.common.exception.BadRequestException;
import com.example.attendance.common.exception.ConflictException;
import com.example.attendance.common.exception.ForbiddenException;
import com.example.attendance.common.exception.NotFoundException;
import com.example.attendance.course.Course;
import com.example.attendance.course.CourseFacultyRepository;
import com.example.attendance.course.CourseService;
import com.example.attendance.enrollment.Enrollment;
import com.example.attendance.enrollment.EnrollmentRepository;
import com.example.attendance.enrollment.EnrollmentStatus;
import com.example.attendance.user.AppUser;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AttendanceService {
    private final CourseService courseService;
    private final CourseFacultyRepository courseFacultyRepository;
    private final AttendanceSessionRepository sessionRepository;
    private final AttendanceQrTokenRepository tokenRepository;
    private final AttendanceRecordRepository recordRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final AuditService auditService;
    private final TokenHasher tokenHasher;

    @Value("${app.qr.expiration-minutes}")
    private long qrExpirationMinutes;

    @Transactional
    public SessionResponse createSession(CreateSessionRequest request, AppUser faculty) {
        Course course = courseService.requireCourse(request.courseId());
        if (!courseFacultyRepository.existsByCourseAndFaculty(course, faculty)) {
            throw new ForbiddenException("Faculty is not assigned to this course");
        }
        if (!request.endsAt().isAfter(request.startsAt())) {
            throw new BadRequestException("Session end time must be after start time");
        }
        AttendanceSession session = new AttendanceSession();
        session.setCourse(course);
        session.setFaculty(faculty);
        session.setTitle(request.title().trim());
        session.setStartsAt(request.startsAt());
        session.setEndsAt(request.endsAt());
        return SessionResponse.from(sessionRepository.save(session));
    }

    @Transactional(readOnly = true)
    public List<SessionResponse> facultySessions(AppUser faculty) {
        return sessionRepository.findByFacultyOrderByCreatedAtDesc(faculty).stream().map(SessionResponse::from).toList();
    }

    @Transactional
    public QrTokenResponse generateQr(Long sessionId, AppUser faculty, String ipAddress) {
        AttendanceSession session = requireSession(sessionId);
        if (!session.getFaculty().getId().equals(faculty.getId())) {
            throw new ForbiddenException("Faculty cannot manage this session");
        }
        String rawToken = tokenHasher.newRawToken();
        AttendanceQrToken qrToken = new AttendanceQrToken();
        qrToken.setSession(session);
        qrToken.setTokenHash(tokenHasher.hash(rawToken));
        qrToken.setExpiresAt(Instant.now().plusSeconds(qrExpirationMinutes * 60));
        AttendanceQrToken saved = tokenRepository.save(qrToken);
        auditService.log(faculty, "QR_GENERATED", "ATTENDANCE_QR_TOKEN", saved.getId(), "QR token generated", ipAddress);
        return new QrTokenResponse(rawToken, saved.getExpiresAt());
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> sessionRecords(Long sessionId, AppUser faculty) {
        AttendanceSession session = requireSession(sessionId);
        if (!session.getFaculty().getId().equals(faculty.getId())) {
            throw new ForbiddenException("Faculty cannot view this session");
        }
        return recordRepository.findBySessionOrderByScannedAtDesc(session).stream()
            .map(AttendanceRecordResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public AttendanceSummaryResponse courseAttendanceSummary(Long courseId, YearMonth month, AppUser faculty) {
        Course course = courseService.requireCourse(courseId);
        if (!courseFacultyRepository.existsByCourseAndFaculty(course, faculty)) {
            throw new ForbiddenException("Faculty is not assigned to this course");
        }

        Instant monthStart = month.atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        Instant nextMonthStart = month.plusMonths(1).atDay(1).atStartOfDay().toInstant(ZoneOffset.UTC);
        List<AttendanceSession> sessions = sessionRepository
            .findByFacultyAndCourseAndStartsAtGreaterThanEqualAndStartsAtLessThanOrderByStartsAtAsc(
                faculty,
                course,
                monthStart,
                nextMonthStart
            );
        List<AttendanceRecord> records = sessions.isEmpty() ? List.of() : recordRepository.findBySessionIn(sessions);
        Map<String, AttendanceRecord> recordByStudentAndSession = records.stream()
            .collect(Collectors.toMap(
                record -> record.getStudent().getId() + ":" + record.getSession().getId(),
                record -> record
            ));

        List<AttendanceSummaryResponse.SummarySession> summarySessions = sessions.stream()
            .map(session -> new AttendanceSummaryResponse.SummarySession(
                session.getId(),
                session.getTitle(),
                session.getStartsAt()
            ))
            .toList();

        List<Enrollment> enrollments = enrollmentRepository.findByCourseAndStatusOrderByStudent_NameAsc(course, EnrollmentStatus.ACTIVE);
        long totalSessions = sessions.size();
        List<AttendanceSummaryResponse.SummaryStudent> summaryStudents = enrollments.stream()
            .map(Enrollment::getStudent)
            .map(student -> {
                List<AttendanceSummaryResponse.SummaryCell> cells = sessions.stream()
                    .map(session -> {
                        AttendanceRecord record = recordByStudentAndSession.get(student.getId() + ":" + session.getId());
                        return new AttendanceSummaryResponse.SummaryCell(
                            session.getId(),
                            record != null,
                            record == null ? null : record.getScannedAt()
                        );
                    })
                    .toList();
                long presentCount = cells.stream().filter(AttendanceSummaryResponse.SummaryCell::present).count();
                double percentage = totalSessions == 0 ? 0.0 : Math.round((presentCount * 10000.0) / totalSessions) / 100.0;
                return new AttendanceSummaryResponse.SummaryStudent(
                    student.getId(),
                    student.getName(),
                    student.getEmail(),
                    student.getRollNumber(),
                    cells,
                    presentCount,
                    totalSessions,
                    percentage
                );
            })
            .toList();

        return new AttendanceSummaryResponse(
            course.getId(),
            course.getName(),
            month.toString(),
            summarySessions,
            summaryStudents
        );
    }

    @Transactional
    public AttendanceRecordResponse scan(ScanAttendanceRequest request, AppUser student, String ipAddress) {
        Instant now = Instant.now();
        String tokenHash = tokenHasher.hash(request.token().trim());
        AttendanceQrToken qrToken = tokenRepository.findByTokenHash(tokenHash)
            .orElseThrow(() -> reject(student, null, "QR token is invalid", ipAddress));
        AttendanceSession session = qrToken.getSession();

        if (qrToken.getExpiresAt().isBefore(now)) {
            throw reject(student, session.getId(), "QR token has expired", ipAddress);
        }
        if (session.getStatus() != SessionStatus.ACTIVE) {
            throw reject(student, session.getId(), "Attendance session is not active", ipAddress);
        }
        if (now.isBefore(session.getStartsAt()) || now.isAfter(session.getEndsAt())) {
            throw reject(student, session.getId(), "Attendance session is outside allowed time", ipAddress);
        }
        if (!enrollmentRepository.existsByStudentAndCourseAndStatus(student, session.getCourse(), EnrollmentStatus.ACTIVE)) {
            throw reject(student, session.getId(), "Student is not enrolled in this course", ipAddress);
        }
        if (recordRepository.existsBySessionAndStudent(session, student)) {
            throw reject(student, session.getId(), "Attendance is already marked", ipAddress);
        }

        AttendanceRecord record = new AttendanceRecord();
        record.setSession(session);
        record.setStudent(student);
        record.setScannedAt(now);
        record.setIpAddress(ipAddress);
        record.setDeviceHash(blankToNull(request.deviceHash()));
        AttendanceRecord saved = recordRepository.save(record);
        auditService.log(student, "ATTENDANCE_MARKED", "ATTENDANCE_RECORD", saved.getId(), "Attendance marked", ipAddress);
        return AttendanceRecordResponse.from(saved);
    }

    @Transactional(readOnly = true)
    public List<AttendanceRecordResponse> myAttendance(AppUser student) {
        return recordRepository.findByStudentOrderByScannedAtDesc(student).stream()
            .map(AttendanceRecordResponse::from)
            .toList();
    }

    private AttendanceSession requireSession(Long sessionId) {
        return sessionRepository.findById(sessionId).orElseThrow(() -> new NotFoundException("Attendance session not found"));
    }

    private ConflictException reject(AppUser student, Long sessionId, String reason, String ipAddress) {
        auditService.log(student, "ATTENDANCE_REJECTED", "ATTENDANCE_SESSION", sessionId, reason, ipAddress);
        return new ConflictException(reason);
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
