package com.example.attendance.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
    @NotBlank @Size(max = 120) String name,
    @Email @NotBlank @Size(max = 180) String email,
    @NotBlank @Size(min = 8, max = 80) String password,
    @Size(max = 60) String rollNumber,
    @Size(max = 60) String employeeCode
) {
}
