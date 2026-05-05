package com.example.attendance.attendance.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record CreateSessionRequest(
    @NotNull Long courseId,
    @NotBlank @Size(max = 160) String title,
    @NotNull Instant startsAt,
    @NotNull @Future Instant endsAt
) {
}
