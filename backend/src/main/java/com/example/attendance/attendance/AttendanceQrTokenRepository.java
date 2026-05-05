package com.example.attendance.attendance;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AttendanceQrTokenRepository extends JpaRepository<AttendanceQrToken, Long> {
    Optional<AttendanceQrToken> findByTokenHash(String tokenHash);
}
