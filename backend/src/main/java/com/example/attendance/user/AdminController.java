package com.example.attendance.user;

import com.example.attendance.course.CourseService;
import com.example.attendance.course.dto.CourseRequest;
import com.example.attendance.course.dto.CourseResponse;
import com.example.attendance.enrollment.EnrollmentService;
import com.example.attendance.enrollment.dto.EnrollmentRequest;
import com.example.attendance.user.dto.CreateUserRequest;
import com.example.attendance.user.dto.UserResponse;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminController {
    private final UserService userService;
    private final CourseService courseService;
    private final EnrollmentService enrollmentService;

    @PostMapping("/students")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createStudent(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request, Role.STUDENT);
    }

    @GetMapping("/students")
    public List<UserResponse> students() {
        return userService.byRole(Role.STUDENT);
    }

    @PostMapping("/faculty")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse createFaculty(@Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(request, Role.FACULTY);
    }

    @GetMapping("/faculty")
    public List<UserResponse> faculty() {
        return userService.byRole(Role.FACULTY);
    }

    @PostMapping("/courses")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponse createCourse(@Valid @RequestBody CourseRequest request) {
        return courseService.create(request);
    }

    @GetMapping("/courses")
    public List<CourseResponse> courses() {
        return courseService.list();
    }

    @PostMapping("/courses/{courseId}/faculty/{facultyId}")
    public CourseResponse assignFaculty(@PathVariable Long courseId, @PathVariable Long facultyId) {
        return courseService.assignFaculty(courseId, facultyId);
    }

    @PostMapping("/enrollments")
    @ResponseStatus(HttpStatus.CREATED)
    public Map<String, String> enroll(@Valid @RequestBody EnrollmentRequest request) {
        enrollmentService.enroll(request);
        return Map.of("message", "Student enrolled");
    }
}
