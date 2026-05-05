package com.example.attendance.enrollment;

import com.example.attendance.course.Course;
import com.example.attendance.user.AppUser;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {
    boolean existsByStudentAndCourse(AppUser student, Course course);

    boolean existsByStudentAndCourseAndStatus(AppUser student, Course course, EnrollmentStatus status);

    List<Enrollment> findByCourseAndStatusOrderByStudent_NameAsc(Course course, EnrollmentStatus status);
}
