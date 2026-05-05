package com.example.attendance.attendance.dto;

import com.example.attendance.attendance.AttendanceSession;
import com.example.attendance.attendance.SessionStatus;
import java.time.Instant;

public record SessionResponse(
    Long id,
    Long courseId,
    String courseName,
    String title,
    Instant startsAt,
    Instant endsAt,
    SessionStatus status
) {
    public static SessionResponse from(AttendanceSession session) {
        return new SessionResponse(
            session.getId(),
            session.getCourse().getId(),
            session.getCourse().getName(),
            session.getTitle(),
            session.getStartsAt(),
            session.getEndsAt(),
            session.getStatus()
        );
    }
}
