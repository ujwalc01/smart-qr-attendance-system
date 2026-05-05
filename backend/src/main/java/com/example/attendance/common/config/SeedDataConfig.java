package com.example.attendance.common.config;

import com.example.attendance.course.Course;
import com.example.attendance.course.CourseFaculty;
import com.example.attendance.course.CourseFacultyRepository;
import com.example.attendance.course.CourseRepository;
import com.example.attendance.enrollment.Enrollment;
import com.example.attendance.enrollment.EnrollmentRepository;
import com.example.attendance.user.AppUser;
import com.example.attendance.user.Role;
import com.example.attendance.user.UserRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class SeedDataConfig {
    private final UserRepository userRepository;
    private final CourseRepository courseRepository;
    private final CourseFacultyRepository courseFacultyRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final PasswordEncoder passwordEncoder;

    @Bean
    CommandLineRunner seedDemoUsers() {
        return args -> {
            seedUser("Priya Nair", "admin@example.com", "Admin@12345", Role.ADMIN, null, "ADM001");

            List<AppUser> faculty = List.of(
                seedUser("Dr. Kavita Menon", "faculty@example.com", "Faculty@12345", Role.FACULTY, null, "FAC001"),
                seedUser("Dr. Meera Iyer", "meera.iyer@example.com", "Faculty@12345", Role.FACULTY, null, "FAC002"),
                seedUser("Prof. Arjun Rao", "arjun.rao@example.com", "Faculty@12345", Role.FACULTY, null, "FAC003"),
                seedUser("Dr. Nisha Kapoor", "nisha.kapoor@example.com", "Faculty@12345", Role.FACULTY, null, "FAC004"),
                seedUser("Prof. Sandeep Kulkarni", "sandeep.kulkarni@example.com", "Faculty@12345", Role.FACULTY, null, "FAC005")
            );

            List<AppUser> students = new ArrayList<>();
            String[] studentNames = {
                "Aarav Sharma",
                "Aditi Rao",
                "Akshay Verma",
                "Ananya Gupta",
                "Arjun Nair",
                "Diya Patel",
                "Ishaan Mehta",
                "Kavya Reddy",
                "Kiran Bhat",
                "Lakshmi Menon",
                "Manav Joshi",
                "Meera Kulkarni",
                "Neha Singh",
                "Nikhil Iyer",
                "Pooja Shah",
                "Pranav Krishnan",
                "Riya Das",
                "Rohan Kapoor",
                "Saanvi Jain",
                "Sahil Khan",
                "Shreya Nair",
                "Tanvi Agarwal",
                "Varun Rao",
                "Vikram Sinha",
                "Zoya Fernandes"
            };
            students.add(seedUser(studentNames[0], "student@example.com", "Student@12345", Role.STUDENT, "STU001", null));
            for (int i = 2; i <= studentNames.length; i++) {
                String number = String.format("%03d", i);
                students.add(seedUser(
                    studentNames[i - 1],
                    "student" + number + "@example.com",
                    "Student@12345",
                    Role.STUDENT,
                    "STU" + number,
                    null
                ));
            }

            List<Course> courses = List.of(
                seedCourse("Data Structures and Algorithms", "CS201"),
                seedCourse("Database Management Systems", "CS202"),
                seedCourse("Operating Systems", "CS203"),
                seedCourse("Computer Networks", "CS204"),
                seedCourse("Software Engineering", "CS205"),
                seedCourse("Web Application Security", "CS206"),
                seedCourse("Artificial Intelligence Fundamentals", "CS207"),
                seedCourse("Cloud Computing", "CS208")
            );

            assign(faculty.get(0), courses.get(0), courses.get(1));
            assign(faculty.get(1), courses.get(2), courses.get(5));
            assign(faculty.get(2), courses.get(3), courses.get(4));
            assign(faculty.get(3), courses.get(1), courses.get(6));
            assign(faculty.get(4), courses.get(7), courses.get(5));

            enrollStudents(students, courses);
        };
    }

    private AppUser seedUser(String name, String email, String password, Role role, String rollNumber, String employeeCode) {
        return userRepository.findByEmail(email).map(user -> {
            user.setName(name);
            user.setRole(role);
            user.setRollNumber(rollNumber);
            user.setEmployeeCode(employeeCode);
            if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                user.setPasswordHash(passwordEncoder.encode(password));
            }
            return userRepository.save(user);
        }).orElseGet(() -> {
            AppUser user = new AppUser();
            user.setName(name);
            user.setEmail(email);
            user.setPasswordHash(passwordEncoder.encode(password));
            user.setRole(role);
            user.setRollNumber(rollNumber);
            user.setEmployeeCode(employeeCode);
            return userRepository.save(user);
        });
    }

    private Course seedCourse(String name, String code) {
        return courseRepository.findByCode(code)
            .map(course -> {
                course.setName(name);
                return courseRepository.save(course);
            })
            .orElseGet(() -> {
                Course course = new Course();
                course.setName(name);
                course.setCode(code);
                return courseRepository.save(course);
            });
    }

    private void assign(AppUser faculty, Course... courses) {
        for (Course course : courses) {
            if (!courseFacultyRepository.existsByCourseAndFaculty(course, faculty)) {
                CourseFaculty assignment = new CourseFaculty();
                assignment.setCourse(course);
                assignment.setFaculty(faculty);
                courseFacultyRepository.save(assignment);
            }
        }
    }

    private void enrollStudents(List<AppUser> students, List<Course> courses) {
        for (int i = 0; i < students.size(); i++) {
            AppUser student = students.get(i);
            enroll(student, courses.get(i % courses.size()));
            enroll(student, courses.get((i + 1) % courses.size()));
            enroll(student, courses.get((i + 2) % courses.size()));
        }
    }

    private void enroll(AppUser student, Course course) {
        if (enrollmentRepository.existsByStudentAndCourse(student, course)) {
            return;
        }
        Enrollment enrollment = new Enrollment();
        enrollment.setStudent(student);
        enrollment.setCourse(course);
        enrollmentRepository.save(enrollment);
    }
}
