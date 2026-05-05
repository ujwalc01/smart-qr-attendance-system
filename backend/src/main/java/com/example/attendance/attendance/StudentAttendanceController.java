package com.example.attendance.attendance;

import com.example.attendance.attendance.dto.AttendanceRecordResponse;
import com.example.attendance.attendance.dto.ScanAttendanceRequest;
import com.example.attendance.common.security.CurrentUser;
import com.example.attendance.common.security.RequestUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/student/attendance")
@RequiredArgsConstructor
public class StudentAttendanceController {
    private final AttendanceService attendanceService;

    @PostMapping("/scan")
    public AttendanceRecordResponse scan(
        @Valid @RequestBody ScanAttendanceRequest request,
        @AuthenticationPrincipal CurrentUser currentUser,
        HttpServletRequest httpRequest
    ) {
        return attendanceService.scan(request, currentUser.user(), RequestUtils.clientIp(httpRequest));
    }

    @GetMapping("/my")
    public List<AttendanceRecordResponse> myAttendance(@AuthenticationPrincipal CurrentUser currentUser) {
        return attendanceService.myAttendance(currentUser.user());
    }
}
