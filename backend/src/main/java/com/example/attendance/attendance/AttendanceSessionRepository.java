package com.example.attendance.attendance;

import com.example.attendance.course.Course;
import com.example.attendance.user.AppUser;
import java.time.Instant;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceSessionRepository extends JpaRepository<AttendanceSession, Long> {
    List<AttendanceSession> findByFacultyOrderByCreatedAtDesc(AppUser faculty);

    List<AttendanceSession> findByFacultyAndCourseAndStartsAtGreaterThanEqualAndStartsAtLessThanOrderByStartsAtAsc(
        AppUser faculty,
        Course course,
        Instant startsAt,
        Instant endsBefore
    );
}
