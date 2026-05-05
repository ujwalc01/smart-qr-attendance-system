package com.example.attendance.attendance.dto;

import com.example.attendance.attendance.AttendanceRecord;
import com.example.attendance.attendance.AttendanceStatus;
import java.time.Instant;

public record AttendanceRecordResponse(
    Long id,
    Long sessionId,
    String sessionTitle,
    Long courseId,
    String courseName,
    Long studentId,
    String studentName,
    String studentEmail,
    Instant scannedAt,
    AttendanceStatus status,
    String ipAddress,
    String deviceHash
) {
    public static AttendanceRecordResponse from(AttendanceRecord record) {
        return new AttendanceRecordResponse(
            record.getId(),
            record.getSession().getId(),
            record.getSession().getTitle(),
            record.getSession().getCourse().getId(),
            record.getSession().getCourse().getName(),
            record.getStudent().getId(),
            record.getStudent().getName(),
            record.getStudent().getEmail(),
            record.getScannedAt(),
            record.getStatus(),
            record.getIpAddress(),
            record.getDeviceHash()
        );
    }
}
