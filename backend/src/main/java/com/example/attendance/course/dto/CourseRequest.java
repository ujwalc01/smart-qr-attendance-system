package com.example.attendance.course.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CourseRequest(
    @NotBlank @Size(max = 160) String name,
    @NotBlank @Size(max = 60) String code
) {
}
