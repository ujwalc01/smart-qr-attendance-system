package com.example.attendance.attendance;

import com.example.attendance.user.AppUser;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    boolean existsBySessionAndStudent(AttendanceSession session, AppUser student);

    List<AttendanceRecord> findBySessionOrderByScannedAtDesc(AttendanceSession session);

    List<AttendanceRecord> findByStudentOrderByScannedAtDesc(AppUser student);

    List<AttendanceRecord> findBySessionIn(List<AttendanceSession> sessions);
}
