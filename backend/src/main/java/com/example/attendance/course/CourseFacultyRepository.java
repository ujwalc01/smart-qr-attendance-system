package com.example.attendance.course;

import com.example.attendance.user.AppUser;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseFacultyRepository extends JpaRepository<CourseFaculty, Long> {
    boolean existsByCourseAndFaculty(Course course, AppUser faculty);

    List<CourseFaculty> findByFaculty(AppUser faculty);
}
