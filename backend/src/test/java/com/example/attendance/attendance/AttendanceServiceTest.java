package com.example.attendance.attendance;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.attendance.attendance.dto.ScanAttendanceRequest;
import com.example.attendance.audit.AuditService;
import com.example.attendance.common.exception.ConflictException;
import com.example.attendance.course.Course;
import com.example.attendance.course.CourseFacultyRepository;
import com.example.attendance.course.CourseService;
import com.example.attendance.enrollment.EnrollmentRepository;
import com.example.attendance.enrollment.EnrollmentStatus;
import com.example.attendance.user.AppUser;
import com.example.attendance.user.Role;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceTest {
    @Mock
    CourseService courseService;
    @Mock
    CourseFacultyRepository courseFacultyRepository;
    @Mock
    AttendanceSessionRepository sessionRepository;
    @Mock
    AttendanceQrTokenRepository tokenRepository;
    @Mock
    AttendanceRecordRepository recordRepository;
    @Mock
    EnrollmentRepository enrollmentRepository;
    @Mock
    AuditService auditService;
    @Mock
    TokenHasher tokenHasher;
    @InjectMocks
    AttendanceService attendanceService;

    private AppUser student;
    private Course course;
    private AttendanceSession session;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(attendanceService, "qrExpirationMinutes", 5L);
        student = user(2L, Role.STUDENT);
        course = course();
        session = session(course);
        when(tokenHasher.hash("raw-token")).thenReturn("hashed-token");
    }

    @Test
    void expiredQrIsRejected() {
        when(tokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(token(Instant.now().minusSeconds(1))));

        assertThatThrownBy(() -> attendanceService.scan(new ScanAttendanceRequest("raw-token", "device"), student, "127.0.0.1"))
            .isInstanceOf(ConflictException.class)
            .hasMessage("QR token has expired");

        verify(auditService).log(student, "ATTENDANCE_REJECTED", "ATTENDANCE_SESSION", 10L, "QR token has expired", "127.0.0.1");
    }

    @Test
    void duplicateAttendanceIsRejected() {
        when(tokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(token(Instant.now().plusSeconds(300))));
        when(enrollmentRepository.existsByStudentAndCourseAndStatus(student, course, EnrollmentStatus.ACTIVE)).thenReturn(true);
        when(recordRepository.existsBySessionAndStudent(session, student)).thenReturn(true);

        assertThatThrownBy(() -> attendanceService.scan(new ScanAttendanceRequest("raw-token", "device"), student, "127.0.0.1"))
            .isInstanceOf(ConflictException.class)
            .hasMessage("Attendance is already marked");
    }

    @Test
    void unenrolledStudentIsRejected() {
        when(tokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(token(Instant.now().plusSeconds(300))));
        when(enrollmentRepository.existsByStudentAndCourseAndStatus(student, course, EnrollmentStatus.ACTIVE)).thenReturn(false);

        assertThatThrownBy(() -> attendanceService.scan(new ScanAttendanceRequest("raw-token", null), student, "127.0.0.1"))
            .isInstanceOf(ConflictException.class)
            .hasMessage("Student is not enrolled in this course");
    }

    @Test
    void successfulAttendanceMarkSavesRecord() {
        when(tokenRepository.findByTokenHash("hashed-token")).thenReturn(Optional.of(token(Instant.now().plusSeconds(300))));
        when(enrollmentRepository.existsByStudentAndCourseAndStatus(student, course, EnrollmentStatus.ACTIVE)).thenReturn(true);
        when(recordRepository.existsBySessionAndStudent(session, student)).thenReturn(false);
        when(recordRepository.save(any(AttendanceRecord.class))).thenAnswer(invocation -> {
            AttendanceRecord record = invocation.getArgument(0);
            record.setId(99L);
            return record;
        });

        var response = attendanceService.scan(new ScanAttendanceRequest("raw-token", "device"), student, "127.0.0.1");

        assertThat(response.id()).isEqualTo(99L);
        assertThat(response.status()).isEqualTo(AttendanceStatus.PRESENT);
        verify(auditService).log(student, "ATTENDANCE_MARKED", "ATTENDANCE_RECORD", 99L, "Attendance marked", "127.0.0.1");
    }

    private AttendanceQrToken token(Instant expiresAt) {
        AttendanceQrToken token = new AttendanceQrToken();
        token.setId(20L);
        token.setSession(session);
        token.setTokenHash("hashed-token");
        token.setExpiresAt(expiresAt);
        return token;
    }

    private AttendanceSession session(Course course) {
        AttendanceSession session = new AttendanceSession();
        session.setId(10L);
        session.setCourse(course);
        session.setFaculty(user(3L, Role.FACULTY));
        session.setTitle("Lecture");
        session.setStartsAt(Instant.now().minusSeconds(60));
        session.setEndsAt(Instant.now().plusSeconds(600));
        session.setStatus(SessionStatus.ACTIVE);
        return session;
    }

    private Course course() {
        Course course = new Course();
        course.setId(5L);
        course.setName("Security");
        course.setCode("SEC101");
        return course;
    }

    private AppUser user(Long id, Role role) {
        AppUser user = new AppUser();
        user.setId(id);
        user.setName(role.name());
        user.setEmail(role.name().toLowerCase() + "@example.com");
        user.setRole(role);
        user.setPasswordHash("hash");
        return user;
    }
}
