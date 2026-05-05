package com.example.attendance.enrollment;

import com.example.attendance.common.exception.BadRequestException;
import com.example.attendance.common.exception.ConflictException;
import com.example.attendance.course.Course;
import com.example.attendance.course.CourseService;
import com.example.attendance.enrollment.dto.EnrollmentRequest;
import com.example.attendance.user.AppUser;
import com.example.attendance.user.Role;
import com.example.attendance.user.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final UserService userService;
    private final CourseService courseService;

    public void enroll(EnrollmentRequest request) {
        AppUser student = userService.requireUser(request.studentId());
        if (student.getRole() != Role.STUDENT) {
            throw new BadRequestException("User is not student");
        }
        Course course = courseService.requireCourse(request.courseId());
        if (enrollmentRepository.existsByStudentAndCourse(student, course)) {
            throw new ConflictException("Student already enrolled in course");
        }
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollmentRepository.save(enrollment);
    }
}
