package com.example.attendance.attendance.dto;

import java.time.Instant;
import java.util.List;

public record AttendanceSummaryResponse(
    Long courseId,
    String courseName,
    String month,
    List<SummarySession> sessions,
    List<SummaryStudent> students
) {
    public record SummarySession(
        Long id,
        String title,
        Instant startsAt
    ) {
    }

    public record SummaryStudent(
        Long studentId,
        String studentName,
        String studentEmail,
        String rollNumber,
        List<SummaryCell> attendance,
        long presentCount,
        long totalSessions,
        double attendancePercentage
    ) {
    }

    public record SummaryCell(
        Long sessionId,
        boolean present,
        Instant scannedAt
    ) {
    }
}
