package com.example.attendance.course;

import com.example.attendance.common.security.CurrentUser;
import com.example.attendance.course.dto.CourseResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/faculty/courses")
@RequiredArgsConstructor
public class FacultyCourseController {
    private final CourseService courseService;

    @GetMapping
    public List<CourseResponse> courses(@AuthenticationPrincipal CurrentUser currentUser) {
        return courseService.listAssignedTo(currentUser.user());
    }
}
