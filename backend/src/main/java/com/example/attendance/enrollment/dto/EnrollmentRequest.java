package com.example.attendance.enrollment.dto;

import jakarta.validation.constraints.NotNull;

public record EnrollmentRequest(
    @NotNull Long studentId,
    @NotNull Long courseId
) {
}
