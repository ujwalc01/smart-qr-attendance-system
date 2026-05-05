package com.example.attendance.auth.dto;

import com.example.attendance.user.dto.UserResponse;

public record LoginResponse(
    String token,
    long expiresInMinutes,
    UserResponse user
) {
}
