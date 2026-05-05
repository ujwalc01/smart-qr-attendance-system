package com.example.attendance.attendance.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ScanAttendanceRequest(
    @NotBlank @Size(max = 300) String token,
    @Size(max = 160) String deviceHash
) {
}
