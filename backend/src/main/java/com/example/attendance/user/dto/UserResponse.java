package com.example.attendance.user.dto;

import com.example.attendance.user.AppUser;
import com.example.attendance.user.Role;
import com.example.attendance.user.UserStatus;

public record UserResponse(
    Long id,
    String name,
    String email,
    Role role,
    String rollNumber,
    String employeeCode,
    UserStatus status
) {
    public static UserResponse from(AppUser user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getRole(),
            user.getRollNumber(),
            user.getEmployeeCode(),
            user.getStatus()
        );
    }
}
