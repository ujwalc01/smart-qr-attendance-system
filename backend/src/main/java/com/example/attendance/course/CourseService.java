package com.example.attendance.course;

import com.example.attendance.common.exception.ConflictException;
import com.example.attendance.common.exception.NotFoundException;
import com.example.attendance.common.exception.BadRequestException;
import com.example.attendance.course.dto.CourseRequest;
import com.example.attendance.course.dto.CourseResponse;
import com.example.attendance.user.AppUser;
import com.example.attendance.user.Role;
import com.example.attendance.user.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepository courseRepository;
    private final CourseFacultyRepository courseFacultyRepository;
    private final UserService userService;

    public CourseResponse create(CourseRequest request) {
        String code = request.code().trim().toUpperCase();
        if (courseRepository.existsByCode(code)) {
            throw new ConflictException("Course code already exists");
        }
        Course course = new Course();
        course.setName(request.name().trim());
        course.setCode(code);
        return CourseResponse.from(courseRepository.save(course));
    }

    public List<CourseResponse> list() {
        return courseRepository.findAllByOrderByNameAsc().stream().map(CourseResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public List<CourseResponse> listAssignedTo(AppUser faculty) {
        return courseFacultyRepository.findByFaculty(faculty).stream()
            .map(CourseFaculty::getCourse)
            .map(CourseResponse::from)
            .toList();
    }

    public Course requireCourse(Long id) {
        return courseRepository.findById(id).orElseThrow(() -> new NotFoundException("Course not found"));
    }

    public CourseResponse assignFaculty(Long courseId, Long facultyId) {
        Course course = requireCourse(courseId);
        AppUser faculty = userService.requireUser(facultyId);
        if (faculty.getRole() != Role.FACULTY) {
            throw new BadRequestException("User is not faculty");
        }
        if (!courseFacultyRepository.existsByCourseAndFaculty(course, faculty)) {
            CourseFaculty assignment = new CourseFaculty();
            assignment.setCourse(course);
            assignment.setFaculty(faculty);
            courseFacultyRepository.save(assignment);
        }
        return CourseResponse.from(course);
    }
}
