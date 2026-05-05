package com.example.attendance.attendance;

import com.example.attendance.attendance.dto.AttendanceRecordResponse;
import com.example.attendance.attendance.dto.AttendanceSummaryResponse;
import com.example.attendance.attendance.dto.CreateSessionRequest;
import com.example.attendance.attendance.dto.QrTokenResponse;
import com.example.attendance.attendance.dto.SessionResponse;
import com.example.attendance.common.security.CurrentUser;
import com.example.attendance.common.security.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.time.YearMonth;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/faculty/attendance-sessions")
@RequiredArgsConstructor
public class FacultyAttendanceController {
    private final AttendanceService attendanceService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SessionResponse createSession(
        @Valid @RequestBody CreateSessionRequest request,
        @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return attendanceService.createSession(request, currentUser.user());
    }

    @GetMapping
    public List<SessionResponse> sessions(@AuthenticationPrincipal CurrentUser currentUser) {
        return attendanceService.facultySessions(currentUser.user());
    }

    @PostMapping("/{sessionId}/qr")
    public QrTokenResponse generateQr(
        @PathVariable Long sessionId,
        @AuthenticationPrincipal CurrentUser currentUser,
        HttpServletRequest request
    ) {
        return attendanceService.generateQr(sessionId, currentUser.user(), RequestUtils.clientIp(request));
    }

    @GetMapping("/{sessionId}/records")
    public List<AttendanceRecordResponse> records(
        @PathVariable Long sessionId,
        @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return attendanceService.sessionRecords(sessionId, currentUser.user());
    }

    @GetMapping("/courses/{courseId}/summary")
    public AttendanceSummaryResponse courseSummary(
        @PathVariable Long courseId,
        @RequestParam String month,
        @AuthenticationPrincipal CurrentUser currentUser
    ) {
        return attendanceService.courseAttendanceSummary(courseId, YearMonth.parse(month), currentUser.user());
    }
}
