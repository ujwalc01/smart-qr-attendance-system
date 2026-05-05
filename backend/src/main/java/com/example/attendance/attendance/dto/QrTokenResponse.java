package com.example.attendance.attendance.dto;

import java.time.Instant;

public record QrTokenResponse(
    String token,
    Instant expiresAt
) {
}
