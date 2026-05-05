package com.example.attendance.course;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseRepository extends JpaRepository<Course, Long> {
    boolean existsByCode(String code);

    Optional<Course> findByCode(String code);

    List<Course> findAllByOrderByNameAsc();
}
